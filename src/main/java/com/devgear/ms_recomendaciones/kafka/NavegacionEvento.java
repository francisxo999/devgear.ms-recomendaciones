package com.devgear.ms_recomendaciones.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Mismo contrato que publican ms-productos y ms-carrito en "navegacion.eventos". */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NavegacionEvento(
        String eventId,
        String tipo,
        String usuarioId,
        Long productoId,
        String categoria,
        Integer cantidad,
        String timestamp
) {}
