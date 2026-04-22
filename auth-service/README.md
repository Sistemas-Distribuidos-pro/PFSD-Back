# auth-service

Servicio de autenticación del backend PFSD. Corre en `8082` y convierte el token de Google en un JWT propio de la aplicación.

## Responsabilidad

- Verificar el `idToken` de Google.
- Crear o reutilizar el usuario federado en memoria.
- Emitir JWT con `subject = userId`.
- Exponer la información del usuario autenticado con `/api/auth/me`.

## Puerto y variables

- Puerto: `AUTH_SERVICE_PORT=8082`
- JWT compartido: `AUTH_JWT_SECRET`
- Expiración: `AUTH_JWT_EXPIRATION_SECONDS`
- CORS compartido: `CORS_ALLOWED_ORIGINS`
- Google client id: `GOOGLE_CLIENT_ID`

## Endpoints

- `POST /api/auth/exchange`
- `GET /api/auth/me`
- `POST /api/users/register`
- `POST /api/users/login`
- `GET /api/users`
- `GET /api/users/{id}`
- `GET /health`

## Contrato importante

El JWT emitido por este servicio usa el `userId` interno como `subject`. `order-service` depende de eso para asociar carrito y órdenes al usuario correcto.