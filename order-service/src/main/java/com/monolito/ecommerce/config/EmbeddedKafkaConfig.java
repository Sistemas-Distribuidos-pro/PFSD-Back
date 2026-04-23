package com.monolito.ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

@Configuration
@Profile("local")
public class EmbeddedKafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedKafkaConfig.class);

    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() throws Exception {
        log.info(">>> Arrancando Kafka embebido...");
        EmbeddedKafkaKraftBroker broker = new EmbeddedKafkaKraftBroker(
                1, 1, KafkaConfig.TOPIC_ORDERS, KafkaConfig.TOPIC_ALERTS);
        broker.kafkaPorts(9092);
        broker.afterPropertiesSet();
        log.info(">>> Kafka embebido listo en: {}", broker.getBrokersAsString());
        return broker;
    }
}
