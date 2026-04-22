package com.monolito.ecommerce.cart.model;

/**
 * DTO para agregar items al carrito
 */
public class AddToCartRequest {
    
    private Long userId;
    private Long productId;
    private Integer quantity;

    public AddToCartRequest() {
    }

    public AddToCartRequest(Long userId, Long productId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters y Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
