package com.monolito.ecommerce.integration.auth;

import com.monolito.ecommerce.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthClient {

    private final RestTemplate restTemplate;
    private final String authBaseUrl;

    public AuthClient(
            RestTemplate restTemplate,
            @Value("${services.auth.base-url:http://localhost:8082/api/users}") String authBaseUrl) {
        this.restTemplate = restTemplate;
        this.authBaseUrl = authBaseUrl;
    }

    public boolean userExists(Long userId) {
        try {
            restTemplate.getForEntity(authBaseUrl + "/{id}", String.class, userId);
            return true;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw new BusinessException("Error validando usuario contra auth-service");
        } catch (Exception ex) {
            throw new BusinessException("No se pudo conectar con auth-service");
        }
    }
}
