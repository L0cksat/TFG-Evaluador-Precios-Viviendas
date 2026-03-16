package com.evaluty.controller;

import com.evaluty.dto.*;
import com.evaluty.service.ValuationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValuationController {

    private final ValuationService valuationService;

    /**
     * POST /api/valoraciones
     *
     * Modo Básico — body esperado:
     * { "direccion": "...", "metrosCuadrados": 80, "habitaciones": 3 }
     *
     * Modo Pro — body esperado:
     * { "referenciaCatastral": "1234567AB1234A0001AB", "habitaciones": 3 }
     */
    @PostMapping
    public ResponseEntity<ValuationResponse> solicitar(
            @RequestBody ValuationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!request.esModoBasicoValido() && !request.esModoProValido()) {
            return ResponseEntity.badRequest().build();
        }

        String username = null;
        if (userDetails !=null){
            username = userDetails.getUsername();
        }

        ValuationResponse response = valuationService.solicitarValoracion(
                request, username);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/valoraciones
     * Historial del usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<List<ValuationResponse>> getMisValoraciones(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                valuationService.obtenerMisValoraciones(userDetails.getUsername()));
    }

    /**
     * GET /api/valoraciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ValuationResponse> getValoracion(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(valuationService.obtenerValoracion(id));
    }
}
