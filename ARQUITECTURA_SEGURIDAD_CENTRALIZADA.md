# 🔐 Arquitectura de Seguridad Centralizada en API Gateway

## 📋 Resumen

Este proyecto implementa **seguridad centralizada en el API Gateway**, siguiendo el patrón **Gateway as Security Enforcement Point**. 

Toda la autenticación y autorización se maneja en el Gateway, dejando que los microservicios se enfoquen exclusivamente en la lógica de negocio.

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENTE                             │
│                  (Postman / Frontend)                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ HTTP Request + JWT Token
                         │ (Bearer Authentication)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     KEYCLOAK                                │
│                (Authorization Server)                       │
│  • Autentica usuarios (admin01, cliente01, transportista01) │
│  • Emite tokens JWT con roles                               │
│  • Puerto 8180 (EXPUESTO AL HOST)                           │
└─────────────────────────────────────────────────────────────┘
                         │
                         │ 1. Cliente obtiene token
                         │ 2. Cliente envía petición con token
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY                              │
│              (Security Enforcement Point)                   │
│  • Puerto 8080 (ÚNICO PUERTO EXPUESTO AL HOST)              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ RESPONSABILIDADES DE SEGURIDAD:                       │  │
│  │ ✅ Valida firma del JWT (usando JWK Set de Keycloak)  │  │
│  │ ✅ Verifica expiración del token                      │  │
│  │ ✅ Extrae roles (resource_access.tpi-backend-client)  │  │
│  │ ✅ Aplica RBAC por endpoint y método HTTP             │  │
│  │ ✅ Retorna 401 Unauthorized si token inválido         │  │
│  │ ✅ Retorna 403 Forbidden si rol insuficiente          │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
│  Componentes:                                                │
│  • SecurityConfig.java - Reglas RBAC completas               │
│  • KeycloakRoleConverter.java - Extrae roles del JWT         │
│  • GatewayConfig.java - Ruteo a microservicios               │
└────────────┬────────────┬───────────────┬────────────────────┘
             │            │               │
             │            │               │ (Red interna Docker)
             │            │               │ (NO expuestos al host)
             ▼            ▼               ▼
