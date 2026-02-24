package com.evaluty.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ValuationRequest {
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotNull @Min(value = 20, message = "Los m2 mínimos son 20")
    private Integer metrosCuadrados;

    @NotNull @Min(value = 1) @Max(value = 10)
    private Integer habitaciones;

    // Opcional: si se proporciona, también se consulta el catastro
    private String numeroCatastro;
}
