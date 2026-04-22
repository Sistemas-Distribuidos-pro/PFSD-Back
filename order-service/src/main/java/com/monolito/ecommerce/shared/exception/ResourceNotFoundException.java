package com.monolito.ecommerce.shared.exception;

/**
 * Excepción personalizada para recursos no encontrados
 * Se lanza cuando un usuario, producto u orden no existe en el almacenamiento en memoria
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s con ID %d no encontrado", resource, id));
    }
}
