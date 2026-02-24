package com.evaluty.dto;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class CatastroResponse {
    private String numeroCatastro;
    private String direccion;
    private Double valoracionMinima;
    private String uso;
    private Double superficieConstruida;
    private String avisoFiscal;
    private String municipio;
}
