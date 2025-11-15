# 🚀 Cómo levantar TODO el sistema con Docker

## Comando rápido (todo en uno)

```bash
cd infra
docker compose up -d
```

Esto levantará **5 contenedores**:
1. ✅ **keycloak** (Identity Provider) - http://localhost:8180
2. ✅ **api-gateway** (Entry point) - http://localhost:8080  
3. ✅ **seguimiento-service** (interno)
4. ✅ **logistica-service** (interno)
5. ✅ **geoapi** (interno, expuesto en 8083)

---

## ⏱️ Tiempo estimado

- **Primera vez (build + start):** 3-5 minutos
- **Siguientes veces (solo start):** 30-60 segundos

---

## 📋 Verificar que todo esté corriendo

```bash
docker compose ps
```

Deberías ver algo como:

```
NAME                    STATUS          PORTS
keycloak                Up (healthy)    0.0.0.0:8180->8080/tcp
api-gateway             Up              0.0.0.0:8080->8080/tcp
seguimiento-service     Up              8081/tcp
logistica-service       Up              8082/tcp
geoapi                  Up              0.0.0.0:8083->8083/tcp
```

---

## 🧪 Probar que funciona

### 1. Verificar salud del gateway

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{"status":"UP"}
```

### 2. Verificar Keycloak

Abre en tu navegador: **http://localhost:8180**

Deberías ver la página de administración de Keycloak.

---

## 🔧 Configuración inicial de Keycloak

**⚠️ IMPORTANTE:** Antes de poder usar la autenticación, debes configurar Keycloak.

Sigue la guía completa en: **[GUIA_KEYCLOAK_DOCKER.md](../GUIA_KEYCLOAK_DOCKER.md)**

Resumen rápido:
1. Acceder a http://localhost:8180 (admin/admin123)
2. Crear realm: `tpi-backend`
3. Crear cliente: `tpi-backend-client` (público)
4. Crear roles: `admin`, `cliente`
5. Crear usuarios con roles asignados

---

## 🛑 Detener todo

```bash
docker compose down
```

---

## 🔄 Reiniciar (tras cambios en código)

```bash
# Reconstruir imágenes
docker compose build

# Levantar de nuevo
docker compose up -d
```

---

## 📊 Ver logs en tiempo real

```bash
# Todos los servicios
docker compose logs -f

# Solo gateway
docker compose logs -f api-gateway

# Solo keycloak
docker compose logs -f keycloak
```

---

## 🗑️ Limpieza completa (borrar todo)

```bash
# Detener y borrar contenedores + volúmenes
docker compose down -v

# Borrar imágenes construidas
docker rmi tpi/api-gateway:latest
docker rmi tpi/seguimiento-service:latest
docker rmi tpi/logistica-service:latest
docker rmi tpi/geoapi:latest
```

---

## 💡 Tips

- Los microservicios **NO** están expuestos al host por seguridad
- Solo accede a ellos a través del **gateway** (puerto 8080)
- Para desarrollo local (sin Docker), usa `mvn spring-boot:run` en cada servicio
- Keycloak usa un volumen persistente (`keycloak_data`) - tu configuración se guarda entre reinicios

---

## ✅ Checklist de verificación

- [ ] Docker y Docker Compose instalados
- [ ] Puerto 8080 libre (gateway)
- [ ] Puerto 8180 libre (Keycloak)
- [ ] Puerto 8083 libre (geoapi)
- [ ] Variable `GOOGLE_MAPS_APIKEY` configurada (en `.env` o sistema)
- [ ] Ejecuté `docker compose up -d` desde `infra/`
- [ ] Todos los contenedores están `Up`
- [ ] Configuré Keycloak (realm, cliente, roles, usuarios)
- [ ] Probé login en http://localhost:8080/api/login

---

**¿Problemas?** Consulta la sección **Troubleshooting** en [GUIA_KEYCLOAK_DOCKER.md](../GUIA_KEYCLOAK_DOCKER.md)
