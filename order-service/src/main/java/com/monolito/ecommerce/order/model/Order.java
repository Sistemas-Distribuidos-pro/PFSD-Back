package com.monolito.ecommerce.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de Orden
 * 
 * Representa una orden de compra confirmada
 */
public class Order {
    
    private Long id;
    private Long userId;
    private List<OrderItem> items;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    public Order(Long id, Long userId, List<OrderItem> items, BigDecimal total) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.status = OrderStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", total=" + total +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
