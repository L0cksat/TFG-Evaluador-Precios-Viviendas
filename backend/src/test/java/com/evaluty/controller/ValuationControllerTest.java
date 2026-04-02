package com.evaluty.controller;

import com.evaluty.dto.*;
import com.evaluty.security.JwtAuthFilter;
import com.evaluty.security.JwtUtil;
import com.evaluty.service.ValuationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ValuationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ValuationService valuationService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser(username = "testuser")
    void testCrearValoracion_ModoBasico() throws Exception {
        ValuationRequest request = new ValuationRequest();
        request.setDireccion("Calle Mayor 10, Madrid");
        request.setMetrosCuadrados(80);
        request.setHabitaciones(3);

        ValuationResponse response = ValuationResponse.builder()
                .id("abc123")
                .status("COMPLETED")
                .direccion("Calle Mayor 10, Madrid")
                .metrosCuadrados(80.0)
                .precioEstimado(240000.0)
                .mediaM2Zona(3000.0)
                .cantidadCasasAnalizadas(15)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(valuationService.solicitarValoracion(any(ValuationRequest.class), eq("testuser")))
                .thenReturn(response);

        mockMvc.perform(post("/api/valoraciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("abc123"))
                .andExpect(jsonPath("$.direccion").value("Calle Mayor 10, Madrid"))
                .andExpect(jsonPath("$.precioEstimado").value(240000.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCrearValoracion_ModoPro() throws Exception {
        ValuationRequest request = new ValuationRequest();
        request.setReferenciaCatastral("1234567AB1234A0001AB");
        request.setHabitaciones(3);

        ValuationResponse response = ValuationResponse.builder()
                .id("def456")
                .status("COMPLETED")
                .referenciaCatastral("1234567AB1234A0001AB")
                .direccion("Avenida Barcelona 25, Valencia")
                .metrosCuadrados(95.0)
                .precioEstimado(285000.0)
                .valorMinimoHacienda(250000.0)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(valuationService.solicitarValoracion(any(ValuationRequest.class), eq("testuser")))
                .thenReturn(response);

        mockMvc.perform(post("/api/valoraciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("def456"))
                .andExpect(jsonPath("$.referenciaCatastral").value("1234567AB1234A0001AB"))
                .andExpect(jsonPath("$.precioEstimado").value(285000.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCrearValoracion_Valida() throws Exception {
        ValuationRequest request = new ValuationRequest();
        request.setDireccion("Calle Prueba 1");
        request.setMetrosCuadrados(50);
        request.setHabitaciones(2);

        ValuationResponse response = ValuationResponse.builder()
                .id("test123")
                .status("COMPLETED")
                .direccion("Calle Prueba 1")
                .metrosCuadrados(50.0)
                .precioEstimado(150000.0)
                .mediaM2Zona(3000.0)
                .cantidadCasasAnalizadas(10)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(valuationService.solicitarValoracion(any(ValuationRequest.class), eq("testuser")))
                .thenReturn(response);

        mockMvc.perform(post("/api/valoraciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test123"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCrearValoracion_RequestInvalido() throws Exception {
        ValuationRequest request = new ValuationRequest();
        request.setDireccion("");

        mockMvc.perform(post("/api/valoraciones")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testObtenerMisValoraciones() throws Exception {
        ValuationResponse v1 = ValuationResponse.builder()
                .id("id1")
                .direccion("Calle A")
                .precioEstimado(150000.0)
                .build();

        ValuationResponse v2 = ValuationResponse.builder()
                .id("id2")
                .direccion("Calle B")
                .precioEstimado(200000.0)
                .build();

        when(valuationService.obtenerMisValoraciones("testuser"))
                .thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/valoraciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].direccion").value("Calle A"))
                .andExpect(jsonPath("$[1].direccion").value("Calle B"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testObtenerValoracionPorId() throws Exception {
        ValuationResponse response = ValuationResponse.builder()
                .id("id123")
                .direccion("Calle Prueba")
                .precioEstimado(180000.0)
                .build();

        when(valuationService.obtenerValoracion("id123"))
                .thenReturn(response);

        mockMvc.perform(get("/api/valoraciones/id123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id123"))
                .andExpect(jsonPath("$.precioEstimado").value(180000.0));
    }

}
