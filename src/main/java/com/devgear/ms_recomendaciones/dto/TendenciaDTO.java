package com.devgear.ms_recomendaciones.dto;

import java.util.List;

public record TendenciaDTO(
        String ventana,
        List<TendenciaItemDTO> items
) {}
