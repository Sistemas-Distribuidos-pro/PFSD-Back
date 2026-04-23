package com.monolito.ecommerce.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.anomaly.AlertEvent;
import com.monolito.ecommerce.config.KafkaConfig;

@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    private final NotificationProducer notificationProducer;

    public AlertConsumer(NotificationProducer notificationProducer) {
        this.notificationProducer = notificationProducer;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ALERTS, groupId = "alert-processor-group")
    public void consumir(AlertEvent alert) {
        log.info("Alerta recibida para userId={} orderId={}", alert.userId(), alert.orderId());

        NotificationEvent notification = new NotificationEvent(
                alert.orderId(),
                alert.userId(),
                alert.razones(),
                alert.detectedAt());

        notificationProducer.publish(notification);
    }
}
