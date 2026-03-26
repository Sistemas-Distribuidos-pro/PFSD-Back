package com.microecommerce.auth.auth.service;

import com.microecommerce.auth.auth.model.ExchangeTokenResponse;
import com.microecommerce.auth.auth.model.VerifiedGoogleUser;
import com.microecommerce.auth.shared.exception.BusinessException;
import com.microecommerce.auth.user.model.User;
import com.microecommerce.auth.user.model.UserResponse;
import com.microecommerce.auth.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class FederatedAuthService {

    private static final String SUPPORTED_PROVIDER = "google";

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    public FederatedAuthService(GoogleTokenVerifierService googleTokenVerifierService,
            JwtTokenService jwtTokenService,
            UserService userService) {
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    public ExchangeTokenResponse exchangeGoogleToken(String provider, String idToken) {
        if (provider == null || !SUPPORTED_PROVIDER.equalsIgnoreCase(provider.trim())) {
            throw new BusinessException("Proveedor no soportado. Use provider=google");
        }

        VerifiedGoogleUser verifiedGoogleUser = googleTokenVerifierService.verify(idToken);

        User user = userService.findOrCreateFederatedUser(
                SUPPORTED_PROVIDER,
                verifiedGoogleUser.getSubject(),
                verifiedGoogleUser.getEmail(),
                verifiedGoogleUser.getFullName());

        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                SUPPORTED_PROVIDER,
                verifiedGoogleUser.getSubject());

        return new ExchangeTokenResponse(
                accessToken,
                "Bearer",
                jwtTokenService.getExpirationSeconds(),
                new UserResponse(user));
    }
}
