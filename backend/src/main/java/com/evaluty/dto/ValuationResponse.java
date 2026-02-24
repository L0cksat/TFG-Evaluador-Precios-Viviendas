package com.evaluty.dto;

import com.evaluty.model.PropiedadComparable;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ValuationResponse {
    private String id;
    private String direccion;
    private Integer metrosCuadrados;
    private Integer habitaciones;

    private Double precioEstimado;
    private Double precioPorMetroCuadrado;
    private String rangoConfianza;

    // Catastro (solo si se solicitó)
    private Double valoracionMinimaOficial;
    private String avisoFiscal;

    private List<PropiedadComparable> comparables;
    private LocalDateTime fechaCreacion;
}
