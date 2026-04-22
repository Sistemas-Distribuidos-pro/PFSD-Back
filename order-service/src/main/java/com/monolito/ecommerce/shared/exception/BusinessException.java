package com.monolito.ecommerce.shared.exception;

/**
 * Excepción para errores de negocio
 * Ejemplo: stock insuficiente, carrito vacío, credenciales inválidas
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
