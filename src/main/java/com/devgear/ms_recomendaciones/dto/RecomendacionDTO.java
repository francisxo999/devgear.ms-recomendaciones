package com.devgear.ms_recomendaciones.dto;

import java.math.BigDecimal;

public record RecomendacionDTO(
        Long productoId,
        String nombre,
        String categoria,
        BigDecimal precio,
        String imagenUrl,
        String motivo
) {}
