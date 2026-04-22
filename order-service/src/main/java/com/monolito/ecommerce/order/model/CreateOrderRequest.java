package com.monolito.ecommerce.order.model;

/**
 * DTO para crear orden
 */
public class CreateOrderRequest {
    
    private Long userId;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long userId) {
        this.userId = userId;
    }

    // Getters y Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
