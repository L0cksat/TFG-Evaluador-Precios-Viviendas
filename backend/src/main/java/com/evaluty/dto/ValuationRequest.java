package com.evaluty.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ValuationRequest {

    // --- MODO BÁSICO: los tres campos manuales ---
    private String direccion;

    @Min(value = 1, message = "Los metros deben ser mayor que 0")
    private Integer metrosCuadrados;

    @Min(value = 0)
    private Integer habitaciones;

    // --- MODO PRO: referencia catastral (opcional) ---
    // Si se proporciona, main.py usa el catastro para obtener dirección y metros
    private String referenciaCatastral;

    /**
     * Valida que el request tiene al menos uno de los dos modos válidos:
     * - Modo Básico: direccion + metrosCuadrados + habitaciones
     * - Modo Pro:    referenciaCatastral + habitaciones
     */
    public boolean esModoBasicoValido() {
        return direccion != null && !direccion.isBlank()
                && metrosCuadrados != null && metrosCuadrados > 0
                && habitaciones != null;
    }

    public boolean esModoProValido() {
        return referenciaCatastral != null && !referenciaCatastral.isBlank()
                && habitaciones != null;
    }
}
