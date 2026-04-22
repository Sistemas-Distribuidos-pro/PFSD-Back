package com.monolito.ecommerce.order.model;

/**
 * Estados de una orden
 */
public enum OrderStatus {
    PENDING,    // Pendiente de confirmación
    CONFIRMED,  // Confirmada y procesada
    CANCELLED   // Cancelada (rollback)
}
