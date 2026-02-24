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
    private final CatastroService catastroService;

    /**
     * Flujo completo de valoración:
     * 1. Llama al bot Python para obtener comparables
     * 2. Calcula el precio estimado con el algoritmo
     * 3. Si se proporcionó RC, consulta el catastro
     * 4. Guarda la valoración en MongoDB
     */
    public ValuationResponse solicitarValoracion(ValuationRequest request, String userEmail) {

        // 1. Obtener comparables via scraping
        log.info("Iniciando valoración para: {}", request.getDireccion());
        List<PropiedadComparable> comparables = pythonBotService.ejecutarBot(
                request.getDireccion(),
                request.getMetrosCuadrados(),
                request.getHabitaciones()
        );

        if (comparables.isEmpty()) {
            throw new RuntimeException("No se encontraron propiedades comparables para esta dirección");
        }

        // 2. Calcular precio estimado (media ponderada por m2)
        double precioEstimado = calcularPrecioEstimado(comparables, request.getMetrosCuadrados());
        double precioPorM2    = precioEstimado / request.getMetrosCuadrados();
        String rangoConfianza = calcularRango(comparables, request.getMetrosCuadrados());

        // 3. Consulta catastro (opcional)
        Double valoracionMinima = null;
        String avisoFiscal = null;
        if (request.getNumeroCatastro() != null && !request.getNumeroCatastro().isBlank()) {
            try {
                CatastroResponse catastro = catastroService.consultarPorReferencia(request.getNumeroCatastro());
                valoracionMinima = catastro.getValoracionMinima();
                avisoFiscal = catastro.getAvisoFiscal();

                // Si precio estimado < mínimo catastro, añadimos aviso específico
                if (valoracionMinima != null && precioEstimado < valoracionMinima) {
                    avisoFiscal = "⚠️ ATENCIÓN: El precio estimado (" +
                            String.format("%.0f€", precioEstimado) +
                            ") es INFERIOR al valor mínimo de transmisión (" +
                            String.format("%.0f€", valoracionMinima) +
                            "). " + avisoFiscal;
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener datos del catastro: {}", e.getMessage());
            }
        }

        // 4. Guardar en MongoDB
        String userId = userRepository.findByEmail(userEmail)
                .map(User::getId).orElse(null);

        Valoracion valoracion = Valoracion.builder()
                .direccion(request.getDireccion())
                .metrosCuadrados(request.getMetrosCuadrados())
                .habitaciones(request.getHabitaciones())
                .precioEstimado(precioEstimado)
                .precioPorMetroCuadrado(precioPorM2)
                .rangoConfianza(rangoConfianza)
                .numeroCatastro(request.getNumeroCatastro())
                .valoracionMinimaOficial(valoracionMinima)
                .avisoFiscal(avisoFiscal)
                .comparables(comparables)
                .userId(userId)
                .build();

        Valoracion saved = valoracionRepository.save(valoracion);

        // Actualizar referencias en el usuario
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

    public ValuationResponse obtenerValoracion(String id, String userEmail) {
        Valoracion v = valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));
        return toResponse(v);
    }

    public List<ValuationResponse> obtenerMisValoraciones(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(u -> valoracionRepository.findByUserId(u.getId())
                        .stream().map(this::toResponse).toList())
                .orElse(Collections.emptyList());
    }

    // ---- Algoritmo de valoración ----

    /**
     * Calcula la media ponderada del precio/m² de los comparables
     * y la proyecta sobre los m² del inmueble consultado.
     */
    private double calcularPrecioEstimado(List<PropiedadComparable> comparables, int metrosCuadrados) {
        // Descartamos outliers (eliminamos el más caro y el más barato si hay más de 4)
        List<PropiedadComparable> filtrados = filtrarOutliers(comparables);

        double sumPonderacion = 0;
        double sumPrecioM2 = 0;

        for (PropiedadComparable p : filtrados) {
            if (p.getMetrosCuadrados() != null && p.getMetrosCuadrados() > 0) {
                double pm2 = (double) p.getPrecio() / p.getMetrosCuadrados();
                double peso = 1.0 / Math.abs(p.getMetrosCuadrados() - metrosCuadrados + 1);
                sumPrecioM2 += pm2 * peso;
                sumPonderacion += peso;
            }
        }

        double precioM2Estimado = sumPonderacion > 0 ? sumPrecioM2 / sumPonderacion :
                filtrados.stream().mapToDouble(PropiedadComparable::getPrecioPorM2).average().orElse(0);

        return Math.round(precioM2Estimado * metrosCuadrados);
    }

    private String calcularRango(List<PropiedadComparable> comparables, int m2) {
        DoubleSummaryStatistics stats = comparables.stream()
                .mapToDouble(p -> p.getPrecioPorM2() != null ? p.getPrecioPorM2() : 0)
                .filter(v -> v > 0)
                .summaryStatistics();

        long min = Math.round(stats.getMin() * m2);
        long max = Math.round(stats.getMax() * m2);
        return String.format("%,.0f€ - %,.0f€", (double) min, (double) max);
    }

    private List<PropiedadComparable> filtrarOutliers(List<PropiedadComparable> comparables) {
        if (comparables.size() <= 4) return comparables;

        List<PropiedadComparable> sorted = comparables.stream()
                .sorted(Comparator.comparingInt(PropiedadComparable::getPrecio))
                .toList();

        // Eliminamos el más barato y el más caro
        return sorted.subList(1, sorted.size() - 1);
    }

    private ValuationResponse toResponse(Valoracion v) {
        return ValuationResponse.builder()
                .id(v.getId())
                .direccion(v.getDireccion())
                .metrosCuadrados(v.getMetrosCuadrados())
                .habitaciones(v.getHabitaciones())
                .precioEstimado(v.getPrecioEstimado())
                .precioPorMetroCuadrado(v.getPrecioPorMetroCuadrado())
                .rangoConfianza(v.getRangoConfianza())
                .valoracionMinimaOficial(v.getValoracionMinimaOficial())
                .avisoFiscal(v.getAvisoFiscal())
                .comparables(v.getComparables())
                .fechaCreacion(v.getFechaCreacion())
                .build();
    }
}
