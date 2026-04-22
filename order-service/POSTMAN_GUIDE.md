# Guía de Postman - order-service

## 🚀 Cómo Importar la Colección

### Método 1: Importar desde archivo
1. Abre **Postman**
2. Click en **Import** (esquina superior izquierda)
3. Arrastra el archivo `Postman_Collection.json` o click en **Choose Files**
4. Selecciona el archivo `Postman_Collection.json`
5. Click en **Import**

### Método 2: Importar desde JSON
1. Abre Postman
2. Click en **Import** → **Raw text**
3. Copia y pega el contenido del archivo `Postman_Collection.json`
4. Click en **Continue** → **Import**

---

## ⚙️ Variables de Entorno

La colección incluye **4 variables** que puedes modificar:

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| `baseUrl` | `http://localhost:8081` | URL base del servidor |
| `userId` | `1` | ID del usuario para pruebas |
| `productId` | `1` | ID del producto para pruebas |
| `orderId` | `1` | ID de orden para pruebas |

**Cómo actualizar variables:**
1. En la colección, ve a la pestaña **Variables**
2. Modifica el campo **Current Value**
3. Guarda los cambios (Ctrl + S)

---

## 📁 Estructura de la Colección

### 👤 Usuarios (4 endpoints)
- **POST** Registrar Usuario
- **POST** Login de Usuario
- **GET** Listar Todos los Usuarios
- **GET** Obtener Usuario por ID

### 📦 Productos (6 endpoints)
- **POST** Crear Producto
- **GET** Listar Todos los Productos
- **GET** Obtener Producto por ID
- **GET** Filtrar por Categoría
- **PUT** Actualizar Stock
- **PUT** Actualizar Precio

### 🛒 Carrito (4 endpoints)
- **POST** Agregar Producto al Carrito
- **GET** Ver Carrito del Usuario
- **DELETE** Eliminar Item del Carrito
- **DELETE** Vaciar Carrito

### 📋 Órdenes (4 endpoints)
- **POST** Crear Orden desde Carrito
- **GET** Obtener Orden por ID
- **GET** Listar Órdenes de Usuario
- **GET** Listar Todas las Órdenes

### 🧪 Flujo Completo E2E (9 pasos)
Secuencia completa de prueba desde registro hasta verificación de stock.

---

## 🎯 Flujo de Prueba Recomendado

### Paso 1: Verificar servidor
```bash
# En terminal PowerShell
mvn spring-boot:run
```
Espera a que aparezca: `Started OrderServiceApplication`

### Paso 2: Prueba básica
1. Ejecuta: **Usuarios → Listar Todos los Usuarios** (debería retornar array vacío)
2. Ejecuta: **Productos → Listar Todos los Productos** (debería retornar array vacío)

### Paso 3: Flujo End-to-End
Usa la carpeta **🧪 Flujo Completo E2E** y ejecuta en orden:

1. **Registrar Usuario** → Guarda el `id` retornado
2. **Crear Producto 1** → Guarda el `id` retornado
3. **Crear Producto 2** → Guarda el `id` retornado
4. **Agregar Mouse al Carrito** → Usa los IDs guardados
5. **Agregar Teclado al Carrito**
6. **Ver Carrito** → Verifica total calculado
7. **Crear Orden** → Se descuenta stock y vacía carrito
8. **Ver Órdenes del Usuario** → Confirma orden creada
9. **Verificar Stock Actualizado** → El stock debe reducirse

---

## 💡 Ejemplos de Respuesta

### ✅ Registro Exitoso
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "id": 1,
    "username": "cliente1",
    "email": "cliente1@example.com",
    "fullName": "Cliente Uno"
  }
}
```

### ✅ Carrito con Items
```json
{
  "success": true,
  "message": "Operación exitosa",
  "data": {
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "productName": "Mouse Logitech",
        "price": 25.99,
        "quantity": 2,
        "subtotal": 51.98
      }
    ],
    "total": 51.98
  }
}
```

### ✅ Orden Creada
```json
{
  "success": true,
  "message": "Orden creada exitosamente",
  "data": {
    "id": 1,
    "userId": 1,
    "items": [...],
    "totalAmount": 141.97,
    "status": "COMPLETED",
    "createdAt": "2026-02-27T..."
  }
}
```

### ❌ Error - Producto No Encontrado
```json
{
  "success": false,
  "message": "Producto no encontrado con ID: 999",
  "timestamp": "2026-02-27T..."
}
```

### ❌ Error - Stock Insuficiente
```json
{
  "success": false,
  "message": "Stock insuficiente para el producto: Laptop Dell",
  "timestamp": "2026-02-27T..."
}
```

---

## 🔧 Solución de Problemas

### Error: "Could not get any response"
**Causa:** El servidor no está ejecutándose.
**Solución:**
```bash
mvn spring-boot:run
```

### Error: "Connection refused"
**Causa:** Puerto incorrecto.
**Solución:** Verifica que `baseUrl` sea `http://localhost:8081`

### Error 404 Not Found
**Causa:** Endpoint incorrecto.
**Solución:** Verifica que la ruta comience con `/api/`

### Error 400 Bad Request
**Causa:** JSON inválido o campos faltantes.
**Solución:** Revisa el body en la pestaña **Body → raw → JSON**

---

## 📊 Tips de Uso

### 1. Variables Dinámicas
Actualiza `userId`, `productId`, `orderId` después de cada creación:
- Copia el `id` de la respuesta
- Pega en **Variables** de la colección

### 2. Ejecutar Múltiples Requests
Usa **Collection Runner**:
1. Click derecho en carpeta **🧪 Flujo Completo E2E**
2. Selecciona **Run collection**
3. Click **Run order-service - API**
4. Ver resultados en tiempo real

### 3. Exportar Resultados
En Collection Runner:
- Click en **Export Results**
- Guarda como JSON o HTML

### 4. Modo Consola
Abre la consola de Postman (View → Show Postman Console):
- Ver headers completos
- Inspeccionar requests/responses
- Debug de errores

---

## 📚 Recursos Adicionales

- **Documentación completa:** Ver `README.md` en el proyecto
- **Guía de testing:** Ver `TESTING.md`
- **Guía rápida:** Ver `QUICKSTART.md`
- **Resumen ejecutivo:** Ver `RESUMEN.md`

---

## 🎓 Notas Académicas

Esta colección demuestra:
- ✅ **REST API** con verbos HTTP correctos (GET, POST, PUT, DELETE)
- ✅ **CRUD completo** en todos los módulos
- ✅ **Respuestas consistentes** con formato `ApiResponse<T>`
- ✅ **Manejo de errores** centralizado con códigos HTTP apropiados
- ✅ **Validaciones de negocio** (stock, usuarios, etc.)
- ✅ **Simulación de transacciones** con rollback manual
- ✅ **Arquitectura monolítica** con comunicación directa entre módulos

---

**Última actualización:** 27 de Febrero 2026  
**Versión:** 1.0.0  
**Framework:** Spring Boot 4.0.3  
**Java:** 17
