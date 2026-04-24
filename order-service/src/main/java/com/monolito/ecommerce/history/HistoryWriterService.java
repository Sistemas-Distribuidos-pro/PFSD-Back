package com.monolito.ecommerce.history;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monolito.ecommerce.anomaly.AlertEvent;
import com.monolito.ecommerce.order.event.OrderEvent;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class HistoryWriterService {

    private static final Logger log = LoggerFactory.getLogger(HistoryWriterService.class);
    private static final String EVENT_TYPE_ORDER = "ORDER";
    private static final String EVENT_TYPE_ALERT = "ALERT";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmssSSS'Z'")
            .withZone(ZoneOffset.UTC);

    private final ObjectMapper objectMapper;
    private final HistoryS3Properties properties;
    private final ObjectProvider<S3Client> s3ClientProvider;

    public HistoryWriterService(
            ObjectMapper objectMapper,
            HistoryS3Properties properties,
            ObjectProvider<S3Client> s3ClientProvider) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.s3ClientProvider = s3ClientProvider;
    }

    public void writeOrderEvent(OrderEvent event) {
        Instant storedAt = Instant.now();
        OrderHistoryDocument document = new OrderHistoryDocument(
                EVENT_TYPE_ORDER,
                storedAt,
                event.orderId(),
                event.userId(),
                event.total(),
                event.itemCount(),
                event.createdAt());
        writeDocument("orders", "order", event.orderId(), storedAt, document);
    }

    public void writeAlertEvent(AlertEvent event) {
        Instant storedAt = Instant.now();
        AlertHistoryDocument document = new AlertHistoryDocument(
                EVENT_TYPE_ALERT,
                storedAt,
                event.orderId(),
                event.userId(),
                event.total(),
                event.razones(),
                event.detectedAt());
        writeDocument("alerts", "alert", event.orderId(), storedAt, document);
    }

    private void writeDocument(
            String prefix,
            String filePrefix,
            Long eventId,
            Instant storedAt,
            Object document) {
        if (!properties.isEnabled()) {
            log.debug("History persistence is disabled; skipping {} event for id={}", filePrefix, eventId);
            return;
        }

        if (properties.getBucketName() == null || properties.getBucketName().isBlank()) {
            log.warn("History bucket name is not configured; skipping {} event for id={}", filePrefix, eventId);
            return;
        }

        S3Client s3Client = s3ClientProvider.getIfAvailable();
        if (s3Client == null) {
            log.warn("S3 client is not available; skipping {} event for id={}", filePrefix, eventId);
            return;
        }

        String objectKey = buildObjectKey(prefix, filePrefix, storedAt, eventId);

        try {
            byte[] payload = objectMapper.writeValueAsBytes(document);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(objectKey)
                    .contentType(CONTENT_TYPE_JSON)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(payload));
            log.info("History stored in S3: bucket={} key={}", properties.getBucketName(), objectKey);
        } catch (Exception exception) {
            log.error("Failed to store {} event in S3 for id={}: {}",
                    filePrefix, eventId, exception.getMessage(), exception);
        }
    }

    private static String buildObjectKey(String prefix, String filePrefix, Instant storedAt, Long eventId) {
        LocalDate date = LocalDate.ofInstant(storedAt, ZoneOffset.UTC);
        return "%s/year=%04d/month=%02d/day=%02d/%s-%s-%s.json".formatted(
                prefix,
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                filePrefix,
                FILE_TIMESTAMP_FORMAT.format(storedAt),
                eventId);
    }

    private record OrderHistoryDocument(
            String eventType,
            Instant storedAt,
            Long orderId,
            Long userId,
            BigDecimal total,
            int itemCount,
            LocalDateTime createdAt) {
    }

    private record AlertHistoryDocument(
            String eventType,
            Instant storedAt,
            Long orderId,
            Long userId,
            BigDecimal total,
            @JsonProperty("razones") List<String> reasons,
            LocalDateTime detectedAt) {
    }
}