┌──────────────┐  ┌────────────────┐  ┌─────────────┐
│ LOGÍSTICA    │  │ SEGUIMIENTO    │  │  GEOAPI     │
│ SERVICE      │  │ SERVICE        │  │  SERVICE    │
│ Puerto 8082  │  │ Puerto 8081    │  │ Puerto 8083 │
├──────────────┤  ├────────────────┤  ├─────────────┤
│ SIN          │  │ SIN            │  │ SIN         │
│ SEGURIDAD    │  │ SEGURIDAD      │  │ SEGURIDAD   │
│              │  │                │  │             │
│ Solo lógica  │  │ Solo lógica    │  │ Solo lógica │
│ de negocio   │  │ de negocio     │  │ de negocio  │
└──────────────┘  └────────────────┘  └─────────────┘
```

---

## 🔑 Matriz de Autorización (RBAC)

Implementada completamente en `api-gateway/SecurityConfig.java`:

### **LOGÍSTICA SERVICE**

| Endpoint                      | GET               | POST  | PUT   | DELETE |
|-------------------------------|-------------------|-------|-------|--------|
| `/api/logistica/camiones`     | admin, transportista | admin | admin | admin  |
| `/api/logistica/depositos`    | admin             | admin | admin | admin  |
| `/api/logistica/ubicaciones`  | admin             | admin | admin | admin  |
| `/api/logistica/rutas`        | admin, transportista | admin | admin | admin  |
| `/api/logistica/tramos`       | admin, transportista | admin | admin | admin  |
| `/api/logistica/tarifas`      | admin             | admin | admin | admin  |

### **SEGUIMIENTO SERVICE**

| Endpoint                          | GET                      | POST          | PUT               | DELETE |
|-----------------------------------|--------------------------|---------------|-------------------|--------|
| `/api/seguimiento/solicitudes`    | cliente, transportista, admin | cliente, admin | transportista, admin | admin  |
| `/api/seguimiento/contenedores`   | authenticated            | cliente, admin | cliente, admin    | admin  |
| `/api/seguimiento/clientes`       | admin                    | admin         | admin             | admin  |

### **GEOAPI SERVICE**

| Endpoint          | Acceso            |
|-------------------|-------------------|
| `/geoapi/**`      | authenticated     |

---

## 📁 Estructura de Archivos

```
api-gateway/
├── src/main/java/ar/edu/tpi/gateway/config/
│   ├── SecurityConfig.java           ✅ TODA la seguridad y RBAC
│   ├── KeycloakRoleConverter.java    ✅ Extrae roles del JWT
│   └── GatewayConfig.java            ✅ Ruteo a microservicios
└── src/main/resources/
    └── application.yml                ✅ Configuración JWT (jwk-set-uri)

logistica-service/
├── pom.xml                            ❌ SIN dependencias de Security
└── src/main/resources/
    └── application.properties         ❌ SIN configuración OAuth2

seguimiento-service/
├── pom.xml                            ❌ SIN dependencias de Security
└── src/main/resources/
    └── application.properties         ❌ SIN configuración OAuth2

infra/
└── docker-compose.yml
    ├── api-gateway                    ✅ Puerto 8080 EXPUESTO
    ├── logistica-service              ❌ SIN puerto expuesto
    ├── seguimiento-service            ❌ SIN puerto expuesto
    ├── geoapi                         ❌ SIN puerto expuesto
    └── keycloak                       ✅ Puerto 8180 EXPUESTO (Admin UI)
```

---

## 🚀 Cómo Funciona

### 1️⃣ **Cliente obtiene token**

```bash
curl -X POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tpi-backend-client" \
  -d "username=admin01" \
  -d "password=Clave123"
```

**Respuesta:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_expires_in": 1800
}
```

### 2️⃣ **Cliente accede a endpoint vía Gateway**

```bash
curl -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI..." \
  http://localhost:8080/api/logistica/camiones
```

### 3️⃣ **Gateway procesa la petición**

1. **Valida el JWT:**
   - Descarga claves públicas desde Keycloak (`jwk-set-uri`)
   - Verifica firma criptográfica
   - Verifica expiración (`exp` claim)

2. **Extrae roles usando KeycloakRoleConverter:**
   ```java
   // De este claim del JWT:
   "resource_access": {
     "tpi-backend-client": {
       "roles": ["admin"]
     }
   }
   
   // Extrae: ROLE_admin
   ```

3. **Aplica reglas RBAC en SecurityConfig:**
   ```java
   .pathMatchers(HttpMethod.GET, "/api/logistica/camiones/**")
     .hasAnyRole("admin", "transportista")
   ```

4. **Si autorizado:** Rutea al microservicio interno
5. **Si NO autorizado:** Retorna `403 Forbidden`
6. **Si token inválido:** Retorna `401 Unauthorized`

### 4️⃣ **Microservicio procesa lógica de negocio**

- **NO valida token** (ya validado por Gateway)
- **NO verifica roles** (ya verificado por Gateway)
- **Solo ejecuta** lógica de negocio (crear camión, listar solicitudes, etc.)

---

## ✅ Ventajas de esta Arquitectura

| Ventaja | Descripción |
|---------|-------------|
| **Separación de responsabilidades** | Gateway = Seguridad, MS = Lógica de negocio |
| **Punto único de aplicación** | Todas las políticas de seguridad en un solo lugar |
| **Simplicidad en microservicios** | Menos dependencias, menos configuración, menos errores |
| **Fácil mantenimiento** | Cambiar reglas RBAC = editar solo SecurityConfig.java |
| **Seguridad por diseño** | Microservicios inaccesibles directamente desde el host |
| **Menor superficie de ataque** | Solo Gateway y Keycloak expuestos al host |

---

## 🔧 Configuración Docker

### Puertos Expuestos al Host

| Servicio | Puerto Host | Puerto Contenedor | Acceso |
|----------|-------------|-------------------|--------|
| **API Gateway** | 8080 | 8080 | ✅ **ÚNICO PUNTO DE ENTRADA** |
| **Keycloak** | 8180 | 8080 | ✅ Admin UI + Token endpoint |
| Logística Service | ❌ | 8082 | Solo red interna |
| Seguimiento Service | ❌ | 8081 | Solo red interna |
| GeoAPI | ❌ | 8083 | Solo red interna |

### Variables de Entorno (Gateway)

```yaml
environment:
  # JWT Validation
  - KEYCLOAK_JWK_URI=http://host.docker.internal:8180/realms/tpi-backend/protocol/openid-connect/certs
  
  # Microservices URLs (red interna)
  - LOGISTICA_URL=http://logistica-service:8082
  - SEGUIMIENTO_URL=http://seguimiento-service:8081
  - GEOAPI_URL=http://geoapi:8083
```

---

## 🧪 Testing

### ✅ Caso 1: Admin crea camión (200 OK)

```bash
# 1. Login como admin
TOKEN=$(curl -s -X POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=tpi-backend-client" \
  -d "username=admin01" \
  -d "password=Clave123" | jq -r .access_token)

# 2. Crear camión
curl -X POST http://localhost:8080/api/logistica/camiones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dominio": "AA111BB",
    "capacidadPesoKg": 15000,
    "capacidadVolumenM3": 50,
    "costoKmBase": 100,
    "consumoPromedioLitrosKm": 0.35,
    "disponible": true
  }'

# Respuesta: 200 OK
```

### ❌ Caso 2: Cliente intenta crear camión (403 Forbidden)

```bash
# 1. Login como cliente
TOKEN=$(curl -s -X POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=tpi-backend-client" \
  -d "username=cliente01" \
  -d "password=Hola123" | jq -r .access_token)

# 2. Intentar crear camión
curl -X POST http://localhost:8080/api/logistica/camiones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ ... }'

# Respuesta: 403 Forbidden
```

### ❌ Caso 3: Sin token (401 Unauthorized)

```bash
curl http://localhost:8080/api/logistica/camiones

# Respuesta: 401 Unauthorized
```

---

## 📝 Cambios Realizados

### ✅ API Gateway
- ✅ Creado `SecurityConfig.java` con todas las reglas RBAC
- ✅ Creado `KeycloakRoleConverter.java` para extraer roles
- ✅ Configurado `application.yml` con `jwk-set-uri`

### ❌ Logística Service
- ❌ Eliminado `SecurityConfig.java`
- ❌ Eliminado `KeycloakRoleConverter.java`
- ❌ Eliminadas dependencias de Spring Security del `pom.xml`
- ❌ Eliminada configuración OAuth2 de `application.properties`

### ❌ Seguimiento Service
- ❌ Eliminado `SecurityConfig.java`
- ❌ Eliminado `KeycloakRoleConverter.java`
- ❌ Eliminadas dependencias de Spring Security del `pom.xml`
- ❌ Eliminada configuración OAuth2 de `application.properties`

### 🐳 Docker Compose
- ✅ Solo Gateway expuesto en puerto 8080
- ❌ Microservicios SIN puertos expuestos al host
- ❌ Microservicios SIN variables de entorno de Keycloak

---

## 🎓 Comparación con Enfoque Anterior

| Aspecto | Antes (Defense in Depth) | Ahora (Gateway Centralizado) |
|---------|--------------------------|------------------------------|
| **SecurityConfig** | En cada microservicio | Solo en Gateway |
| **Validación JWT** | En cada microservicio | Solo en Gateway |
| **Dependencias Security** | En cada pom.xml | Solo en Gateway |
| **Complejidad MS** | Alta (Security + Negocio) | Baja (Solo Negocio) |
| **Punto de fallo** | Múltiples | Único (Gateway) |
| **Mantenimiento RBAC** | 3 archivos | 1 archivo |
| **Puertos expuestos** | 4 (Gateway + 3 MS) | 1 (Gateway) |

---

## 📚 Referencias

- [Spring Cloud Gateway - Security](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-security)
- [OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Keycloak JWT Structure](https://www.keycloak.org/docs/latest/server_admin/#_token-exchange)
- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)

---

## 🚀 Próximos Pasos

1. ✅ Probar todos los endpoints con Postman
2. ✅ Verificar que microservicios son inaccesibles desde el host
3. ✅ Validar reglas RBAC por rol
4. ⏳ Agregar rate limiting en el Gateway
5. ⏳ Implementar circuit breaker para resiliencia
6. ⏳ Agregar logging y trazabilidad distribuida

---

**Autor:** Sistema de Backend TPI  
**Fecha:** 15 de noviembre de 2025  
**Versión:** 2.0 - Seguridad Centralizada
