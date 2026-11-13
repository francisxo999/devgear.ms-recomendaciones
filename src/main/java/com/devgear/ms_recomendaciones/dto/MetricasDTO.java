package com.devgear.ms_recomendaciones.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record MetricasDTO(
        long totalEventos,
        Map<String, Long> eventosPorTipo,
        long eventosUltimoMinuto,
        long usuariosActivos,
        List<Conteo> topCategorias,
        Map<Integer, Long> eventosPorParticion,
        Instant ultimoEvento
) {
    public record Conteo(String nombre, long total) {}
}
