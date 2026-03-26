package com.microecommerce.auth.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.microecommerce.auth.auth.model.VerifiedGoogleUser;
import com.microecommerce.auth.shared.exception.BusinessException;
import com.microecommerce.auth.shared.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierService(@Value("${auth.google.client-id}") String clientId,
            @Value("${auth.google.issuer}") String issuer) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .setIssuers(Collections.singletonList(issuer))
                .build();
    }

    public VerifiedGoogleUser verify(String idToken) {
        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new UnauthorizedException("Token de Google inválido");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            Object emailVerifiedClaim = payload.get("email_verified");
            boolean emailVerified = Boolean.TRUE.equals(emailVerifiedClaim)
                    || "true".equals(String.valueOf(emailVerifiedClaim));

            if (!emailVerified) {
                throw new UnauthorizedException("La cuenta de Google no tiene email verificado");
            }

            String subject = payload.getSubject();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");

            if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
                throw new BusinessException("El token de Google no contiene claims obligatorias");
            }

            if (fullName == null || fullName.isBlank()) {
                fullName = email;
            }

            return new VerifiedGoogleUser(subject, email, fullName);
        } catch (GeneralSecurityException | IOException ex) {
            throw new UnauthorizedException("No se pudo validar el token de Google");
        }
    }
}
