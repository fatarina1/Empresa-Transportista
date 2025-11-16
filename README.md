# 🚀 TPI Backend - Empresa Transportista

Sistema de gestión de transporte de contenedores con microservicios, Keycloak y API Gateway.

---

## 📚 Documentación

| Archivo | Descripción |
|---------|-------------|
| **[INICIO_RAPIDO.md](INICIO_RAPIDO.md)** | ⭐ Comandos para levantar TODO el sistema |
| **[DATOS_SEED.md](DATOS_SEED.md)** | 🌱 Datos precargados para pruebas |
| **[POSTMAN_COLLECTION.json](POSTMAN_COLLECTION.json)** | 🧪 Colección completa de endpoints |

---

## 🎯 Inicio Rápido

### 1. Levantar el sistema

```bash
cd infra
docker compose up -d
```

### 2. Usuarios de prueba

| Usuario | Password | Rol |
|---------|----------|-----|
| admin01 | admin123 | ADMIN |
| cliente01 | cliente123 | CLIENTE |
| transp01 | transp123 | TRANSPORTISTA |

### 3. Importar colección en Postman

1. Abrir Postman
2. Import → `POSTMAN_COLLECTION.json`
3. Ejecutar login según el rol que necesites

---

## 🏗️ Arquitectura

| Servicio | Puerto | Swagger UI | Descripción |
|----------|--------|------------|-------------|
| **API Gateway** | 8080 | - | Punto de entrada único + autenticación |
| **Keycloak** | 8180 | - | Gestión de usuarios y autenticación |
| **Seguimiento Service** | 8081 | [/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | Clientes, solicitudes, contenedores |
| **Logística Service** | 8082 | [/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) | Rutas, tramos, camiones, depósitos |
| **GeoAPI** | 8083 | [/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) | Cálculo de distancias |

### Flujo de Datos

```
Cliente (Postman)
    ↓
API Gateway :8080 → Keycloak :8180 (validar token)
    ↓
Microservicios :8081/:8082/:8083
    ↓
Base de datos H2 (en memoria)
```

---

## 📊 Datos Precargados

El sistema viene con **7 solicitudes**, **6 rutas** y **3 rutas completadas** listas para probar. Ver [DATOS_SEED.md](DATOS_SEED.md) para detalles completos.

### Ejemplos listos para usar:

**✅ Rutas finalizadas (probar costo real):**
```bash
GET /api/logistica/rutas/4/costo-real  # Ruta eficiente
GET /api/logistica/rutas/5/costo-real  # Con estadía en depósito
GET /api/logistica/rutas/6/costo-real  # Ruta perfecta
```

**🚚 Contenedores en depósitos:**
```bash
GET /api/logistica/depositos/3/contenedores  # Rosario (3 días)
GET /api/logistica/depositos/2/contenedores  # Mendoza (7 días)
```

---

## 🧪 Testing con Postman

1. **Importar colección:** `POSTMAN_COLLECTION.json`
2. **Login:** Ejecutar request según rol (ADMIN/CLIENTE/TRANSPORTISTA)
3. **Probar endpoints:** Token se guarda automáticamente

**💡 Alternativa: Usar Swagger UI**
- Seguimiento: http://localhost:8081/swagger-ui/index.html
- Logística: http://localhost:8082/swagger-ui/index.html
- GeoAPI: http://localhost:8083/swagger-ui/index.html

---

## 🛠️ Comandos Útiles

### Ver logs centralizados
```bash
cd infra
docker compose logs -f                    # Todos los servicios
docker compose logs -f logistica-service  # Un servicio específico
docker compose logs | grep ERROR          # Solo errores
```

### Reiniciar un servicio
```bash
cd infra
docker compose restart logistica-service
```

### Detener todo
```bash
cd infra
docker compose down
```

---

## 📝 Requisitos Funcionales Implementados

- ✅ **RF1:** Crear solicitud de traslado (Cliente)
- ✅ **RF2:** Consultar estado de solicitud (Cliente)  
- ✅ **RF3:** Calcular ruta tentativa con costos (Admin)
- ✅ **RF4:** Programar ruta para solicitud (Admin)
- ✅ **RF5:** Consultar contenedores en depósito (Admin)
- ✅ **RF6:** Asignar camión a tramo (validación de capacidad)
- ✅ **RF7:** Iniciar y finalizar tramos (Transportista)
- ✅ **RF8:** Calcular costo real automático (Admin)
- ✅ **RN1:** Validación de capacidad de camión

---

## 📂 Estructura del Proyecto

```
EmpresaTransportistaTP/
├── api-gateway/           # Gateway + seguridad Keycloak
├── seguimiento-service/   # Clientes, solicitudes, contenedores
├── logistica-service/     # Rutas, tramos, camiones, depósitos
├── geoapi/               # Cálculo de distancias
├── infra/                # Docker compose
├── README.md             # Este archivo
├── INICIO_RAPIDO.md      # Guía de inicio
├── DATOS_SEED.md         # Datos precargados
└── POSTMAN_COLLECTION.json
```

---

## 🚀 Tecnologías

- **Backend:** Spring Boot 3.5.6, Java 21
- **Seguridad:** Keycloak 24.0.3, OAuth2 + JWT
- **Gateway:** Spring Cloud Gateway
- **Base de datos:** H2 (en memoria)
- **Contenedores:** Docker + Docker Compose
- **Testing:** Postman

├── geoapi/                # Servicio de cálculo de distancias
├── logistica-service/     # Gestión de rutas, tramos, camiones
├── seguimiento-service/   # Gestión de solicitudes y contenedores
├── infra/                 # Docker Compose y configuración
│   ├── docker-compose.yml
│   └── docker-compose.override.yml
├── INICIO_RAPIDO.md       ⭐ EMPEZAR AQUÍ
├── FLUJOS_PRUEBA_POR_ROL.md  ⭐ GUÍA DE PRUEBAS
├── POSTMAN_COLLECTION.json   ⭐ IMPORTAR EN POSTMAN
└── README.md              ← Estás aquí
```

---

**Versión:** 1.0 Final (16 Nov 2025)  
**Estado:** ✅ Sistema completo y funcional
