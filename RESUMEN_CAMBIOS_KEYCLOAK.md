# ✅ Resumen de Cambios - Integración Keycloak + Docker

## 🎯 ¿Qué se corrigió?

### 1. **Inconsistencias en URLs de Keycloak**
   - ❌ **Antes:** `api-gateway` mezclaba `localhost:8180` y `keycloak:8080`
   - ✅ **Ahora:** Usa variables de entorno con valores por defecto correctos
   
### 2. **Variables de entorno unificadas**
   - Todos los servicios ahora usan `KEYCLOAK_ISSUER_URI`
   - Los valores por defecto apuntan a `http://keycloak:8080` (nombre del servicio Docker)
   
### 3. **Health checks y dependencias**
   - Gateway espera a que Keycloak esté saludable antes de iniciar
   - Todos los servicios dependen correctamente de Keycloak

### 4. **Documentación completa**
   - Guía paso a paso en `GUIA_KEYCLOAK_DOCKER.md`
   - Quick start en `infra/LEVANTAR_TODO.md`
   - Archivo `.env.example` con todas las variables documentadas

---

## 📁 Archivos modificados

### Configuración de servicios:
- ✅ `api-gateway/src/main/resources/application.yml`
- ✅ `seguimiento-service/src/main/resources/application.properties`
- ✅ `logistica-service/src/main/resources/application.properties`

### Docker:
- ✅ `infra/docker-compose.yml`

### Documentación nueva:
- ✅ `GUIA_KEYCLOAK_DOCKER.md` (guía completa)
- ✅ `infra/LEVANTAR_TODO.md` (quick start)
- ✅ `infra/.env.example` (template de variables)

---

## 🚀 Cómo usar (TL;DR)

### 1. Levantar todo:
```bash
cd infra
docker compose up -d
```

### 2. Configurar Keycloak (primera vez):
1. Abrir http://localhost:8180
2. Login: `admin` / `admin123`
3. Crear realm: `tpi-backend`
4. Crear cliente: `tpi-backend-client` (público)
5. Crear roles: `admin`, `cliente`
6. Crear usuarios y asignar roles

### 3. Probar autenticación:
```bash
# Login vía navegador
open http://localhost:8080/api/login

# Obtener token (tras login)
curl http://localhost:8080/api/me/token

# Llamar endpoint protegido
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/seguimiento/solicitudes
```

---

## 🔍 Diferencias entre ejecución local y Docker

| Contexto | Keycloak URL (issuer-uri) | Puerto Keycloak | Desde |
|----------|---------------------------|-----------------|-------|
| **Docker** | `http://keycloak:8080` | 8180 → 8080 | Contenedor → Contenedor |
| **Navegador** | `http://localhost:8180` | 8180 | Host → Contenedor |
| **Local dev** | `http://localhost:8180` | 8180 | App local → Contenedor |

**Variables de entorno configuradas:**

- `KEYCLOAK_ISSUER_URI`: URL para validar tokens (desde microservicios)
- `KEYCLOAK_AUTHZ_URI`: URL para redirigir al login (desde navegador)
- `GATEWAY_REDIRECT_URI`: URL de callback tras login

---

## ✅ Validaciones implementadas

### En `application.yml` (gateway):
```yaml
spring.security.oauth2:
  resourceserver:
    jwt:
      issuer-uri: ${KEYCLOAK_ISSUER_URI:http://keycloak:8080/realms/tpi-backend}
  client:
    registration.keycloak:
      redirect-uri: "${GATEWAY_REDIRECT_URI:http://localhost:8080/api/login/oauth2/code/keycloak}"
    provider.keycloak:
      authorization-uri: ${KEYCLOAK_AUTHZ_URI:http://keycloak:8080/realms/...}
```

### En `application.properties` (microservicios):
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI:http://keycloak:8080/realms/tpi-backend}
```

### En `docker-compose.yml`:
```yaml
environment:
  - KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/tpi-backend
  - KEYCLOAK_AUTHZ_URI=http://localhost:8180/realms/tpi-backend/protocol/openid-connect/auth
