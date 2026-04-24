package com.monolito.ecommerce.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.anomaly.AlertEvent;
import com.monolito.ecommerce.config.KafkaConfig;

@Component
public class AlertHistoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertHistoryConsumer.class);

    private final HistoryWriterService historyWriterService;

    public AlertHistoryConsumer(HistoryWriterService historyWriterService) {
        this.historyWriterService = historyWriterService;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ALERTS, groupId = "alert-history-group")
    public void consume(AlertEvent event) {
        log.info("Alert history event received for orderId={} userId={}", event.orderId(), event.userId());
        historyWriterService.writeAlertEvent(event);
    }
}
