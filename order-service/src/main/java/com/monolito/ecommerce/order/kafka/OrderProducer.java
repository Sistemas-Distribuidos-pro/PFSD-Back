package com.monolito.ecommerce.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.config.KafkaConfig;
import com.monolito.ecommerce.order.event.OrderEvent;

@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC_ORDERS, String.valueOf(event.orderId()), event);
        log.info("Orden publicada en Kafka: orderId={} userId={} total={}",
                event.orderId(), event.userId(), event.total());
    }
}
