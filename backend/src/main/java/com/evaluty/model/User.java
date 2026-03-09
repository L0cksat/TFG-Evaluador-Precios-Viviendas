package com.evaluty.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id private String id;
    @Indexed(unique = true) private String email;
    private String password;
    private String nombre;
    private String apellidos;
    @Builder.Default private String role = "ROLE_USER";
    @CreatedDate private LocalDateTime createdAt;
    private List<String> valoracionIds;
}
