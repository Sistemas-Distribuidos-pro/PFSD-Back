package com.monolito.ecommerce.notification;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationEvent(
        Long orderId,
        Long userId,
        List<String> razones,
        LocalDateTime detectedAt
        ) {

}
