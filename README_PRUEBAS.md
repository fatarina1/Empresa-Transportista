# 📦 Sistema de Transporte - Empresa Transportista

## 🎯 Sistema Completo Implementado

### ✅ Funcionalidades Principales

1. **Creación de Solicitudes con Cliente y Contenedor**
   - Clientes pueden crear solicitudes de transporte
   - Asociación de contenedores a solicitudes
   - Estados: BORRADOR → PROGRAMADA → EN_TRANSITO → ENTREGADA

2. **Asignación de Rutas mediante Rutas Alternativas**
   - Cálculo automático de 2 rutas alternativas:
     - Ruta directa (sin depósitos)
     - Ruta optimizada (con 1-2 depósitos seleccionados automáticamente)
   - Selección inteligente de depósitos por proximidad geográfica
   - **Incluye tiempo de estadía:** 24 horas por cada depósito
   - **Incluye costo de estadía:** Según `costoEstadiaDiaria` de cada depósito

3. **Asignación de Camión y Operatoria del Transportista**
   - Validación de capacidad de camión (RN1)
   - Asignación de camiones a tramos
   - Inicio y finalización de tramos
   - Control de estados de tramos

4. **Seguimiento de la Solicitud para el Cliente**
   - Consulta de estado de solicitudes
   - Historial completo de cambios de estado
   - Información de costos y tiempos estimados
   - Actualización automática de costos cuando se asigna ruta

5. **Cálculos de Costos y Tiempos**
   - **Costos estimados:** Calculados en rutas tentativas
   - **Costos reales:** Calculados al finalizar ruta (RF8)
   - **Bonificaciones:** Por llegar antes de lo estimado
   - **Penalizaciones:** Por retrasos
   - **Estadía:** Costo y tiempo por depósitos intermedios

---

## 🚀 Inicio Rápido

### 1. Levantar el Sistema

```bash
cd infra
docker compose up -d
```

### 2. Configurar Keycloak (Primera vez)

Ver `GUIA_KEYCLOAK_DOCKER.md` para crear:
- Realm: `tpi-backend`
- Cliente: `tpi-backend-client`
- Usuarios: `cliente01`, `admin01`, `transportista01`

### 3. Importar Colección de Postman

Importar: `POSTMAN_COLLECTION_V2.json`

### 4. Probar los Flujos

Seguir la guía en `INICIO_RAPIDO.md`

---

## 📁 Archivos Importantes

### 🧪 Para Pruebas

- **`POSTMAN_COLLECTION_V2.json`** - Colección completa y organizada de Postman con:
  - 7 carpetas de pruebas
  - Flujo completo de punta a punta
  - Todos los escenarios de RF3, RF5, RF8, RN1
  - Autenticación automática con tokens

- **`INICIO_RAPIDO.md`** - Guía paso a paso para probar TODO el sistema:
  - Cómo levantar los servicios
  - Cómo configurar Keycloak
  - Orden de ejecución de pruebas
  - Troubleshooting

- **`DATOS_SEED.md`** - Documentación completa de datos precargados:
  - 3 Clientes
  - 10 Contenedores (7 en uso, 3 disponibles para pruebas)
  - 7 Solicitudes en diferentes estados
  - 6 Rutas (2 en ejecución, 3 finalizadas, 1 propuesta)
  - 3 Camiones
  - 4 Ubicaciones
  - 3 Depósitos con costos de estadía

### 📚 Documentación Técnica

- **`FLUJOS_SISTEMA.md`** - Diagramas y explicación de flujos
- **`GUIA_KEYCLOAK_DOCKER.md`** - Configuración de autenticación
- **`README.md`** - Este archivo

---

## 🗂️ Estructura del Proyecto

```
EmpresaTransportistaTP/
├── api-gateway/          # Gateway con autenticación Keycloak
├── seguimiento-service/  # Solicitudes, Clientes, Contenedores
├── logistica-service/    # Rutas, Tramos, Camiones, Depósitos, Costos
├── geoapi/              # Cálculo de distancias y tiempos
├── infra/               # Docker Compose y configuración
│   └── docker-compose.yml
├── POSTMAN_COLLECTION_V2.json  # ⭐ Colección completa
├── INICIO_RAPIDO.md            # ⭐ Guía de pruebas
├── DATOS_SEED.md               # ⭐ Datos precargados
├── FLUJOS_SISTEMA.md
└── GUIA_KEYCLOAK_DOCKER.md
```

---

## 🧪 Casos de Prueba Cubiertos

### ✅ RF3 - Rutas Alternativas
- Cálculo de ruta directa vs ruta con depósitos
- Selección automática de depósitos por proximidad
- Incluye tiempo de estadía (24h por depósito)
- Incluye costo de estadía en cálculo total

