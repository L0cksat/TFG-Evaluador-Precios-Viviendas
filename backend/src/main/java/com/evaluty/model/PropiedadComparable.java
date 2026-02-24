package com.evaluty.model;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PropiedadComparable {
    private String titulo;
    private String ubicacion;
    private Integer precio;
    private Integer metrosCuadrados;
    private Integer habitaciones;
    private Double precioPorM2;
}
