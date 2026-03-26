package com.microecommerce.auth.user.service;

import com.microecommerce.auth.shared.exception.BusinessException;
import com.microecommerce.auth.shared.exception.ResourceNotFoundException;
import com.microecommerce.auth.user.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, User> userDatabase = new ConcurrentHashMap<>();
    private final Map<String, Long> federatedIdentityIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public User registerUser(String username, String email, String password, String fullName) {
        boolean usernameExists = userDatabase.values().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));

        if (usernameExists) {
            throw new BusinessException("El nombre de usuario ya existe: " + username);
        }

        boolean emailExists = userDatabase.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

        if (emailExists) {
            throw new BusinessException("El email ya está registrado: " + email);
        }

        Long userId = idGenerator.getAndIncrement();
        User user = new User(userId, username, email, password, fullName);
        userDatabase.put(userId, user);
        return user;
    }

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

    public User getUserById(Long userId) {
        User user = userDatabase.get(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Usuario", userId);
        }
        return user;
    }

    public User findOrCreateFederatedUser(String provider, String externalSubject, String email, String fullName) {
        String identityKey = provider + ":" + externalSubject;
        Long existingUserId = federatedIdentityIndex.get(identityKey);
        if (existingUserId != null) {
            User existingUser = userDatabase.get(existingUserId);
            if (existingUser != null) {
                return existingUser;
            }
        }

        User byEmail = userDatabase.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (byEmail != null) {
            federatedIdentityIndex.put(identityKey, byEmail.getId());
            return byEmail;
        }

        Long userId = idGenerator.getAndIncrement();
        String username = generateUniqueUsernameFromEmail(email);
        User user = new User(userId, username, email, "", fullName);
        userDatabase.put(userId, user);
        federatedIdentityIndex.put(identityKey, userId);
        return user;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userDatabase.values());
    }

    private String generateUniqueUsernameFromEmail(String email) {
        String[] emailParts = email.split("@");
        String base = emailParts.length > 0 ? emailParts[0].trim().toLowerCase() : "user";
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (usernameExists(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean usernameExists(String username) {
        return userDatabase.values().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }
}
