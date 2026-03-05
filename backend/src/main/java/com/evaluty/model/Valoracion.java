package com.evaluty.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "valoraciones")
public class Valoracion {

    @Id
    private String id;

    // --- Datos de entrada ---
    private String direccion;
    private Double metrosCuadrados;

    // --- Resultados de calculo.py ---
    private String status;
    private Double precioEstimado;
    private Double mediaM2Zona;
    private Integer cantidadCasasAnalizadas;

    // --- Solo en Modo Pro (con referencia catastral) ---
    private String referenciaCatastral;
    private Double valorMinimoHacienda;

    // --- Ruta al PDF generado por Python ---
    private String rutaPdf;

    // --- Metadatos ---
    private String userId;

    @CreatedDate
    private LocalDateTime fechaCreacion;
}
