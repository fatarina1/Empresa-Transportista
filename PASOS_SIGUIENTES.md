# ✅ Pasos Siguientes - Sistema Funcionando

## Estado Actual

✅ **Todos los contenedores están corriendo:**
- Keycloak: http://localhost:8180 (healthy)
- API Gateway: http://localhost:8080 (running)
- GeoAPI: http://localhost:8083 (running)
- Logística Service: interno (running)
- Seguimiento Service: interno (running)

✅ **El endpoint OAuth2 está funcionando:**
- URL: http://localhost:8080/oauth2/authorization/keycloak
- Redirige correctamente a Keycloak

---

## 🔧 Paso 1: Configurar Keycloak (PENDIENTE)

**Necesitas hacer esto AHORA antes de probar el login:**

1. **Abre Keycloak**: http://localhost:8180
2. **Inicia sesión** con:
   - Usuario: `admin`
   - Contraseña: `admin123`

3. **Crea el Realm `tpi-backend`:**
   - Click en el menú desplegable arriba a la izquierda (dice "master")
   - Click en "Create Realm"
   - Realm name: `tpi-backend`
   - Click "Create"

4. **Crea el Cliente `tpi-backend-client`:**
   - Ve a "Clients" → "Create client"
   - Client ID: `tpi-backend-client`
   - Client type: `OpenID Connect`
   - Click "Next"
   - **Client authentication:** OFF (cliente público)
   - **Authorization:** OFF
   - **Authentication flow:**
     - ✅ Standard flow
     - ✅ Direct access grants (importante para password grant)
   - Click "Next"
   - **Valid redirect URIs:**
     ```
     http://localhost:8080/api/login/oauth2/code/keycloak
     http://localhost:8080/*
     ```
   - **Web origins:**
     ```
     http://localhost:8080
     ```
   - Click "Save"

5. **Crea Roles:**
   - Ve a "Realm roles" → "Create role"
   - Crea dos roles:
     - `admin`
     - `cliente`

6. **Crea Usuario de Prueba:**
   - Ve a "Users" → "Create new user"
   - Username: `cliente01`
   - Email: `cliente01@example.com`
   - First name: `Cliente`
   - Last name: `Uno`
   - Click "Create"
   - En la pestaña **Credentials**:
     - Click "Set password"
     - Password: `Clave123`
     - Password confirmation: `Clave123`
     - **Temporary:** OFF (importante)
     - Click "Save"
   - En la pestaña **Role mapping**:
     - Click "Assign role"
     - Selecciona `cliente`
     - Click "Assign"

---

## 🧪 Paso 2: Probar la Autenticación

### Opción A: Login vía navegador

1. Abre: **http://localhost:8080/oauth2/authorization/keycloak**
2. Te redirigirá a Keycloak
3. Ingresa:
   - Username: `cliente01`
   - Password: `Clave123`
4. Keycloak te redirigirá de vuelta (verás un 404 pero es normal)

### Opción B: Obtener token directamente (recomendado para pruebas)

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
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "scope": "openid profile email"
}
```

---

## 🔒 Paso 3: Probar Endpoints Protegidos

Una vez tengas el `access_token`, prueba los microservicios:

```bash
# Guarda el token en una variable (reemplaza con tu token real)
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI..."

# Probar seguimiento-service
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/seguimiento/solicitudes

# Probar logística-service
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/logistica/ubicaciones

# Probar geoapi
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/geoapi/api/distancia?origen=Madrid&destino=Barcelona
```

---

## 📚 Documentación Completa

Para más detalles, consulta:
- **GUIA_KEYCLOAK_DOCKER.md**: Guía completa de configuración
- **INICIO_RAPIDO.md**: Guía rápida de inicio

---

## ❓ Troubleshooting Rápido

### "Invalid user credentials"
→ El usuario no existe o la contraseña es incorrecta. Verifica en Keycloak que:
- El realm `tpi-backend` existe
- El usuario existe en ese realm
- La contraseña no es temporal (Temporary: OFF)

### "Invalid client"
→ El cliente no existe. Verifica que `tpi-backend-client` esté creado en el realm `tpi-backend`

### "401 Unauthorized" al llamar endpoints
→ El token expiró o es inválido. Obtén un token nuevo.

### "404 Not Found" al llamar endpoints
→ Verifica que la ruta sea correcta (ej: `/api/seguimiento/solicitudes` no `/solicitudes`)

---

**¡Siguiente paso:** Configura Keycloak siguiendo el Paso 1 de arriba! 🚀
