package com.monolito.ecommerce.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderEvent(
        Long orderId,
        Long userId,
        BigDecimal total,
        int itemCount,
        LocalDateTime createdAt
        ) {

}
