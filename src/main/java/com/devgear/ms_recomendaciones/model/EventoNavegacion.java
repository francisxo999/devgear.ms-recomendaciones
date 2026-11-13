package com.devgear.ms_recomendaciones.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Cada evento consumido de Kafka se guarda una sola vez (la PK es el eventId del mensaje),
 * así que si Kafka entrega un mensaje repetido (at-least-once) simplemente se ignora.
 * Las recomendaciones y métricas se calculan agregando esta tabla.
 */
@Entity
@Table(name = "eventos_navegacion", indexes = {
        @Index(name = "idx_evento_usuario", columnList = "usuarioId"),
        @Index(name = "idx_evento_procesado", columnList = "procesadoEn")
})
public class EventoNavegacion {

    @Id
    @Column(length = 64)
    private String eventId;

    @Column(nullable = false, length = 32)
    private String tipo;

    @Column(nullable = false, length = 64)
    private String usuarioId;

    private Long productoId;

    private String categoria;

    private Integer cantidad;

    private Instant ocurridoEn;

    @Column(nullable = false)
    private Instant procesadoEn;

    private Integer particion;

    @Column(name = "kafka_offset")
    private Long offsetKafka;

    public EventoNavegacion() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Instant getOcurridoEn() { return ocurridoEn; }
    public void setOcurridoEn(Instant ocurridoEn) { this.ocurridoEn = ocurridoEn; }

    public Instant getProcesadoEn() { return procesadoEn; }
    public void setProcesadoEn(Instant procesadoEn) { this.procesadoEn = procesadoEn; }

    public Integer getParticion() { return particion; }
    public void setParticion(Integer particion) { this.particion = particion; }

    public Long getOffsetKafka() { return offsetKafka; }
    public void setOffsetKafka(Long offsetKafka) { this.offsetKafka = offsetKafka; }
}
