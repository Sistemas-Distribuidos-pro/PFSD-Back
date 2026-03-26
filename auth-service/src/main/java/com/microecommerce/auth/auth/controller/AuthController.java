package com.microecommerce.auth.auth.controller;

import com.microecommerce.auth.auth.model.ExchangeTokenRequest;
import com.microecommerce.auth.auth.model.ExchangeTokenResponse;
import com.microecommerce.auth.auth.service.FederatedAuthService;
import com.microecommerce.auth.shared.dto.ApiResponse;
import com.microecommerce.auth.shared.exception.BusinessException;
import com.microecommerce.auth.shared.exception.UnauthorizedException;
import com.microecommerce.auth.user.model.User;
import com.microecommerce.auth.user.model.UserResponse;
import com.microecommerce.auth.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final FederatedAuthService federatedAuthService;
    private final UserService userService;

    public AuthController(FederatedAuthService federatedAuthService, UserService userService) {
        this.federatedAuthService = federatedAuthService;
        this.userService = userService;
    }

    @PostMapping("/exchange")
    public ResponseEntity<ApiResponse<ExchangeTokenResponse>> exchangeGoogleToken(
            @RequestBody ExchangeTokenRequest request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().isBlank()) {
            throw new BusinessException("idToken es obligatorio");
        }

        if (request.getProvider() == null || request.getProvider().isBlank()) {
            throw new BusinessException("provider es obligatorio");
        }

        ExchangeTokenResponse response = federatedAuthService.exchangeGoogleToken(
                request.getProvider(),
                request.getIdToken());

        return ResponseEntity.ok(ApiResponse.success("Token intercambiado exitosamente", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Sesión no válida o expirada");
        }

        String subject = String.valueOf(authentication.getPrincipal());
        Long userId;
        try {
            userId = Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new UnauthorizedException("Token inválido: subject no reconocido");
        }

        User user = userService.getUserById(userId);
        UserResponse response = new UserResponse(user);
        return ResponseEntity.ok(ApiResponse.success("Sesión válida", response));
    }
}
