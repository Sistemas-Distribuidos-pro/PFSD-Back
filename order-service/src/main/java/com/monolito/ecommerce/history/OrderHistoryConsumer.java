package com.monolito.ecommerce.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.monolito.ecommerce.config.KafkaConfig;
import com.monolito.ecommerce.order.event.OrderEvent;

@Component
public class OrderHistoryConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderHistoryConsumer.class);

    private final HistoryWriterService historyWriterService;

    public OrderHistoryConsumer(HistoryWriterService historyWriterService) {
        this.historyWriterService = historyWriterService;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ORDERS, groupId = "order-history-group")
    public void consume(OrderEvent event) {
        log.info("Order history event received for orderId={} userId={}", event.orderId(), event.userId());
        historyWriterService.writeOrderEvent(event);
    }
}
