package com.monolito.ecommerce.order.model;

/**
 * DTO  para crear orden.
 *
 * El usuario efectivo se obtiene del JWT. El campo userId se mantiene
 * solo para compatibilidad con clientes anteriores y se valida contra
 * el sujeto autenticado.
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
