package com.monolito.ecommerce.user.service;

import com.monolito.ecommerce.shared.exception.BusinessException;
import com.monolito.ecommerce.shared.exception.ResourceNotFoundException;
import com.monolito.ecommerce.user.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servicio de Usuarios
 * 
 * ALMACENAMIENTO EN MEMORIA:
 * - Usa ConcurrentHashMap para thread-safety
 * - AtomicLong para generación de IDs auto-incrementales
 * - Los datos se pierden al reiniciar (característica del monolito sin DB)
 * 
 * VENTAJAS DEL MONOLITO:
 * - Acceso directo a memoria compartida (latencia cero)
 * - No necesita serialización/deserialización como REST
 * - Transacciones en memoria pueden ser atómicas
 */
@Service
public class UserService {

    // Almacenamiento en memoria: Map<userId, User>
    private final Map<Long, User> userDatabase = new ConcurrentHashMap<>();
    
    // Generador de IDs auto-incrementales
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Registrar un nuevo usuario
     */
    public User registerUser(String username, String email, String password, String fullName) {
        // Validar que el username no exista
        boolean usernameExists = userDatabase.values().stream()
            .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        
        if (usernameExists) {
            throw new BusinessException("El nombre de usuario ya existe: " + username);
        }

        // Validar que el email no exista
        boolean emailExists = userDatabase.values().stream()
            .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        
        if (emailExists) {
            throw new BusinessException("El email ya está registrado: " + email);
        }

        // Crear usuario
        Long userId = idGenerator.getAndIncrement();
        User user = new User(userId, username, email, password, fullName);
        
        // Guardar en memoria
        userDatabase.put(userId, user);
        
        return user;
    }

    /**
     * Login de usuario (autenticación simple)
     */
    public User login(String username, String password) {
        User user = userDatabase.values().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (!user.getPassword().equals(password)) {
            throw new BusinessException("Credenciales inválidas");
        }

        return user;
    }

    /**
     * Obtener usuario por ID
     */
    public User getUserById(Long userId) {
        User user = userDatabase.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Usuario", userId);
        }
        return user;
    }

    /**
     * Listar todos los usuarios
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(userDatabase.values());
    }

    /**
     * Verificar si un usuario existe (útil para otros módulos)
     * COMUNICACIÓN INTERNA: llamada directa en memoria, no HTTP
     */
    public boolean userExists(Long userId) {
        return userDatabase.containsKey(userId);
    }
}
