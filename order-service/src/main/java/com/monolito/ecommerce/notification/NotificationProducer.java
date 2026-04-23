package com.monolito.ecommerce.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.config.KafkaConfig;

@Component
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(NotificationEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC_NOTIFICATIONS,
                String.valueOf(event.userId()), event);
        log.info("Notificación publicada: orderId={} userId={}",
                event.orderId(), event.userId());
    }
}
