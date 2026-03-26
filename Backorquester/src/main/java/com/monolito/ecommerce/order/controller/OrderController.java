package com.monolito.ecommerce.order.controller;

import com.monolito.ecommerce.order.model.CreateOrderRequest;
import com.monolito.ecommerce.order.model.Order;
import com.monolito.ecommerce.order.service.OrderService;
import com.monolito.ecommerce.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST de Órdenes
 *
 * ENDPOINTS:
 * POST /api/orders - Crear orden desde carrito
 * GET /api/orders/{id} - Obtener orden por ID
 * GET /api/orders/user/{userId} - Listar órdenes de un usuario
 * GET /api/orders - Listar todas las órdenes
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Crear orden desde el carrito
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        Order order = orderService.createOrder(authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Orden creada exitosamente", order));
    }

    /**
     * Obtener orden por ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * Listar órdenes de un usuario
     * GET /api/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByUser(
            @PathVariable Long userId,
            Authentication authentication) {
        Long authenticatedUserId = getAuthenticatedUserId(authentication);
        if (!authenticatedUserId.equals(userId)) {
            throw new AccessDeniedException("No tienes permisos para ver órdenes de otro usuario");
        }

        List<Order> orders = orderService.getOrdersByUser(authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Listar todas las órdenes
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("No autenticado");
        }

        try {
            return Long.parseLong(String.valueOf(authentication.getPrincipal()));
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Token inválido: subject no reconocido");
        }
    }
}
