package com.devgear.ms_recomendaciones.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Solo el "content" de la página que devuelve ms-productos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductosPageDTO(List<ProductoDTO> content) {}
