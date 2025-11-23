# 🎯 Inicio Rápido - Sistema Completo

## 🚀 Guía de Prueba Completa del Sistema

Este documento te guía para probar **TODOS los flujos** del sistema en el orden correcto.

---

## ▶️ Paso 1: Levantar TODO el Sistema

```bash
# 1. Ir a la carpeta infra
cd infra

# 2. Levantar todos los servicios (PostgreSQL, Keycloak, GeoAPI, Gateway, Servicios)
docker compose up -d

# 3. Verificar que todo esté corriendo
docker compose ps
```

**Tiempo estimado:** 2-3 minutos (primera vez puede tardar más por el build)

**Servicios disponibles:**
- 🔐 Keycloak: http://localhost:8180
- 🚪 API Gateway: http://localhost:8080
- 🗺️ GeoAPI: http://localhost:3001
- 🗄️ PostgreSQL: localhost:5432 (seguimiento) y 5433 (logistica)

---

## 🔑 Paso 2: Configurar Keycloak (SOLO PRIMERA VEZ)

1. **Abrir:** http://localhost:8180
2. **Login:** `admin` / `admin123`
3. **Crear Realm:** `tpi-backend`
4. **Crear Cliente:** `tpi-backend-client`
   - Client authentication: OFF (público)
   - Valid redirect URIs: `http://localhost:8080/*`
5. **Crear Roles de Cliente:**
   - `admin`
   - `cliente`
   - `transportista`
6. **Crear Usuarios:**
   - Username: `cliente01` / Password: `Hola123` → Rol: `cliente`
   - Username: `admin01` / Password: `Clave123` → Rol: `admin`
   - Username: `transportista01` / Password: `Clave123` → Rol: `transportista`

**Guía detallada:** Ver `GUIA_KEYCLOAK_DOCKER.md`

---

## 📥 Paso 3: Importar Colección de Postman

1. Abrir Postman
2. Importar: `POSTMAN_COLLECTION_V2.json`
3. Verás 7 carpetas organizadas:
   - 🔐 1. Autenticación
   - 🎯 2. FLUJO COMPLETO - Nueva Solicitud
   - 📦 3. Contenedores en Depósito (RF5)
   - 💰 4. Costos Reales (RF8)
   - 🧮 5. Rutas Alternativas (RF3)
   - 📋 6. Consultas Generales
   - 🔧 7. Operaciones del Transportista

---

## 🧪 Paso 4: Probar los Flujos del Sistema

### 🔐 A. AUTENTICACIÓN

1. Ejecutar: **"Login como CLIENTE"**
   - ✅ Se guarda automáticamente el token en variable `access_token_cliente`
2. Ejecutar: **"Login como ADMIN"**
   - ✅ Se guarda automáticamente el token en variable `access_token_admin`
3. Ejecutar: **"Login como TRANSPORTISTA"**
   - ✅ Se guarda automáticamente el token en variable `access_token_transportista`

---

### 🎯 B. FLUJO COMPLETO - Crear una Nueva Solicitud

**Este flujo prueba TODO el sistema de punta a punta:**

1. **2.1 CLIENTE - Listar mis Contenedores**
   - Ver contenedores disponibles (8, 9, 10 están libres)
   
2. **2.2 CLIENTE - Crear Solicitud**
   - Crea solicitud con contenedor 8 (Material de oficina: 450kg, 2.8m³)
   - Estado inicial: `BORRADOR`
   - ✅ Se guarda automáticamente el ID en `nueva_solicitud_id`

3. **2.3 ADMIN - Calcular Rutas Alternativas**
   - Origen: Córdoba (ID=1)
   - Destino: Mendoza (ID=2)
   - **Resultado esperado:**
     - **Ruta 1 (Directa):** Córdoba → Mendoza (~10h, sin estadía)
     - **Ruta 2 (Optimizada):** Córdoba → [Depósito automático + 24h estadía] → Mendoza
   - 💰 Costos incluyen estadía
   - ⏱️ Tiempos incluyen 24h por cada depósito

4. **2.4 ADMIN - Asignar Ruta a Solicitud**
   - El sistema genera la ruta óptima
   - Estado cambia a: `PROGRAMADA`
   - ✅ Se guarda automáticamente el ID en `nueva_ruta_id`
   - ✅ Se actualizan costos y tiempos estimados en la solicitud

5. **2.5 ADMIN - Ver Detalles de la Ruta Creada**
   - Ver tramos generados
   - Cada tramo tiene `fechaInicioEstimada` y `fechaFinEstimada`
   - Los tramos que terminan en depósito incluyen 24h de estadía

6. **2.6 TRANSPORTISTA - Asignar Camión al Primer Tramo**
   - ⚠️ Copiar el ID del primer tramo de la respuesta anterior
   - Cambiar "CAMBIAR_POR_ID_TRAMO" en la URL
   - Asignar camión 1 (AB123CD)

7. **2.7 TRANSPORTISTA - Iniciar Primer Tramo**
   - Estado del tramo: `EN_PROGRESO`
   - Estado de la solicitud: `EN_TRANSITO`
   - Se registra `fechaInicioReal`

8. **2.8 TRANSPORTISTA - Finalizar Primer Tramo**
   - Estado del tramo: `FINALIZADO`
   - Se registra `fechaFinReal`
   - Si termina en depósito: contenedor queda esperando 24h

9. **2.9 CLIENTE - Ver Estado de mi Solicitud**
   - Ver estado actualizado
   - Ver costos estimados
   - Ver tiempos estimados

10. **2.10 CLIENTE - Ver Historial de Estados**
    - Ver todos los cambios: BORRADOR → PROGRAMADA → EN_TRANSITO
    - Con fechas y detalles de cada cambio

