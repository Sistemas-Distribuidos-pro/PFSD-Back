package com.monolito.ecommerce.anomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.config.KafkaConfig;

@Component
public class AlertProducer {

    private static final Logger log = LoggerFactory.getLogger(AlertProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AlertProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AlertEvent alert) {
        kafkaTemplate.send(KafkaConfig.TOPIC_ALERTS, String.valueOf(alert.orderId()), alert);
        log.warn("Alerta publicada: orderId={} userId={} razones={}",
                alert.orderId(), alert.userId(), alert.razones());
    }
}
