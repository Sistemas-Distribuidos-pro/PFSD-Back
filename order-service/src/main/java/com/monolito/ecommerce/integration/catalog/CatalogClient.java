package com.monolito.ecommerce.integration.catalog;

import com.monolito.ecommerce.integration.dto.ApiResponse;
import com.monolito.ecommerce.shared.exception.BusinessException;
import com.monolito.ecommerce.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;
    private final String catalogBaseUrl;

    public CatalogClient(
            RestTemplate restTemplate,
            @Value("${services.catalog.base-url:http://localhost:8083/api/products}") String catalogBaseUrl) {
        this.restTemplate = restTemplate;
        this.catalogBaseUrl = catalogBaseUrl;
    }

    public ProductSnapshot getProductById(Long productId) {
        try {
            ResponseEntity<ApiResponse<ProductSnapshot>> response = restTemplate.exchange(
                    catalogBaseUrl + "/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<ProductSnapshot>>() {
                    },
                    productId);

            ApiResponse<ProductSnapshot> body = response.getBody();
            if (body == null || !body.isSuccess() || body.getData() == null) {
                throw new BusinessException("Respuesta inválida de catalog-service");
            }

            return body.getData();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Producto", productId);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("No se pudo conectar con catalog-service");
        }
    }

    public boolean hasStock(Long productId, Integer quantity) {
        ProductSnapshot product = getProductById(productId);
        return product.getStock() != null && product.getStock() >= quantity;
    }

    public void decreaseStock(Long productId, Integer quantity) {
        ProductSnapshot product = getProductById(productId);
        Integer newStock = product.getStock() - quantity;

        if (newStock < 0) {
            throw new BusinessException(
                    String.format("Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                            product.getName(), product.getStock(), quantity));
        }

        updateStock(productId, newStock);
    }

    public void increaseStock(Long productId, Integer quantity) {
        ProductSnapshot product = getProductById(productId);
        Integer newStock = product.getStock() + quantity;
        updateStock(productId, newStock);
    }

    private void updateStock(Long productId, Integer stock) {
        try {
            URI uri = URI.create(catalogBaseUrl + "/" + productId + "/stock?stock=" + stock);
            RequestEntity<Void> request = new RequestEntity<>(HttpMethod.PUT, uri);
            restTemplate.exchange(request, String.class);
        } catch (Exception ex) {
            throw new BusinessException("No se pudo actualizar stock en catalog-service");
        }
    }
}
