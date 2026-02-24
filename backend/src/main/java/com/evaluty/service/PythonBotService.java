package com.evaluty.service;

import com.evaluty.model.PropiedadComparable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * Servicio que lanza el bot de scraping Python como subproceso
 * y parsea el JSON resultante.
 */
@Slf4j
@Service
public class PythonBotService {

    @Value("${evaluty.python.executable}")
    private String pythonExecutable;

    @Value("${evaluty.python.bot-path}")
    private String botPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Ejecuta el bot y devuelve la lista de propiedades comparables.
     */
    public List<PropiedadComparable> ejecutarBot(String direccion, int m2, int habitaciones) {
        log.info("Lanzando bot Python: direccion='{}', m2={}, hab={}", direccion, m2, habitaciones);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    botPath,
                    direccion,
                    String.valueOf(m2),
                    String.valueOf(habitaciones)
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Leemos todo el output del proceso
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[BOT] {}", line);
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            log.info("Bot terminado con código: {}", exitCode);

            if (exitCode != 0) {
                log.error("El bot Python terminó con error. Output:\n{}", output);
                return Collections.emptyList();
            }

            // El JSON queda en json/resultados_scraping.json (relativo al bot)
            // Lo leemos desde ahí directamente
            File jsonFile = resolveJsonFile();
            if (!jsonFile.exists()) {
                log.warn("No se encontró el archivo JSON de resultados");
                return Collections.emptyList();
            }

            List<Map<String, Object>> rawList = objectMapper.readValue(
                    jsonFile, new TypeReference<>() {});

            return rawList.stream()
                    .map(this::mapToPropiedadComparable)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (IOException | InterruptedException e) {
            log.error("Error ejecutando el bot Python", e);
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    private PropiedadComparable mapToPropiedadComparable(Map<String, Object> raw) {
        try {
            int precio = Integer.parseInt(raw.getOrDefault("precio_raw", "0").toString());
            int m2 = ((Number) raw.getOrDefault("m2", 0)).intValue();
            int hab = ((Number) raw.getOrDefault("habitaciones", 0)).intValue();

            if (precio == 0 || m2 == 0) return null;

            return PropiedadComparable.builder()
                    .titulo((String) raw.getOrDefault("titulo", "Sin título"))
                    .ubicacion((String) raw.getOrDefault("ubicacion", "Desconocida"))
                    .precio(precio)
                    .metrosCuadrados(m2)
                    .habitaciones(hab)
                    .precioPorM2(m2 > 0 ? (double) precio / m2 : 0.0)
                    .build();
        } catch (Exception e) {
            log.warn("Error mapeando propiedad comparable: {}", raw, e);
            return null;
        }
    }

    private File resolveJsonFile() {
        // Ruta del JSON relativa al bot script
        File botFile = new File(botPath);
        return new File(botFile.getParent(), "json/resultados_scraping.json");
    }
}
