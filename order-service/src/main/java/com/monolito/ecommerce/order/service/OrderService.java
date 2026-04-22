package com.monolito.ecommerce.order.service;

import com.monolito.ecommerce.cart.model.CartItem;
import com.monolito.ecommerce.cart.service.CartService;
import com.monolito.ecommerce.integration.catalog.CatalogClient;
import com.monolito.ecommerce.order.model.Order;
import com.monolito.ecommerce.order.model.OrderItem;
import com.monolito.ecommerce.order.model.OrderStatus;
import com.monolito.ecommerce.shared.exception.BusinessException;
import com.monolito.ecommerce.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de Órdenes
 *
 * TRANSACCIONES SIMULADAS:
 * - En un monolito con DB, usarías @Transactional para ACID
 * - Aquí simulamos rollback manual si algo falla
 * - Thread-safe con synchronized
 *
 * VENTAJAS DEL MONOLITO:
 * - Flujo en un solo proceso
 * - Acceso directo a ProductService, CartService, UserService
 * - NO hay latencia de red
 * - NO hay serialización/deserialización
 * - FÁCIL mantener consistencia (en este caso, manual)
 *
 * DESVENTAJAS:
 * - Si falla la app, se pierden los datos (no hay persistencia)
 * - Escalabilidad limitada a recursos de una máquina
 */
@Service
public class OrderService {

    // Almacenamiento en memoria
    private final Map<Long, Order> orderDatabase = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // Dependencias externas
    private final CartService cartService;
    private final CatalogClient catalogClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    public OrderService(CartService cartService, CatalogClient catalogClient) {
        this.cartService = cartService;
        this.catalogClient = catalogClient;
    }

    /**
     * Crear orden desde el carrito
     *
     * SIMULACIÓN DE TRANSACCIÓN:
     * 1. Validar usuario y carrito
     * 2. Validar stock de todos los productos
     * 3. Descontar inventario
     * 4. Si algo falla, hacer ROLLBACK manual
     * 5. Crear orden
     * 6. Limpiar carrito
     *
     * En un monolito real con BD:
     *
     * @Transactional haría esto automáticamente
     *                Si algo falla, la operación se revierte
     */
    public synchronized Order createOrder(Long userId) {
        // PASO 2: Obtener carrito
        List<CartItem> cartItems = cartService.getCart(userId);

        if (cartItems.isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }

        // PASO 3: Validar stock de TODOS los productos antes de procesar
        // Esto simula una validación pre-transaccional
        for (CartItem item : cartItems) {
            if (!catalogClient.hasStock(item.getProductId(), item.getQuantity())) {
                throw new BusinessException(
                        String.format("Stock insuficiente para %s", item.getProductName()));
            }
        }

        // PASO 4: Crear orden (todavía PENDING)
        Long orderId = idGenerator.getAndIncrement();

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> new OrderItem(
                        cartItem.getProductId(),
                        cartItem.getProductName(),
                        cartItem.getPrice(),
                        cartItem.getQuantity()))
                .toList();

        BigDecimal total = cartService.calculateTotal(userId);

        Order order = new Order(orderId, userId, orderItems, total);
        order.setStatus(OrderStatus.PENDING);

        // PASO 5: TRANSACCIÓN SIMULADA - Descontar inventario
        // Guardamos los cambios por si hay que hacer rollback
        Map<Long, Integer> stockChanges = new HashMap<>();

        try {
            for (OrderItem item : orderItems) {
                // Guardar cambio para posible rollback
                stockChanges.put(item.getProductId(), item.getQuantity());

                // Descontar stock (puede lanzar excepción)
                catalogClient.decreaseStock(item.getProductId(), item.getQuantity());
            }

            // Si llegamos aquí, la operación salió bien
            order.setStatus(OrderStatus.CONFIRMED);
            orderDatabase.put(orderId, order);

            // PASO 6: Limpiar carrito
            cartService.clearCart(userId);

        } catch (RuntimeException e) {
            // ROLLBACK MANUAL: Revertir todos los cambios de stock

            for (Map.Entry<Long, Integer> entry : stockChanges.entrySet()) {
                try {
                    catalogClient.increaseStock(entry.getKey(), entry.getValue());
                    LOGGER.info("Stock revertido para producto {}", entry.getKey());
                } catch (RuntimeException rollbackEx) {
                    LOGGER.error("Error en rollback para producto {}", entry.getKey(), rollbackEx);
                }
            }

            order.setStatus(OrderStatus.CANCELLED);

            // Nota académica: En un monolito con BD y @Transactional,
            // este rollback sería AUTOMÁTICO. Aquí lo hacemos manual
            // para demostrar el concepto.

            throw new BusinessException("No se pudo crear la orden: " + e.getMessage(), e);
        }

        return order;
    }

    /**
     * Obtener orden por ID
     */
    public Order getOrderById(Long orderId) {
        Order order = orderDatabase.get(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Orden", orderId);
        }
        return order;
    }

    /**
     * Listar órdenes de un usuario
     */
    public List<Order> getOrdersByUser(Long userId) {
        return orderDatabase.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .toList();
    }

    /**
     * Listar todas las órdenes
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(orderDatabase.values());
    }
}
