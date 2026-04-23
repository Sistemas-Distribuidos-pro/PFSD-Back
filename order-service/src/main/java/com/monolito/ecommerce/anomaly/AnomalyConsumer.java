package com.monolito.ecommerce.anomaly;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.config.KafkaConfig;
import com.monolito.ecommerce.order.event.OrderEvent;

@Component
public class AnomalyConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnomalyConsumer.class);

    private final AlertProducer alertProducer;
    private final EmailNotificationService emailService;

    public AnomalyConsumer(AlertProducer alertProducer, EmailNotificationService emailService) {
        this.alertProducer = alertProducer;
        this.emailService = emailService;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ORDERS, groupId = "anomaly-detector-group")
    public void consumir(OrderEvent event) {
        log.info("Evento recibido: orderId={} userId={} total={}",
                event.orderId(), event.userId(), event.total());

        List<String> razones = AnomalyDetector.detectar(event);

        if (razones.isEmpty()) {
            log.info("Orden #{} sin anomalías", event.orderId());
            return;
        }

        log.warn("Anomalía detectada en orden #{}: {}", event.orderId(), razones);

        AlertEvent alerta = new AlertEvent(
                event.orderId(),
                event.userId(),
                event.total(),
                razones,
                LocalDateTime.now());

        alertProducer.publish(alerta);
        emailService.enviarAlerta(alerta);
    }
}
