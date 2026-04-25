package com.monolito.ecommerce.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monolito.ecommerce.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationStore store;

    public NotificationController(NotificationStore store) {
        this.store = store;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationEvent>>> getMisNotificaciones(
            Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        List<NotificationEvent> notificaciones = store.obtener(userId);
        return ResponseEntity.ok(ApiResponse.success(notificaciones));
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("No autenticado");
        }
        try {
            return Long.parseLong(String.valueOf(authentication.getPrincipal()));
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Token inválido");
        }
    }
}
