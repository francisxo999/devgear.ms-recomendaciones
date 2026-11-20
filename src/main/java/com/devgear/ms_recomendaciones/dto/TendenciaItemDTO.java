package com.devgear.ms_recomendaciones.dto;

import java.math.BigDecimal;

public record TendenciaItemDTO(
        Long productoId,
        String nombre,
        String categoria,
        BigDecimal precio,
        String imagenUrl,
        long interacciones
) {}
