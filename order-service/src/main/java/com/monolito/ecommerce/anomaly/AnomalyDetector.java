package com.monolito.ecommerce.anomaly;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.monolito.ecommerce.order.event.OrderEvent;

public class AnomalyDetector {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("500.00");
    private static final int HIGH_ITEM_COUNT_THRESHOLD = 10;

    // Funciones puras — building blocks
    static final Predicate<OrderEvent> esMontoAlto
            = event -> event.total().compareTo(HIGH_VALUE_THRESHOLD) > 0;

    static final Predicate<OrderEvent> esCantidadInusual
            = event -> event.itemCount() > HIGH_ITEM_COUNT_THRESHOLD;

    // Reglas: descripción → predicado puro
    private static final Map<String, Predicate<OrderEvent>> REGLAS = Map.of(
            "Monto alto (> $" + HIGH_VALUE_THRESHOLD + ")", esMontoAlto,
            "Cantidad inusual de ítems (> " + HIGH_ITEM_COUNT_THRESHOLD + ")", esCantidadInusual
    );

    // Función principal: evalúa todas las reglas y devuelve las que se cumplen
    public static List<String> detectar(OrderEvent event) {
        return REGLAS.entrySet().stream()
                .filter(regla -> regla.getValue().test(event))
                .map(Map.Entry::getKey)
                .toList();
    }

    // Función derivada — composición pura
    public static boolean esAnomalous(OrderEvent event) {
        return !detectar(event).isEmpty();
    }
}
