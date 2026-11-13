package com.devgear.ms_recomendaciones.kafka;

import com.devgear.ms_recomendaciones.model.EventoNavegacion;
import com.devgear.ms_recomendaciones.repository.EventoNavegacionRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/**
 * Consumidor del tópico navegacion.eventos. Todas las instancias comparten el mismo
 * group-id (recomendaciones-group): Kafka reparte las 3 particiones entre ellas.
 * Con concurrency=3 una sola instancia ya abre 3 hilos, uno por partición.
 */
@Component
public class NavegacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NavegacionConsumer.class);

    private final EventoNavegacionRepository repository;
    private final JsonMapper jsonMapper;

    public NavegacionConsumer(EventoNavegacionRepository repository, JsonMapper jsonMapper) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(
            topics = "${devgear.kafka.topic-navegacion:navegacion.eventos}",
            concurrency = "${devgear.kafka.concurrency:3}")
    public void consumir(ConsumerRecord<String, String> registro) {
        NavegacionEvento evento;
        try {
            evento = jsonMapper.readValue(registro.value(), NavegacionEvento.class);
        } catch (Exception e) {
            // Mensaje mal formado: no tiene sentido reintentarlo, se omite.
            log.warn("Mensaje inválido en {}-{}@{}: {}", registro.topic(), registro.partition(),
                    registro.offset(), e.getMessage());
            return;
        }
        if (evento.eventId() == null || evento.usuarioId() == null || evento.tipo() == null) {
            log.warn("Evento incompleto en {}-{}@{}, se omite", registro.topic(), registro.partition(), registro.offset());
            return;
        }

        // Idempotencia: Kafka garantiza "al menos una vez", así que un evento puede llegar repetido.
        if (repository.existsById(evento.eventId())) {
            log.debug("Evento {} ya procesado, se ignora", evento.eventId());
            return;
        }

        EventoNavegacion fila = new EventoNavegacion();
        fila.setEventId(evento.eventId());
        fila.setTipo(evento.tipo());
        fila.setUsuarioId(evento.usuarioId());
        fila.setProductoId(evento.productoId());
        fila.setCategoria(evento.categoria());
        fila.setCantidad(evento.cantidad());
        fila.setOcurridoEn(evento.timestamp() != null ? Instant.parse(evento.timestamp()) : null);
        fila.setProcesadoEn(Instant.now());
        fila.setParticion(registro.partition());
        fila.setOffsetKafka(registro.offset());
        repository.save(fila);

        log.info("{} usuario={} producto={} categoria={} <- {}-{}@{}", evento.tipo(), evento.usuarioId(),
                evento.productoId(), evento.categoria(), registro.topic(), registro.partition(), registro.offset());
    }
}
