package com.evaluty.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CatastroRequest {
    @NotBlank(message = "El número de catastro es obligatorio")
    @Size(min = 14, max = 20, message = "El número de catastro no tiene el formato correcto")
    private String numeroCatastro;
}
