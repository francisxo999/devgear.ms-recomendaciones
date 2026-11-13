package com.devgear.ms_recomendaciones.controller;

import com.devgear.ms_recomendaciones.dto.MetricasDTO;
import com.devgear.ms_recomendaciones.dto.RecomendacionesResponseDTO;
import com.devgear.ms_recomendaciones.service.RecomendacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recomendaciones")
public class RecomendacionController {

    private final RecomendacionService service;

    public RecomendacionController(RecomendacionService service) {
        this.service = service;
    }

    // Recomendaciones personalizadas según lo que el usuario vio y agregó al carrito.
    @GetMapping
    public ResponseEntity<RecomendacionesResponseDTO> misRecomendaciones(JwtAuthenticationToken auth) {
        String usuarioId = auth.getToken().getClaimAsString("oid");
        String bearer = "Bearer " + auth.getToken().getTokenValue();
        return ResponseEntity.ok(service.recomendar(usuarioId, bearer));
    }

    // Métricas del flujo de eventos para el dashboard del administrador.
    @GetMapping("/metricas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetricasDTO> metricas() {
        return ResponseEntity.ok(service.metricas());
    }
}
