package com.devgear.ms_recomendaciones.telemetria;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Escribe cada evento de telemetría en la tabla DynamoDB TelemetriaDevGear. El DynamoDB Stream de esa
 * tabla dispara la Lambda "streams", que lo envía por API Gateway WebSocket al dashboard.
 *
 * Es seguro para el flujo principal: la escritura va en un hilo aparte con una cola acotada y
 * cualquier falla solo se registra en el log; nunca afecta al consumidor de Kafka.
 * Viene DESACTIVADO (TELEMETRIA_HABILITADA=false) para que en local, sin credenciales de AWS, no haga nada.
 */
@Component
public class TelemetriaPublisher {

    private static final Logger log = LoggerFactory.getLogger(TelemetriaPublisher.class);
    private static final String ORIGEN = "recomendaciones";

    private final boolean habilitada;
    private final String tabla;
    private final String region;
    private final ThreadPoolExecutor cola;
    private volatile DynamoDbClient cliente;
    private volatile long ultimoAviso;

    public TelemetriaPublisher(@Value("${TELEMETRIA_HABILITADA:false}") boolean habilitada,
                               @Value("${TELEMETRIA_TABLA:TelemetriaDevGear}") String tabla,
                               @Value("${AWS_REGION:us-east-1}") String region) {
        this.habilitada = habilitada;
        this.tabla = tabla;
        this.region = region;
        this.cola = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2000),
                r -> {
                    Thread hilo = new Thread(r, "telemetria-dynamodb");
                    hilo.setDaemon(true);
                    return hilo;
                },
                new ThreadPoolExecutor.DiscardPolicy());
        log.info("Telemetría a DynamoDB {} (tabla {}, región {})", habilitada ? "ACTIVADA" : "desactivada", tabla, region);
    }

    /** Encola un evento. Si la telemetría está desactivada no hace nada. */
    public void publicar(String id, String tipo, String clienteId, Long productoId, String categoria, Long monto) {
        if (!habilitada) {
            return;
        }
        cola.execute(() -> escribir(id, tipo, clienteId, productoId, categoria, monto));
    }

    private void escribir(String id, String tipo, String clienteId, Long productoId, String categoria, Long monto) {
        try {
            Instant ahora = Instant.now();
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", texto(id));
            item.put("origen", texto(ORIGEN));
            item.put("tipo", texto(tipo));
            item.put("timestamp", numero(ahora.toEpochMilli()));
            item.put("ttl", numero(ahora.plusSeconds(86_400).getEpochSecond())); // DynamoDB borra a las 24 h
            if (clienteId != null) {
                item.put("clienteId", texto(clienteId));
            }
            if (productoId != null) {
                item.put("productoId", numero(productoId));
            }
            if (categoria != null) {
                item.put("categoria", texto(categoria));
            }
            if (monto != null) {
                item.put("monto", numero(monto));
            }
            cliente().putItem(PutItemRequest.builder().tableName(tabla).item(item).build());
        } catch (Exception e) {
            long t = System.currentTimeMillis();
            if (t - ultimoAviso > 30_000) {
                ultimoAviso = t;
                log.warn("No se pudo escribir telemetría en DynamoDB: {}", e.getMessage());
            }
        }
    }

    private DynamoDbClient cliente() {
        DynamoDbClient c = cliente;
        if (c == null) {
            synchronized (this) {
                if (cliente == null) {
                    cliente = DynamoDbClient.builder()
                            .region(Region.of(region))
                            .overrideConfiguration(ClientOverrideConfiguration.builder()
                                    .apiCallTimeout(Duration.ofSeconds(3))
                                    .build())
                            .build();
                }
                c = cliente;
            }
        }
        return c;
    }

    private static AttributeValue texto(String v) {
        return AttributeValue.builder().s(v).build();
    }

    private static AttributeValue numero(long v) {
        return AttributeValue.builder().n(Long.toString(v)).build();
    }

    @PreDestroy
    void cerrar() {
        cola.shutdownNow();
        DynamoDbClient c = cliente;
        if (c != null) {
            c.close();
        }
    }
}
