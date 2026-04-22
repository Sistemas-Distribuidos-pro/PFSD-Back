package com.monolito.ecommerce.product.service;

import com.monolito.ecommerce.product.model.Product;
import com.monolito.ecommerce.shared.exception.BusinessException;
import com.monolito.ecommerce.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Servicio de Productos
 * 
 * ALMACENAMIENTO EN MEMORIA:
 * - ConcurrentHashMap para catálogo de productos
 * - Control de inventario en memoria
 * 
 * COMUNICACIÓN INTERNA:
 * - Otros módulos (Cart, Order) llaman directamente a estos métodos
 * - NO hay llamadas HTTP ni serialización
 * - Latencia CERO entre módulos
 */
@Service
public class ProductService {

    // Almacenamiento en memoria: Map<productId, Product>
    private final Map<Long, Product> productDatabase = new ConcurrentHashMap<>();
    
    // Generador de IDs
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Crear un nuevo producto
     */
    public Product createProduct(String name, String description, BigDecimal price, Integer stock, String category) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor a cero");
        }

        if (stock < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }

        Long productId = idGenerator.getAndIncrement();
        Product product = new Product(productId, name, description, price, stock, category);
        
        productDatabase.put(productId, product);
        
        return product;
    }

    /**
     * Obtener producto por ID
     */
    public Product getProductById(Long productId) {
        Product product = productDatabase.get(productId);
        if (product == null) {
            throw new ResourceNotFoundException("Producto", productId);
        }
        return product;
    }

    /**
     * Listar todos los productos
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(productDatabase.values());
    }

    /**
     * Filtrar productos por categoría
     */
    public List<Product> getProductsByCategory(String category) {
        return productDatabase.values().stream()
            .filter(p -> p.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }

    /**
     * Actualizar stock de un producto
     * Método usado internamente por OrderService
     */
    public synchronized void updateStock(Long productId, Integer newStock) {
        Product product = getProductById(productId);
        
        if (newStock < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }
        
        product.setStock(newStock);
    }

    /**
     * Reducir stock (usado al crear órdenes)
     * SINCRONIZACIÓN: usa synchronized para evitar race conditions
     * En un monolito con DB usarías transacciones ACID
     */
    public synchronized void decreaseStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        
        if (product.getStock() < quantity) {
            throw new BusinessException(
                String.format("Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                    product.getName(), product.getStock(), quantity)
            );
        }
        
        product.setStock(product.getStock() - quantity);
    }

    /**
     * Incrementar stock (usado en rollback de transacciones)
     * SIMULACIÓN DE ROLLBACK: en un monolito real con DB esto sería automático
     */
    public synchronized void increaseStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        product.setStock(product.getStock() + quantity);
    }

    /**
     * Verificar disponibilidad de stock
     */
    public boolean hasStock(Long productId, Integer quantity) {
        Product product = productDatabase.get(productId);
        return product != null && product.getStock() >= quantity;
    }

    /**
     * Actualizar precio de producto
     */
    public Product updatePrice(Long productId, BigDecimal newPrice) {
        Product product = getProductById(productId);
        
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor a cero");
        }
        
        product.setPrice(newPrice);
        return product;
    }
}
