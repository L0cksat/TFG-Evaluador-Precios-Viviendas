package com.evaluty.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ValuationResponse {

    private String id;

    // Datos de la valoración
    private String status;
    private String direccion;
    private Double metrosCuadrados;
    private Double precioEstimado;
    private Double mediaM2Zona;
    private Integer cantidadCasasAnalizadas;

    // Solo en Modo Pro
    private String referenciaCatastral;
    private Double valorMinimoHacienda;

    // Para descargar el informe PDF
    private String urlInforme;

    private LocalDateTime fechaCreacion;
}
