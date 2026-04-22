# catalog-service

Servicio de catálogo y stock del backend PFSD. Corre en `8083` y expone productos, inventario y actualización de stock.

## Responsabilidad

- Gestionar productos en memoria.
- Exponer consultas y CRUD básico de catálogo.
- Permitir al `order-service` validar y ajustar stock por HTTP.

## Puerto y variables

- Puerto: `CATALOG_SERVICE_PORT=8083`
- JWT compartido: `AUTH_JWT_SECRET`
- Expiración: `AUTH_JWT_EXPIRATION_SECONDS`
- CORS compartido: `CORS_ALLOWED_ORIGINS`

## Endpoints

- `POST /api/products`
- `GET /api/products`
- `GET /api/products/{id}`
- `GET /api/products/category/{category}`
- `PUT /api/products/{id}/stock`
- `PUT /api/products/{id}/price`
- `GET /health`

## Contrato importante

`order-service` consulta este servicio para validar stock antes de confirmar una orden y para descontar o revertir inventario durante el checkout sincrono.