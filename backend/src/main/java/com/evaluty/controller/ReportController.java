package com.evaluty.controller;

import com.evaluty.model.Valoracion;
import com.evaluty.repository.ValoracionRepository;
import com.evaluty.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/informes")
@RequiredArgsConstructor
public class ReportController {

    private final PdfService pdfService;
    private final ValoracionRepository valoracionRepository;

    /**
     * GET /api/informes/{id}
     * Genera y descarga el informe PDF de una valoración.
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> descargarInforme(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Valoracion valoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));

        byte[] pdf = pdfService.generarInforme(valoracion);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("informe-evaluty-" + id + ".pdf")
                .build());
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
