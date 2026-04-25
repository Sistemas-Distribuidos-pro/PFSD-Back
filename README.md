# PFSD Backend

Base backend del proyecto PFSD antes de introducir eventos. El sistema ya está dividido en tres servicios Spring Boot que cooperan por HTTP:

- `order-service` en `8081`
- `auth-service` en `8082`
- `catalog-service` en `8083`

No hay Kafka, Spark ni una capa event-driven todavía. El flujo actual es sincrono: el cliente obtiene un JWT en `auth-service`, consume catálogo en `catalog-service` y completa carrito/checkout en `order-service`.

## Arquitectura actual

`auth-service` valida el token de Google y emite el JWT de la aplicación. `catalog-service` expone el dominio de productos. `order-service` centraliza carrito, órdenes y checkout usando ese JWT para identificar al usuario.

### Flujo sincrono de órdenes

1. El frontend inicia sesión con Google y llama a `POST /api/auth/exchange`.
2. `auth-service` devuelve un JWT propio con `subject = userId`.
3. El frontend agrega items al carrito con `POST /api/cart/add` en `order-service`.
4. El checkout se completa con `POST /api/orders` en `order-service`.
5. `order-service` valida stock contra `catalog-service`, descuenta inventario y limpia el carrito.

### Historial para batch analytics

El backend también persiste historial de eventos en Amazon S3 para análisis batch sin alterar el flujo online.

- Se guardan únicamente los eventos de `orders` y `alerts`.
- `notifications` no se persisten en S3.
- La escritura ocurre desde consumidores Kafka dedicados, como efecto secundario.
- Los objetos se organizan por prefijo y fecha UTC en S3.

## Documentación por servicio

- [order-service/README.md](order-service/README.md)
- [auth-service/README.md](auth-service/README.md)
- [catalog-service/README.md](catalog-service/README.md)

## Variables de entorno

Usa [`.env.example`](.env.example) como referencia. Los nombres viejos se mantienen como alias temporales cuando hace falta, pero el nombre preferido para el servicio de 8081 es `ORDER_SERVICE_PORT`.

## Cómo levantarlo

No existe un `pom.xml` raíz. Cada servicio se compila y ejecuta desde su carpeta:

```bash
cd auth-service && mvn spring-boot:run
cd catalog-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
```

Para empaquetar:

```bash
cd auth-service && mvn clean package
cd catalog-service && mvn clean package
cd order-service && mvn clean package
```

## Endpoints clave

- `auth-service`:
  - `POST /api/auth/exchange`
  - `GET /api/auth/me`
  - `POST /api/users/register`
  - `POST /api/users/login`
  - `GET /api/users`
- `catalog-service`:
  - `POST /api/products`
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `PUT /api/products/{id}/stock`
- `order-service`:
  - `POST /api/cart/add`
  - `GET /api/cart/{userId}`
  - `DELETE /api/cart/{userId}`
  - `POST /api/orders`
  - `GET /api/orders/{id}`
  - `GET /api/orders/user/{userId}`

## Notas de compatibilidad

- `BACKORQUESTER_PORT` puede seguir funcionando temporalmente como alias del puerto de `order-service`.
- El checkout no necesita un body con `userId`; usa el `subject` del JWT.
- La separación entre servicios es real, pero el flujo sigue siendo sincrono y transaccional a nivel de aplicación.

### Requisitos Previos

- JDK 17 o superior
- Maven 3.6+

### Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

---

## 📝 Ejemplo de Flujo Completo

### 1. Registrar Usuario

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "email": "juan@example.com",
    "password": "1234",
    "fullName": "Juan Pérez"
  }'
```

### 2. Crear Productos

```bash
# Producto 1
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell",
    "description": "Laptop profesional",
    "price": 1200.00,
    "stock": 10,
    "category": "Electrónica"
  }'

# Producto 2
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mouse Logitech",
    "description": "Mouse inalámbrico",
    "price": 25.00,
    "stock": 50,
    "category": "Accesorios"
  }'
```

### 3. Agregar al Carrito

```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 0,
    "productId": 0,
    "quantity": 1
  }'

curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 0,
    "productId": 1,
    "quantity": 2
  }'
```

### 4. Ver Carrito

```bash
curl http://localhost:8080/api/cart/0
```

### 5. Crear Orden

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 0
  }'
```

---

## ✅ VENTAJAS del Monolito

### 1. **Simplicidad**
- Un solo proyecto, un solo repositorio
- Fácil de entender y navegar
- Curva de aprendizaje baja

### 2. **Latencia Cero Entre Módulos**
- Comunicación directa en memoria
- No hay overhead de red
- No hay serialización/deserialización

### 3. **Despliegue Sencillo**
- Un solo artefacto `.jar`
- No necesitas orquestación (Kubernetes, Docker Swarm)
- Ideal para desarrollo y testing

### 4. **Fácil Debugging**
- Todo el código está en un solo stacktrace
- Puedes hacer debug de principio a fin
- No hay "cajas negras" distribuidas

### 5. **Transacciones Reales (con DB)**
- Con base de datos: ACID garantizado
- Rollbacks automáticos
- Consistencia fuerte

### 6. **Testing E2E Simplificado**
- Puedes probar todo el flujo en un solo proceso
- No necesitas levantar múltiples servicios

---

## ⚠️ DESVENTAJAS del Monolito

### 1. **Escalabilidad Limitada**
- Solo escala verticalmente (más CPU/RAM a UNA máquina)
- No puedes escalar módulos independientemente
- Por ejemplo: Si "Order" necesita más recursos, tienes que escalar TODO

### 2. **Sin Persistencia (en este demo)**
- Los datos se pierden al reiniciar
- No apto para producción sin DB

