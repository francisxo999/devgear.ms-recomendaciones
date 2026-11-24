package com.devgear.ms_recomendaciones.telemetria;

import com.devgear.ms_recomendaciones.kafka.NavegacionEvento;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tercer consumer group sobre navegacion.eventos (telemetria-recomendaciones-group): reenvía cada evento
 * nuevo al dashboard en tiempo real vía DynamoDB. Es independiente de recomendaciones-group, así que
 * una falla aquí nunca frena el cálculo de recomendaciones. Empieza en "latest": solo envía lo que ocurra
 * desde que arranca, sin reproducir el historial del tópico.
 */
@Component
public class TelemetriaListener {

    private static final Logger log = LoggerFactory.getLogger(TelemetriaListener.class);

    private final TelemetriaPublisher publisher;
    private final JsonMapper jsonMapper;

    public TelemetriaListener(TelemetriaPublisher publisher, JsonMapper jsonMapper) {
        this.publisher = publisher;
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(
            topics = "${devgear.kafka.topic-navegacion:navegacion.eventos}",
            groupId = "telemetria-recomendaciones-group",
            concurrency = "1",
            properties = "auto.offset.reset=latest")
    public void consumir(ConsumerRecord<String, String> registro) {
        try {
            NavegacionEvento e = jsonMapper.readValue(registro.value(), NavegacionEvento.class);
            if (e.eventId() == null || e.tipo() == null) {
                return;
            }
            publisher.publicar(e.eventId(), e.tipo(), e.usuarioId(), e.productoId(), e.categoria(), null);
        } catch (Exception ex) {
            log.debug("Mensaje omitido en telemetría {}-{}@{}: {}", registro.topic(), registro.partition(),
                    registro.offset(), ex.getMessage());
        }
    }
}
