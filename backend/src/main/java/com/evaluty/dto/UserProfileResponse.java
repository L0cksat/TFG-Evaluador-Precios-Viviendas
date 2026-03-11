package com.evaluty.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String nombre;
    private String apellidos;
    private LocalDateTime createdAt;
    private List<String> valoracionIds;
}
