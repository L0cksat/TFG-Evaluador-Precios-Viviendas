package com.evaluty.dto;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String nombre;
    private String role;
}
