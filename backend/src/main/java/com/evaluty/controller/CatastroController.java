package com.evaluty.controller;

import com.evaluty.dto.*;
import com.evaluty.service.CatastroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catastro")
@RequiredArgsConstructor
public class CatastroController {

    private final CatastroService catastroService;

    /**
     * POST /api/catastro/consultar
     * Consulta el valor mínimo de transmisión por número de catastro.
     */
    @PostMapping("/consultar")
    public ResponseEntity<CatastroResponse> consultar(
            @Valid @RequestBody CatastroRequest request) {

        return ResponseEntity.ok(
                catastroService.consultarPorReferencia(request.getNumeroCatastro()));
    }
}
