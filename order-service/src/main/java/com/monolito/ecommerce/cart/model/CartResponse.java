package com.monolito.ecommerce.cart.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta del carrito
 */
public class CartResponse {
    
    private Long userId;
    private List<CartItem> items;
    private Integer totalItems;
    private BigDecimal total;

    public CartResponse() {
    }

    public CartResponse(Long userId, List<CartItem> items, BigDecimal total) {
        this.userId = userId;
        this.items = items;
        this.totalItems = items.stream().mapToInt(CartItem::getQuantity).sum();
        this.total = total;
    }

    // Getters y Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
