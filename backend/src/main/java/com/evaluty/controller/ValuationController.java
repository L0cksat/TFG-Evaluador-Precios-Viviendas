package com.evaluty.controller;

import com.evaluty.dto.*;
import com.evaluty.service.ValuationService;
import jakarta.validation.Valid;
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
     * Solicita una nueva valoración. Lanza el bot Python en background.
     */
    @PostMapping
    public ResponseEntity<ValuationResponse> solicitar(
            @Valid @RequestBody ValuationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ValuationResponse response = valuationService.solicitarValoracion(
                request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/valoraciones
     * Devuelve el historial de valoraciones del usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<List<ValuationResponse>> getMisValoraciones(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                valuationService.obtenerMisValoraciones(userDetails.getUsername()));
    }

    /**
     * GET /api/valoraciones/{id}
     * Detalle de una valoración concreta.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ValuationResponse> getValoracion(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                valuationService.obtenerValoracion(id, userDetails.getUsername()));
    }
}