```

---

## 🎯 Flujo OAuth2/OIDC implementado

```
┌─────────┐     ┌─────────┐     ┌──────────┐     ┌──────────────┐
│ Usuario │────▶│ Gateway │────▶│ Keycloak │────▶│ Microservicio│
└─────────┘     └─────────┘     └──────────┘     └──────────────┘
     │               │                │                  │
     │  1. /api/login│                │                  │
     │──────────────▶│                │                  │
     │               │ 2. Redirect    │                  │
     │               │───────────────▶│                  │
     │               │                │                  │
     │  3. Formulario login            │                  │
     │◀──────────────────────────────│                  │
     │               │                │                  │
     │  4. Credenciales                │                  │
     │───────────────────────────────▶│                  │
     │               │                │                  │
     │               │ 5. Code        │                  │
     │               │◀───────────────│                  │
     │               │                │                  │
     │               │ 6. Exchange code                   │
     │               │───────────────▶│                  │
     │               │                │                  │
     │               │ 7. Access token│                  │
     │               │◀───────────────│                  │
     │               │                │                  │
     │  8. Token     │                │                  │
     │◀──────────────│                │                  │
     │               │                │                  │
     │  9. API call con token         │                  │
     │──────────────▶│────────────────┼─────────────────▶│
     │               │                │  10. Valida JWT  │
     │               │                │◀─────────────────│
     │               │                │                  │
     │  11. Response │                │                  │
     │◀──────────────│◀───────────────┴─────────────────│
```

---

## 🔒 Seguridad implementada

✅ **Cliente público** (sin client secret) - apropiado para SPAs  
✅ **Authorization Code Flow** - flujo OAuth2 seguro  
✅ **JWT validation** - microservicios validan tokens contra Keycloak  
✅ **CORS configurado** - solo orígenes permitidos  
✅ **Roles y permisos** - configurables en Keycloak  

### Próximos pasos de seguridad (producción):
- [ ] Usar HTTPS en todos los endpoints
- [ ] Client credentials flow para comunicación server-to-server
- [ ] Refresh tokens y manejo de expiración
- [ ] Rate limiting y protección contra ataques
- [ ] Secrets management (no hardcodear passwords)

---

## 📚 Recursos

- **Keycloak Admin Console:** http://localhost:8180
- **API Gateway:** http://localhost:8080
- **Gateway Health:** http://localhost:8080/actuator/health
- **GeoAPI:** http://localhost:8083
- **Keycloak Docs:** https://www.keycloak.org/documentation
- **Spring Security OAuth2:** https://spring.io/guides/tutorials/spring-boot-oauth2/

---

## ❓ FAQ

### ¿Por qué dos puertos para Keycloak (8080 y 8180)?

- **8080** es el puerto interno del contenedor Keycloak
- **8180** es el puerto expuesto al host (tu máquina)
- Los microservicios hablan entre sí usando nombres de servicio (`keycloak:8080`)
- Tu navegador accede desde el host (`localhost:8180`)

### ¿Cómo agrego autorización basada en roles?

En tus controllers, agrega:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin-only")
public String adminEndpoint() {
    return "Solo admins";
}
```

### ¿Cómo propago el token del gateway a los microservicios?

Opción 1: El frontend obtiene el token y lo envía directamente  
Opción 2: Crea un filtro en el gateway que agregue el header `Authorization`

### ¿Puedo usar esto en producción?

Con ajustes:
- ✅ Cambiar contraseñas por defecto
- ✅ Usar HTTPS
- ✅ Configurar client secrets (si aplica)
- ✅ Usar base de datos persistente (no H2)
- ✅ Configurar límites de recursos (CPU/RAM)
- ✅ Implementar logging centralizado

---

## 🎉 Conclusión

Tu implementación de Keycloak está **casi perfecta** según las guías oficiales. Los cambios realizados corrigen las inconsistencias de URLs y preparan el sistema para producción.

**Puntos destacados:**
- ✅ Arquitectura de microservicios correcta
- ✅ OAuth2/OIDC implementado según estándares
- ✅ Separación de concerns (gateway vs microservicios)
- ✅ Configuración mediante variables de entorno
- ✅ Healthchecks y dependencias correctas
- ✅ Documentación completa

**Recomendaciones finales:**
1. Prueba el flujo completo con los comandos de la guía
2. Crea usuarios de prueba en Keycloak
3. Implementa `@PreAuthorize` en endpoints críticos
4. Configura refresh tokens si la sesión debe durar > 5 minutos
5. Considera agregar un frontend (React/Angular) que consuma estos servicios

---

**¿Necesitas ayuda con algún paso específico?** Toda la documentación está en:
- `GUIA_KEYCLOAK_DOCKER.md` - Guía completa
- `infra/LEVANTAR_TODO.md` - Quick start
