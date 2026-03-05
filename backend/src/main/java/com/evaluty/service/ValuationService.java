package com.evaluty.service;

import com.evaluty.dto.*;
import com.evaluty.model.*;
import com.evaluty.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValuationService {

    private final ValoracionRepository valoracionRepository;
    private final UserRepository userRepository;
    private final PythonBotService pythonBotService;

    /**
     * Flujo completo:
     * 1. Lanza main.py (orquestador Python) — hace scraping, cálculo y PDF
     * 2. Lee precio_estimado.json
     * 3. Guarda la valoración en MongoDB
     * 4. Devuelve el resultado al controller
     */
    public ValuationResponse solicitarValoracion(ValuationRequest request, String userEmail) {

        // 1. Ejecutar Python y obtener resultado
        Map<String, Object> resultado = pythonBotService.ejecutar(request);

        // 2. Mapear el JSON de Python a nuestro modelo
        String userId = userRepository.findByEmail(userEmail)
                .map(User::getId).orElse(null);

        Valoracion valoracion = Valoracion.builder()
                .status((String) resultado.get("status"))
                .direccion((String) resultado.get("direccion"))
                .metrosCuadrados(toDouble(resultado.get("metros_cuadrados")))
                .precioEstimado(toDouble(resultado.get("precio_estimado")))
                .mediaM2Zona(toDouble(resultado.get("media_m2_zona")))
                .cantidadCasasAnalizadas(toInt(resultado.get("cantidad_casas_analizadas")))
                // Modo Pro — puede ser null si es modo básico
                .referenciaCatastral(request.getReferenciaCatastral())
                .valorMinimoHacienda(toDouble(resultado.get("valor_minimo_hacienda")))
                .rutaPdf(pythonBotService.getRutaPdf().toString())
                .userId(userId)
                .build();

        Valoracion saved = valoracionRepository.save(valoracion);

        // 3. Actualizar el historial del usuario
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                List<String> ids = user.getValoracionIds() == null
                        ? new ArrayList<>() : new ArrayList<>(user.getValoracionIds());
                ids.add(saved.getId());
                user.setValoracionIds(ids);
                userRepository.save(user);
            });
        }

        return toResponse(saved);
    }

    public ValuationResponse obtenerValoracion(String id) {
        return valoracionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada: " + id));
    }

    public List<ValuationResponse> obtenerMisValoraciones(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(u -> valoracionRepository.findByUserId(u.getId())
                        .stream().map(this::toResponse).toList())
                .orElse(Collections.emptyList());
    }

    // ---- Helpers de conversión ----

    private ValuationResponse toResponse(Valoracion v) {
        return ValuationResponse.builder()
                .id(v.getId())
                .status(v.getStatus())
                .direccion(v.getDireccion())
                .metrosCuadrados(v.getMetrosCuadrados())
                .precioEstimado(v.getPrecioEstimado())
                .mediaM2Zona(v.getMediaM2Zona())
                .cantidadCasasAnalizadas(v.getCantidadCasasAnalizadas())
                .referenciaCatastral(v.getReferenciaCatastral())
                .valorMinimoHacienda(v.getValorMinimoHacienda())
                .urlInforme("/api/informes/" + v.getId())
                .fechaCreacion(v.getFechaCreacion())
                .build();
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