---

### 📦 C. CONTENEDORES EN DEPÓSITO (RF5)

**Probar consulta de contenedores esperando en depósitos:**

1. **3.1 Contenedores en Depósito Rosario**
   - `GET /api/logistica/depositos/3/contenedores`
   - **Resultado:** Contenedor ID=1 esperando hace 3 días
   - Costo acumulado de estadía: $600/día × 3 = $1,800

2. **3.2 Contenedores en Depósito Mendoza**
   - `GET /api/logistica/depositos/2/contenedores`
   - **Resultado:** Contenedor ID=2 esperando hace 7 días
   - ⚠️ Estadía prolongada
   - Costo acumulado: $450/día × 7 = $3,150

3. **3.3 Listar Todos los Depósitos**
   - Ver los 3 depósitos con sus costos diarios

---

### 💰 D. COSTOS REALES (RF8)

**Probar cálculo de costos reales en rutas finalizadas:**

1. **4.1 ⭐ Ruta 4 - Eficiente**
   - `GET /api/logistica/rutas/4/costo-real`
   - **Resultado esperado:**
     - Costo estimado: $52,000
     - Costo real: $48,500
     - ✅ **Bonificación:** $3,500 (llegó 40min antes)

2. **4.2 ⚠️ Ruta 5 - Con Demoras**
   - `GET /api/logistica/rutas/5/costo-real`
   - **Resultado esperado:**
     - Costo estimado: $95,000
     - Costo real: $108,500
     - ⚠️ **Penalización:** $13,500 (3h de retraso)
     - Incluye costo de estadía en Depósito Córdoba

3. **4.3 🎯 Ruta 6 - Perfecta**
   - `GET /api/logistica/rutas/6/costo-real`
   - **Resultado esperado:**
     - Costo estimado: $42,000
     - Costo real: $42,000
     - 🎯 Sin ajustes (tiempo y costo exactos)

---

### 🧮 E. RUTAS ALTERNATIVAS CON ESTADÍA (RF3)

**Probar cálculo de rutas con depósitos automáticos:**

1. **5.1 Córdoba → Buenos Aires (Carga Liviana)**
   - Contenedor 10: 280kg, 1.5m³
   - **Resultado:**
     - Ruta directa vs ruta con Depósito Rosario
     - Ruta con depósito incluye 24h estadía ($600)

2. **5.2 Buenos Aires → Córdoba (Carga Media)**
   - Contenedor 9: 1100kg, 5.5m³
   - Depósitos seleccionados automáticamente

3. **5.3 Rosario → Mendoza (Ruta Larga)**
   - Puede usar 1-2 depósitos intermedios
   - Cada depósito suma:
     - ⏱️ 24h de tiempo
     - 💰 Costo de estadía diaria

---

### 🔧 F. OPERACIONES DEL TRANSPORTISTA

1. **7.1 Ver Tramos Asignados**
   - Tramos listos para iniciar

2. **7.2 Ver Tramos en Progreso**
   - Tramos actualmente en viaje

3. **7.3 Validar Capacidad de Camión (RN1)**
   - Probar con carga que excede capacidad
   - ❌ Debe rechazar

---

## 📋 Datos Precargados para Pruebas

Ver archivo `DATOS_SEED.md` para detalles completos de:
- ✅ 3 Clientes
- ✅ 10 Contenedores (7 en uso, 3 disponibles)
- ✅ 7 Solicitudes (2 en tránsito, 1 programada, 3 entregadas, 1 borrador)
- ✅ 6 Rutas (2 en ejecución, 1 propuesta, 3 finalizadas)
- ✅ 3 Camiones (2 disponibles, 1 en uso)
- ✅ 4 Ubicaciones
- ✅ 3 Depósitos (con costos de estadía)

---

## 🔄 Reiniciar Datos de Prueba

```bash
# Opción 1: Reiniciar servicios (mantiene datos)
docker compose restart logistica-service seguimiento-service

# Opción 2: Recrear contenedores (recarga seed completo)
docker compose down logistica-service seguimiento-service
docker compose up -d --build logistica-service seguimiento-service
```

---

## 📊 Endpoints Principales

### Seguimiento Service
- `GET /api/solicitudes` - Listar solicitudes
- `POST /api/solicitudes` - Crear solicitud
- `GET /api/solicitudes/{id}/historial-estados` - Historial
- `GET /api/contenedores/cliente/{clienteId}` - Contenedores por cliente

### Logistica Service
- `POST /api/logistica/costos/rutas-tentativas` - Calcular rutas alternativas
- `GET /api/logistica/rutas/{id}/costo-real` - Costo real de ruta
- `GET /api/logistica/depositos/{id}/contenedores` - Contenedores en depósito
- `PATCH /api/logistica/tramos/{id}/iniciar` - Iniciar tramo
- `PATCH /api/logistica/tramos/{id}/finalizar` - Finalizar tramo

---

## ❓ Troubleshooting

### Los servicios no inician
```bash
docker compose logs -f logistica-service
docker compose logs -f seguimiento-service
```

### Token inválido o expirado
- Volver a ejecutar el login en Postman
- Los tokens expiran en 5 minutos

### No se encuentran datos
- Verificar que los servicios hayan iniciado completamente
- Esperar 10-15 segundos después de `docker compose up`

---

## 📚 Documentación Adicional

- `DATOS_SEED.md` - Detalles completos de datos precargados
- `FLUJOS_SISTEMA.md` - Diagramas de flujo del sistema
- `GUIA_KEYCLOAK_DOCKER.md` - Configuración detallada de Keycloak
- `POSTMAN_COLLECTION_V2.json` - Colección completa de endpoints

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
