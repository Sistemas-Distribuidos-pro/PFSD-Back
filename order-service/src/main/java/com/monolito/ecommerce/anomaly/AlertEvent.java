package com.monolito.ecommerce.anomaly;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AlertEvent(
        Long orderId,
        Long userId,
        BigDecimal total,
        List<String> razones,
        LocalDateTime detectedAt
        ) {

}
