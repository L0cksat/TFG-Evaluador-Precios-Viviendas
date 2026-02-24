package com.evaluty.controller;

import com.evaluty.dto.UserProfileResponse;
import com.evaluty.model.User;
import com.evaluty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** GET /api/users/me — Perfil del usuario autenticado */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellidos(user.getApellidos())
                .createdAt(user.getCreatedAt())
                .valoracionIds(user.getValoracionIds())
                .build());
    }

    /** DELETE /api/users/me — Baja de cuenta */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {

        userRepository.findByEmail(userDetails.getUsername())
                .ifPresent(userRepository::delete);
        return ResponseEntity.noContent().build();
    }
}