### ✅ RF5 - Contenedores en Depósito
- Consultar contenedores esperando en cada depósito
- Ver tiempo de estadía acumulado
- Ver costo de estadía acumulado
- 2 casos precargados: Rosario (3 días) y Mendoza (7 días)

### ✅ RF8 - Costo Real
- Cálculo de costos reales al finalizar ruta
- Bonificaciones por eficiencia (llegar antes)
- Penalizaciones por demoras (llegar tarde)
- 3 casos precargados: eficiente, con demoras, perfecta

### ✅ RN1 - Validación de Capacidad
- Validar peso y volumen antes de asignar camión
- Rechazar si excede capacidad

### ✅ Flujo Completo
- Cliente crea solicitud → Admin calcula rutas → Admin asigna ruta
- → Transportista asigna camión → Transportista ejecuta → Cliente consulta
- Actualización automática de estados
- Actualización automática de costos y tiempos

---

## 📊 Datos de Seed Destacados

### Contenedores Disponibles para Nuevas Solicitudes
- **CONT-008-2024:** 450kg, 2.8m³ (Material de oficina)
- **CONT-009-2024:** 1100kg, 5.5m³ (Electrodomésticos)
- **CONT-010-2024:** 280kg, 1.5m³ (Herramientas)

### Rutas con Contenedores en Depósito (RF5)
- **Ruta 1:** Contenedor en Depósito Rosario hace 3 días
- **Ruta 2:** Contenedor en Depósito Mendoza hace 7 días (⚠️ prolongado)

### Rutas Finalizadas para Costo Real (RF8)
- **Ruta 4:** ⭐ Eficiente - Costo $48,500 (ahorro de $3,500)
- **Ruta 5:** ⚠️ Con demoras - Costo $108,500 (penalización de $13,500)
- **Ruta 6:** 🎯 Perfecta - Costo exacto $42,000

---

## 🔧 Tecnologías Utilizadas

- **Backend:** Spring Boot 3.2, Java 17
- **Seguridad:** Keycloak (OAuth2 + OpenID Connect)
- **API Gateway:** Spring Cloud Gateway
- **Base de datos:** PostgreSQL (2 instancias)
- **Cálculos geográficos:** GeoAPI (Node.js)
- **Containerización:** Docker + Docker Compose
- **Documentación:** OpenAPI 3.0 (Swagger)

---

## 📞 Endpoints Principales

### Gateway (http://localhost:8080)
- `POST /api/solicitudes` - Crear solicitud
- `POST /api/logistica/costos/rutas-tentativas` - Calcular rutas alternativas
- `GET /api/logistica/depositos/{id}/contenedores` - Contenedores en depósito
- `GET /api/logistica/rutas/{id}/costo-real` - Costo real de ruta
- `PATCH /api/logistica/tramos/{id}/iniciar` - Iniciar tramo
- `PATCH /api/logistica/tramos/{id}/finalizar` - Finalizar tramo

---

## 🎓 Notas Importantes

### Estadía en Depósitos
- **Tiempo estándar:** 24 horas por depósito
- **Costo:** Según `Deposito.costoEstadiaDiaria`
- **Cálculo en rutas tentativas:** Incluido en `tiempoTotalEstimado` y `costoTotalEstimado`
- **Cálculo en tramos:** Fechas incluyen demora de 24h entre tramos

### Selección Automática de Depósitos
- Algoritmo basado en proximidad geográfica
- Calcula punto medio de la ruta
- Selecciona depósito(s) más cercano(s) a ese punto
- Puede usar 0, 1 o 2 depósitos según distancia total

### Actualización de Costos
- Cuando se asigna ruta: llama a `PATCH /solicitudes/{id}/costos-estimados`
- Cuando se finaliza ruta: llama a `PATCH /solicitudes/{id}/costos`

---

## 🔄 Reiniciar Datos

```bash
# Recrear servicios con datos frescos
cd infra
docker compose down logistica-service seguimiento-service
docker compose up -d --build logistica-service seguimiento-service
```

---

## 📚 Para Más Información

1. **Probar el sistema:** Ver `INICIO_RAPIDO.md`
2. **Datos disponibles:** Ver `DATOS_SEED.md`
3. **Flujos del negocio:** Ver `FLUJOS_SISTEMA.md`
4. **Configurar Keycloak:** Ver `GUIA_KEYCLOAK_DOCKER.md`
5. **APIs disponibles:** Importar `POSTMAN_COLLECTION_V2.json`

---

**¡Listo para probar! 🚀**

Abre `INICIO_RAPIDO.md` y sigue la guía paso a paso.
