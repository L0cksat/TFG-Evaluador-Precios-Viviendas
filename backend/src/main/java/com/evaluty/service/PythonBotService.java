package com.evaluty.service;

import com.evaluty.dto.ValuationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

/**
 * Lanza main.py como subproceso y devuelve el contenido de precio_estimado.json.
 *
 * main.py tiene dos modos:
 *   Modo Básico (4 args): python main.py <dirección> <metros> <habitaciones>
 *   Modo Pro    (3 args): python main.py <referencia_catastral> <habitaciones>
 *
 * main.py orquesta internamente: catastro.py → bot_inmobiliario.py → calculo.py → generador_pdf.py
 * Los archivos de salida quedan en data-service/json/
 */
@Slf4j
@Service
public class PythonBotService {

    @Value("${evaluty.python.executable}")
    private String pythonExecutable;

    @Value("${evaluty.python.main-path}")
    private String mainPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Ejecuta el flujo completo de Python y devuelve el Map con los datos de precio_estimado.json.
     */
    public Map<String, Object> ejecutar(ValuationRequest request) {
        ProcessBuilder pb;

        if (request.esModoProValido()) {
            log.info("Lanzando Python Modo Pro — RC: {}, habs: {}",
                    request.getReferenciaCatastral(), request.getHabitaciones());
            pb = new ProcessBuilder(
                    pythonExecutable,
                    "main.py",
                    request.getReferenciaCatastral(),
                    String.valueOf(request.getHabitaciones())
            );
        } else if (request.esModoBasicoValido()) {
            log.info("Lanzando Python Modo Básico — dir: {}, m2: {}, habs: {}",
                    request.getDireccion(), request.getMetrosCuadrados(), request.getHabitaciones());
            pb = new ProcessBuilder(
                    pythonExecutable,
                    "main.py",
                    request.getDireccion(),
                    String.valueOf(request.getMetrosCuadrados()),
                    String.valueOf(request.getHabitaciones())
            );
        } else {
            throw new IllegalArgumentException(
                "Debes proporcionar o (dirección + metros + habitaciones) " +
                "o (referencia catastral + habitaciones)");
        }

        // El working directory debe ser la carpeta data-service,
        // porque main.py llama a los demás scripts con rutas relativas
        File dataServiceDir = new File(mainPath).getParentFile();
        pb.directory(dataServiceDir);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // Logueamos el output del proceso para debug
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> log.debug("[PYTHON] {}", line));
            }

            int exitCode = process.waitFor();
            log.info("Python terminó con código: {}", exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("El proceso Python terminó con error (código " + exitCode + ")");
            }

            // Leemos el JSON de resultado que genera calculo.py
            Path rutaJson = dataServiceDir.toPath().resolve("json/precio_estimado.json");

            if (!Files.exists(rutaJson)) {
                throw new RuntimeException("Python no generó el archivo precio_estimado.json");
            }

            Map<String, Object> resultado = objectMapper.readValue(
                    rutaJson.toFile(), new TypeReference<>() {});

            if ("error".equals(resultado.get("status"))) {
                throw new RuntimeException("Python devolvió error: " + resultado.get("message"));
            }

            return resultado;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error ejecutando Python", e);
            throw new RuntimeException("No se pudo ejecutar el proceso de valoración: " + e.getMessage());
        }
    }

    /**
     * Devuelve la ruta absoluta al PDF generado por generador_pdf.py.
     */
    public Path getRutaPdf() {
        File dataServiceDir = new File(mainPath).getParentFile();
        return dataServiceDir.toPath().resolve("json/informe_tasacion.pdf");
    }
}
