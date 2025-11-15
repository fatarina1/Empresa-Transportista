# 🔐 Guía Simple: Keycloak para el TPI

Esta es la guía simplificada para usar Keycloak con tu TPI, siguiendo el enunciado del trabajo.

---

## ✅ Paso 1: Servicios Corriendo

Ya tenés todo levantado con Docker:

```bash
cd infra
docker compose ps
```

Deberías ver:
- ✅ `keycloak` en puerto 8180
- ✅ `api-gateway` en puerto 8080
- ✅ `seguimiento-service`, `logistica-service`, `geoapi`

---

## 🔧 Paso 2: Configurar Keycloak

### 2.1 Acceder a Keycloak

1. Abre: **http://localhost:8180**
2. Login:
   - Usuario: `admin`
   - Contraseña: `admin123`

### 2.2 Crear Realm `tpi-backend`

1. Click en el menú desplegable arriba a la izquierda (dice "master")
2. Click "Create Realm"
3. Realm name: `tpi-backend`
4. Click "Create"

### 2.3 Crear Cliente `tpi-backend-client`

1. Ve a **Clients** → **Create client**
2. Configura:
   - **Client ID:** `tpi-backend-client`
   - **Client type:** `OpenID Connect`
   - Click **Next**
3. En **Capability config:**
   - **Client authentication:** OFF
   - **Authorization:** OFF
   - **Authentication flow:**
     - ✅ **Standard flow** (ON)
     - ✅ **Direct access grants** (ON) ← Importante para obtener tokens
   - Click **Next**
4. En **Login settings:**
   - **Valid redirect URIs:** `http://localhost:8080/*`
   - **Web origins:** `http://localhost:8080`
   - Click **Save**

### 2.4 Crear Roles (según enunciado TPI)

1. Ve a **Realm roles** → **Create role**
2. Crea estos roles:
   - `cliente`
   - `admin` (para Operador/Administrador)
   - `transportista`

### 2.5 Crear Usuarios de Prueba

Crea al menos un usuario por rol:

**Usuario Cliente:**
1. **Users** → **Create new user**
   - Username: `cliente01`
   - Email: `cliente01@example.com`
   - First name: `Cliente`
   - Last name: `Uno`
2. **Credentials** → **Set password**
   - Password: `Hola123`
   - Temporary: **OFF**
3. **Role mapping** → **Assign role** → Selecciona `cliente`

**Usuario Admin:**
1. Username: `admin01`
   - Email: `admin01@example.com`
   - Password: `Clave123` (Temporary: OFF)
   - Rol: `admin`

**Usuario Transportista:**
1. Username: `transportista01`
   - Email: `transportista01@example.com`
   - Password: `Clave123` (Temporary: OFF)
   - Rol: `transportista`

---

## 🧪 Paso 3: Obtener Token (Password Grant)

Este es el método más simple para pruebas del TPI:

```bash
curl -X POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tpi-backend-client" \
  -d "username=cliente01" \
  -d "password=Clave123"
```

**Respuesta esperada:**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "scope": "openid profile email"
}
```

Copia el `access_token` para usarlo en las siguientes peticiones.

---

## 🔒 Paso 4: Probar Endpoints Protegidos

### 4.1 Guardar el token en una variable

```bash
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

(Reemplaza con tu token real)

### 4.2 Llamar a endpoints vía Gateway

```bash
# Ver solicitudes (requiere autenticación)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/seguimiento/solicitudes

# Ver ubicaciones
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/logistica/ubicaciones

# Ver camiones
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/logistica/camiones
```

Si el token es válido, obtendrás los datos. Si no, recibirás `401 Unauthorized`.

---

## 📊 Paso 5: Verificar el Token

1. Copia tu `access_token`
2. Ve a: **https://jwt.io**
3. Pega el token en el decoder
4. Verifica que contenga:
   - `preferred_username`: tu usuario
   - `realm_access.roles`: tus roles asignados
   - `exp`: fecha de expiración (5 minutos por defecto)
   - `iss`: `http://keycloak:8080/realms/tpi-backend`

---

## 🎯 Resumen del Flujo (según TPI)

```
┌─────────────┐
│   Cliente   │
│  (Postman)  │
└──────┬──────┘
       │ 1. POST /token (usuario + password)
       ▼
┌──────────────────┐
│    Keycloak      │  ← Valida credenciales
│  (puerto 8180)   │  ← Devuelve JWT con roles
└──────┬───────────┘
       │ 2. JWT token
       ▼
┌─────────────────┐
│   Cliente con   │
│   Bearer token  │
└──────┬──────────┘
       │ 3. GET/POST con Authorization: Bearer <token>
       ▼
┌──────────────────┐
│   API Gateway    │  ← Valida JWT
│  (puerto 8080)   │  ← Enruta a microservicio
└──────┬───────────┘
       │ 4. Request + JWT
       ▼
┌──────────────────┐
│  Microservicio   │  ← Valida JWT
│ (puerto interno) │  ← Verifica roles
│                  │  ← Ejecuta lógica
└──────┬───────────┘
       │ 5. Response
       ▼
┌─────────────────┐
│    Cliente      │
└─────────────────┘
```

---

## 🛠️ Comandos Útiles

### Reiniciar servicios
```bash
cd infra
docker compose restart api-gateway
docker compose restart seguimiento-service
```

### Ver logs
```bash
docker compose logs -f keycloak
docker compose logs -f api-gateway
```

### Detener todo
```bash
docker compose down
```

### Limpiar todo (incluye datos de Keycloak)
```bash
docker compose down -v
```

---

## ❓ Troubleshooting

### "Invalid user credentials"
→ El usuario no existe o la contraseña es incorrecta en Keycloak.

### "Invalid client"
→ El cliente `tpi-backend-client` no está creado en el realm `tpi-backend`.

### "401 Unauthorized"
→ El token expiró (duran 5 minutos) o no es válido. Obtén uno nuevo.

### "ERR_TOO_MANY_REDIRECTS" en navegador
→ No uses el navegador para obtener tokens. Usa `curl` o Postman con el flujo password grant.

---

## ✅ Checklist para el TPI

- [ ] Keycloak corriendo en puerto 8180
- [ ] Realm `tpi-backend` creado
- [ ] Cliente `tpi-backend-client` creado con Direct Access Grants habilitado
- [ ] Roles creados: `cliente`, `admin`, `transportista`
- [ ] Usuarios de prueba creados con sus roles
- [ ] Puedo obtener token con password grant
- [ ] Los microservicios validan el token JWT
- [ ] Los endpoints verifican roles según el enunciado

---

**¡Listo!** Con esto tenés Keycloak funcionando para el TPI de manera simple. 🎉