### 3. **Single Point of Failure**
- Si el proceso falla, TODO cae
- No hay redundancia

### 4. **Acoplamiento Temporal**
- Todos los módulos se despliegan juntos
- Un bug en "Cart" requiere redesplegar "User", "Product", "Order"

### 5. **Límite de Tecnologías**
- Todo debe usar el mismo stack (Java/Spring)
- No puedes usar Node.js para "Cart" y Python para "Order"

---

## 🚫 ¿Qué NO es este Monolito?

### ❌ Monolito Distribuido (Anti-patrón)

Un **monolito distribuido** es el PEOR escenario:

```
┌─────────┐  HTTP   ┌─────────┐  HTTP   ┌─────────┐
│  User   ├────────►│ Product ├────────►│  Order  │
│ Service │         │ Service │         │ Service │
└─────────┘         └─────────┘         └─────────┘
     │                    │                    │
     ▼                    ▼                    ▼
 Shared DB          Shared DB          Shared DB
```

**Problemas**:
- ❌ Tiene las DESVENTAJAS de microservicios (complejidad, latencia)
- ❌ NO tiene las VENTAJAS de microservicios (escalado independiente)
- ❌ Acoplamiento por base de datos compartida
- ❌ Acoplamiento por HTTP (si User cae, Product no funciona)

**Este proyecto NO es esto**. Todo vive en el mismo proceso.

---

## 🔄 ¿Cuándo Migrar a Microservicios?

### Señales de que necesitas microservicios:

1. ✅ Tu equipo crece (>50 desarrolladores)
2. ✅ Necesitas escalar módulos independientemente
3. ✅ Quieres usar diferentes tecnologías por módulo
4. ✅ Tienes recursos para gestionar complejidad distribuida
5. ✅ Tu tráfico justifica la infraestructura extra

### Señales de que el monolito es suficiente:

1. ✅ Equipo pequeño (2-10 personas)
2. ✅ MVP o proyecto académico
3. ✅ Tráfico moderado
4. ✅ Simplicidad > Escalabilidad
5. ✅ Budget limitado para infraestructura

---

## 🎓 Evolución Posible

### De Monolito a Microservicios

Si este proyecto creciera, podrías extraer módulos:

```
MONOLITO ACTUAL:
┌──────────────────────────────┐
│  User + Product + Cart + Order │
└──────────────────────────────┘

MICROSERVICIOS FUTUROS:
┌──────┐   ┌─────────┐   ┌──────┐   ┌───────┐
│ User │   │ Product │   │ Cart │   │ Order │
└──────┘   └─────────┘   └──────┘   └───────┘
```

**Cambios necesarios**:
1. Cada servicio tendría su propia base de datos
2. Comunicación por HTTP/gRPC en vez de llamadas directas
3. Gestión de transacciones distribuidas (Saga pattern)
4. Service Discovery (Eureka, Consul)
5. API Gateway (Spring Cloud Gateway)

---

## 📚 Conceptos Clave Aprendidos

- ✅ Qué es un monolito y qué NO lo es
- ✅ Diferencia entre monolito modular y caótico
- ✅ Comunicación en memoria vs HTTP
- ✅ Simulación de transacciones sin base de datos
- ✅ Ventajas/desventajas de arquitecturas monolíticas
- ✅ Cuándo conviene monolito vs microservicios
- ✅ Thread-safety con `ConcurrentHashMap`
- ✅ Arquitectura en capas (Controller, Service, Model)

---

## 📂 Estructura del Proyecto

```
src/main/java/com/monolito/ecommerce/
│
├── EcommerceMonolitoApplication.java    # Punto de entrada
│
├── user/
│   ├── controller/
│   │   └── UserController.java
│   ├── service/
│   │   └── UserService.java
│   └── model/
│       ├── User.java
│       ├── RegisterRequest.java
│       ├── LoginRequest.java
│       └── UserResponse.java
│
├── product/
│   ├── controller/
│   │   └── ProductController.java
│   ├── service/
│   │   └── ProductService.java
│   └── model/
│       ├── Product.java
│       └── ProductRequest.java
│
├── cart/
│   ├── controller/
│   │   └── CartController.java
│   ├── service/
│   │   └── CartService.java
│   └── model/
│       ├── CartItem.java
│       ├── CartResponse.java
│       └── AddToCartRequest.java
│
├── order/
│   ├── controller/
│   │   └── OrderController.java
│   ├── service/
│   │   └── OrderService.java
│   └── model/
│       ├── Order.java
│       ├── OrderItem.java
│       ├── OrderStatus.java
│       └── CreateOrderRequest.java
│
└── shared/
    ├── exception/
    │   ├── ResourceNotFoundException.java
    │   ├── BusinessException.java
    │   └── GlobalExceptionHandler.java
    └── dto/
        ├── ApiResponse.java
        └── ErrorResponse.java
```

---

## 👨‍💻 Autor

Sistema desarrollado con propósitos académicos para demostrar fundamentos de arquitectura monolítica modular.

---

## 📄 Licencia

Proyecto académico - Uso educativo

---

## 🎯 Conclusión

Este proyecto demuestra que:

1. ✅ Los **monolitos bien estructurados** son válidos y efectivos
2. ✅ NO toda aplicación necesita ser microservicios
3. ✅ La arquitectura debe elegirse según necesidades REALES
4. ✅ Los monolitos son ideales para:
   - Proyectos pequeños/medianos
   - Equipos reducidos
   - MVPs
   - Aprendizaje

**Recuerda**: No hay arquitectura perfecta, solo arquitectura apropiada para el contexto. 🎯
# Back_Cloud
