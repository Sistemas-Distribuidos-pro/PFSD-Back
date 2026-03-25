package com.monolito.ecommerce.cart.controller;

import com.monolito.ecommerce.cart.model.AddToCartRequest;
import com.monolito.ecommerce.cart.model.CartItem;
import com.monolito.ecommerce.cart.model.CartResponse;
import com.monolito.ecommerce.cart.service.CartService;
import com.monolito.ecommerce.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST de Carritom
 * 
 * ENDPOINTS:
 * POST   /api/cart/add          - Agregar producto al carrito
 * GET    /api/cart/{userId}     - Obtener carrito del usuario
 * DELETE /api/cart/{userId}     - Vaciar carrito
 * DELETE /api/cart/{userId}/item/{productId} - Eliminar item del carrito
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Agregar producto al carrito
     * POST /api/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItem>> addToCart(@RequestBody AddToCartRequest request) {
        CartItem item = cartService.addToCart(
            request.getUserId(),
            request.getProductId(),
            request.getQuantity()
        );

        return ResponseEntity.ok(ApiResponse.success("Producto agregado al carrito", item));
    }

    /**
     * Obtener carrito del usuario
     * GET /api/cart/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        List<CartItem> items = cartService.getCart(userId);
        BigDecimal total = cartService.calculateTotal(userId);
        
        CartResponse response = new CartResponse(userId, items, total);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Vaciar carrito
     * DELETE /api/cart/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Carrito vaciado", null));
    }

    /**
     * Eliminar item específico del carrito
     * DELETE /api/cart/{userId}/item/{productId}
     */
    @DeleteMapping("/{userId}/item/{productId}")
    public ResponseEntity<ApiResponse<String>> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Producto eliminado del carrito", null));
    }
}
