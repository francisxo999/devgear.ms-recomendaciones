package com.devgear.ms_recomendaciones.dto;

import java.util.List;

public record RecomendacionesResponseDTO(
        List<String> categoriasFavoritas,
        List<RecomendacionDTO> recomendaciones
) {}
