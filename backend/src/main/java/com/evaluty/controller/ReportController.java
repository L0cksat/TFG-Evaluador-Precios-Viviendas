package com.evaluty.controller;

import com.evaluty.repository.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;

@Slf4j
@RestController
@RequestMapping("/api/informes")
@RequiredArgsConstructor
public class ReportController {

    private final ValoracionRepository valoracionRepository;

    /**
     * GET /api/informes/{id}
     * Sirve el PDF que generó Python (informe_tasacion.pdf) para una valoración concreta.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> descargarInforme(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Obtenemos la ruta del PDF guardada en MongoDB cuando se creó la valoración
        String rutaPdf = valoracionRepository.findById(id)
                .map(v -> v.getRutaPdf())
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada: " + id));

        Path pdfPath = Paths.get(rutaPdf);

        if (!Files.exists(pdfPath)) {
            log.warn("PDF no encontrado en disco: {}", rutaPdf);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new PathResource(pdfPath);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"informe-evaluty-" + id + ".pdf\"")
                .body(resource);
    }
}
