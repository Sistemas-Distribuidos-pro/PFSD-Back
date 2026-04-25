# order-service

Servicio de órdenes y checkout sincrono del backend PFSD. Vive en `8081` y centraliza carrito, creación de orden y cierre de compra.

## Responsabilidad

- Mantiene el carrito en memoria por usuario.
- Valida stock contra `catalog-service` antes de confirmar una orden.
- Usa el JWT emitido por `auth-service` para identificar al usuario autenticado.
- Hace rollback manual si falla el descuento de inventario.

## Puerto y configuración

- Puerto principal: `ORDER_SERVICE_PORT=8081`
- Alias temporal: `BACKEND_PORT=8081` y `BACKORQUESTER_PORT=8081`
- Base URL de auth: `AUTH_SERVICE_PORT=8082` o `AUTH_SERVICE_BASE_URL`
- Base URL de catálogo: `CATALOG_SERVICE_PORT=8083` o `CATALOG_SERVICE_BASE_URL`

## Endpoints

- `POST /api/cart/add`
- `GET /api/cart/{userId}`
- `DELETE /api/cart/{userId}`
- `DELETE /api/cart/{userId}/item/{productId}`
- `POST /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders/user/{userId}`
- `GET /health`

## Flujo de checkout

1. El cliente envía `Authorization: Bearer <JWT>`.
2. El servicio lee el `subject` del JWT como `userId`.
3. Se obtiene el carrito del usuario.
4. Se valida stock en `catalog-service`.
5. Se descuenta inventario y se persiste la orden en memoria.
6. Se limpia el carrito.

## History persistence in S3

- `orders` and `alerts` are persisted as JSON objects in Amazon S3 for batch analytics.
- `notifications` are intentionally excluded from this history layer.
- `OrderHistoryConsumer` listens to `orders` and `AlertHistoryConsumer` listens to `alerts`.
- Objects are written under `orders/year=YYYY/month=MM/day=DD/` and `alerts/year=YYYY/month=MM/day=DD/`.
- History persistence is best-effort and does not block checkout or anomaly processing if S3 is unavailable.

### Configuration

- `history.s3.enabled` controls whether history persistence is active.
- `history.s3.bucket-name` sets the destination bucket.
- `history.s3.region` sets the AWS region used by the S3 client.
