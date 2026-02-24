package com.evaluty.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "valoraciones")
public class Valoracion {
    @Id private String id;

    // Datos de la vivienda consultada
    private String direccion;
    private Integer metrosCuadrados;
    private Integer habitaciones;

    // Resultados del algoritmo
    private Double precioEstimado;
    private Double precioPorMetroCuadrado;
    private String rangoConfianza;

    // Catastro (opcional)
    private String numeroCatastro;
    private Double valoracionMinimaOficial;
    private String avisoFiscal;

    // Comparables del scraping
    private List<PropiedadComparable> comparables;

    // Metadatos
    private String userId;
    @Builder.Default private Boolean esPublica = false;
    @CreatedDate private LocalDateTime fechaCreacion;
}
