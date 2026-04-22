# Configuracion GitHub Actions - order-service

## Valores desde Terraform

Basado en `terraform output` del ambiente **local**:

```
EC2 Host: 3.226.76.177
EC2 Instance ID: i-011535c7fc838a8fd
Backend URL: http://3.226.76.177:8081
Order Service Port: 8081
SSH User: ubuntu
Deploy Path: /opt/ecommerce
```

---

## Secrets Requeridos

### 1. EC2_HOST
```bash
gh secret set EC2_HOST --body "3.226.76.177"
```
**Valor:** `3.226.76.177`

### 2. EC2_SSH_KEY
```bash
# Desde tu archivo .pem de AWS:
gh secret set EC2_SSH_KEY < ~/.ssh/tu-clave-ec2.pem
```
**Nota:** Usa la clave SSH privada (.pem) que creaste en AWS para esta instancia EC2.

---

## Variables (Opcionales - tienen defaults)

### 3. EC2_SSH_USER
```bash
gh variable set EC2_SSH_USER --body "ubuntu"
```
**Default:** `ubuntu` (ya configurado en workflow)

### 4. BACKEND_APP_DIR
```bash
gh variable set BACKEND_APP_DIR --body "/opt/ecommerce"
```
**Default:** `/opt/ecommerce` (ya configurado en workflow)

### 5. ORDER_SERVICE_PORT
```bash
gh variable set ORDER_SERVICE_PORT --body "8081"
```
**Default:** `8081` (ya configurado en workflow)

### 6. BACKEND_PORT
```bash
gh variable set BACKEND_PORT --body "8081"
```
**Compatibilidad temporal:** alias del puerto de `order-service`.

---

## Configuración Manual en GitHub

Si prefieres usar la UI de GitHub:

1. Ve a tu repositorio backend en GitHub
2. **Settings** → **Secrets and variables** → **Actions**
3. En la pestaña **Secrets**:
   - Click **New repository secret**
   - Nombre: `EC2_HOST`, Valor: `3.226.76.177`
   - Click **New repository secret**
   - Nombre: `EC2_SSH_KEY`, Valor: [contenido completo del .pem]

4. En la pestaña **Variables** (opcional):
   - Click **New repository variable**
   - Nombre: `EC2_SSH_USER`, Valor: `ubuntu`
   - Click **New repository variable**
   - Nombre: `BACKEND_APP_DIR`, Valor: `/opt/ecommerce`
   - Click **New repository variable**
   - Nombre: `BACKEND_PORT`, Valor: `8081`
   - Nombre: `ORDER_SERVICE_PORT`, Valor: `8081`
   - Nombre: `BACKEND_PORT`, Valor: `8081` (compatibilidad)

---

## Verificación

Una vez configurado, el workflow se ejecutará automáticamente al hacer push a `main` o `develop`:

```bash
git add .
git commit -m "Deploy backend"
git push origin main
```

Verifica el despliegue:
```bash
curl http://3.226.76.177:8081/health
```

---

## Troubleshooting

### Error: "Falta secret EC2_HOST"
**Solución:** Configura el secret EC2_HOST con el valor `3.226.76.177`

### Error: "Permission denied (publickey)"
**Solución:** Verifica que EC2_SSH_KEY contenga la clave privada completa (.pem) incluyendo las líneas `-----BEGIN RSA PRIVATE KEY-----` y `-----END RSA PRIVATE KEY-----`

### Error: Connection timeout
**Solución:** Verifica que el Security Group permita SSH (puerto 22) desde GitHub Actions IPs o desde 0.0.0.0/0

---

**Última actualización:** 27 de Febrero 2026
**Terraform Environment:** local
