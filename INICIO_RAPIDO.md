# 🎯 Inicio Rápido - Sistema Completo

## ▶️ Levantar TODO en 3 comandos

```bash
# 1. Ir a la carpeta infra
cd infra

# 2. Levantar todos los servicios
docker compose up -d

# 3. Verificar que todo esté corriendo
docker compose ps
```

**Tiempo estimado:** 2-3 minutos (primera vez puede ser más por el build)

---

## ✅ Verificación rápida

```bash
# Gateway está UP
curl http://localhost:8080/actuator/health

# Keycloak accesible
curl http://localhost:8180
```

---

## 🔑 Configurar Keycloak (PRIMERA VEZ)

1. **Abrir:** http://localhost:8180
2. **Login:** `admin` / `admin123`
3. **Crear Realm:** `tpi-backend`
4. **Crear Cliente:** `tpi-backend-client` (tipo: público)
5. **Crear Roles:** `admin`, `cliente`
6. **Crear Usuario:**
   - Username: `cliente01`
   - Password: `Clave123` (temporal OFF)
   - Asignar rol: `cliente`

**Guía detallada:** Ver `GUIA_KEYCLOAK_DOCKER.md`

---

## 🧪 Probar Autenticación

### Opción 1: Navegador (recomendado)

1. Visitar: http://localhost:8080/api/login
2. Te redirige a Keycloak → ingresar credenciales
3. Tras login exitoso, obtener token:

```bash
curl http://localhost:8080/api/me/token
```

### Opción 2: Script automático

```bash
cd infra
chmod +x test-keycloak.sh
./test-keycloak.sh
```

### Opción 3: Comando directo (desarrollo)

```bash
curl -X POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=tpi-backend-client" \
  -d "username=cliente01" \
  -d "password=Clave123"
```

---

## 🔒 Llamar Endpoints Protegidos

```bash
# Reemplaza <TOKEN> con el access_token que obtuviste

# Listar solicitudes
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/seguimiento/solicitudes

# Listar ubicaciones
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/logistica/ubicaciones

# Listar clientes
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/seguimiento/clientes
```

---

## 📊 Ver Logs

```bash
# Todos los servicios
docker compose logs -f

# Solo gateway
docker compose logs -f api-gateway

# Solo keycloak
docker compose logs -f keycloak
```

---

## 🛑 Detener

```bash
docker compose down
```

---

## 🔄 Reiniciar (tras cambios en código)

```bash
docker compose build
docker compose up -d
```

---

## 📚 Documentación Completa

- **`GUIA_KEYCLOAK_DOCKER.md`** - Guía paso a paso completa
- **`infra/LEVANTAR_TODO.md`** - Comandos básicos
- **`RESUMEN_CAMBIOS_KEYCLOAK.md`** - Qué se corrigió y por qué

---

## 🆘 Problemas Comunes

### "Connection refused"
**Solución:** Espera 30-60 segundos a que los servicios inicien

### "401 Unauthorized"
**Solución:** Token expirado (5 min), obtén uno nuevo

### "Keycloak no inicia"
**Solución:** Revisa logs: `docker compose logs -f keycloak`

---

## ✨ Servicios Disponibles

| Servicio | URL | Puerto |
|----------|-----|--------|
| **API Gateway** | http://localhost:8080 | 8080 |
| **Keycloak Admin** | http://localhost:8180 | 8180 |
| **GeoAPI** | http://localhost:8083 | 8083 |
| Seguimiento | (interno) | 8081 |
| Logística | (interno) | 8082 |

**Nota:** Los microservicios solo son accesibles vía Gateway por seguridad.

---

**¡Listo para usar!** 🚀
