package com.devgear.ms_recomendaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    /**
     * Si procesar un mensaje falla (por ejemplo la BD no responde), se reintenta 3 veces
     * con 1 s entre intentos. Si sigue fallando se omite ese mensaje y se continúa:
     * un mensaje "venenoso" no debe frenar toda la partición.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
    }
}
