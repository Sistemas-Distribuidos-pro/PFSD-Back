package com.monolito.ecommerce.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.config.KafkaConfig;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationStore store;

    public NotificationConsumer(NotificationStore store) {
        this.store = store;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_NOTIFICATIONS, groupId = "notification-store-group")
    public void consumir(NotificationEvent event) {
        log.info("Notificación almacenada: orderId={} userId={}",
                event.orderId(), event.userId());
        store.agregar(event);
    }
}
