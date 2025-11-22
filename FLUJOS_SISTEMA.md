# 📋 Documentación de Flujos del Sistema de Transporte

## 📑 Índice
1. [Registro de Usuarios (Clientes) con Keycloak](#1-registro-de-usuarios-clientes-con-keycloak)
2. [Creación de Solicitudes con Cliente y Contenedor](#2-creación-de-solicitudes-con-cliente-y-contenedor)
3. [Asignación de Rutas mediante Rutas Alternativas](#3-asignación-de-rutas-mediante-rutas-alternativas)
4. [Asignación de Camión y Operatoria del Transportista](#4-asignación-de-camión-y-operatoria-del-transportista)
5. [Seguimiento de Solicitud para el Cliente](#5-seguimiento-de-solicitud-para-el-cliente)
6. [Cálculos de Costos y Tiempos](#6-cálculos-de-costos-y-tiempos)

---

## 1. Registro de Usuarios (Clientes) con Keycloak

### 🔐 Descripción
El sistema utiliza **Keycloak** como proveedor de identidad OAuth2/OIDC para autenticación y autorización centralizada.

### 🔄 Flujo de Autenticación

```
┌─────────┐         ┌─────────────┐         ┌──────────┐         ┌────────────┐
│ Cliente │ ─────>  │ API Gateway │ ─────>  │ Keycloak │ ─────>  │ Servicios  │
└─────────┘         └─────────────┘         └──────────┘         └────────────┘
     │                     │                       │                    │
     │  1. Login           │                       │                    │
     ├──────────────────> │                       │                    │
     │                     │  2. Redirect Auth     │                    │
     │                     ├─────────────────────> │                    │
     │                     │                       │                    │
     │  3. Credenciales    │                       │                    │
     ├───────────────────────────────────────────> │                    │
     │                     │                       │                    │
     │  4. JWT Token       │                       │                    │
     │ <─────────────────────────────────────────  │                    │
     │                     │                       │                    │
     │  5. Request + JWT   │                       │                    │
     ├──────────────────> │                       │                    │
     │                     │  6. Validate JWT      │                    │
     │                     ├─────────────────────> │                    │
     │                     │                       │                    │
     │                     │  7. Token OK          │                    │
     │                     │ <───────────────────  │                    │
     │                     │                       │                    │
     │                     │  8. Forward Request   │                    │
     │                     ├────────────────────────────────────────>  │
     │                     │                       │                    │
     │  9. Response        │                       │                    │
     │ <─────────────────────────────────────────────────────────────  │
```

### 📍 Endpoints

**Login/Obtener Token:**
```http
POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&
client_id=tpi-backend-client&
username=admin&
password=admin
```

**Respuesta:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer"
}
```

### 🔑 Configuración Keycloak

- **Realm:** `tpi-backend`
- **Client ID:** `tpi-backend-client`
- **Roles:** Configurables según necesidades del negocio
- **Puerto:** 8180 (local)

### 🛡️ Protección de Endpoints

Todos los endpoints del API Gateway validan automáticamente el JWT:
```
Authorization: Bearer {access_token}
```

---

## 2. Creación de Solicitudes con Cliente y Contenedor

### 📦 Descripción
El sistema permite crear solicitudes de transporte vinculando automáticamente clientes y contenedores. Si no existen, los crea en el mismo request.

### 🔄 Flujo de Creación

```
┌─────────┐         ┌─────────────────┐         ┌──────────────────┐
│ Cliente │         │ Seguimiento     │         │ Logística        │
│  (API)  │         │ Service         │         │ Service          │
└─────────┘         └─────────────────┘         └──────────────────┘
     │                      │                           │
     │  POST /solicitudes   │                           │
     ├────────────────────> │                           │
     │  (CrearSolicitud     │                           │
     │   Request)           │                           │
     │                      │                           │
     │                      │ 1. Verificar Cliente      │
     │                      ├─> existe? NO              │
     │                      │   → Crear Cliente         │
     │                      │                           │
     │                      │ 2. Verificar Contenedor   │
     │                      ├─> existe? NO              │
     │                      │   → Crear Contenedor      │
     │                      │                           │
     │                      │ 3. Verificar Ubicaciones  │
     │                      │   (HTTP)                  │
     │                      ├─────────────────────────> │
     │                      │                           │
     │                      │ 4. Crear/Obtener Ubics    │
     │                      │ <───────────────────────  │
     │                      │                           │
     │                      │ 5. Verificar Camiones     │
     │                      │   disponibles             │
     │                      ├─────────────────────────> │
     │                      │                           │
     │                      │ 6. Disponibilidad OK      │
     │                      │ <───────────────────────  │
     │                      │                           │
     │                      │ 7. Crear Solicitud        │
     │                      ├─> Estado: SOLICITADA      │
     │                      │   Generar Número          │
     │                      │                           │
     │  SolicitudDTO        │                           │
     │ <──────────────────  │                           │
```

### 📍 Endpoint

```http
POST http://localhost:8080/api/seguimiento/solicitudes
Authorization: Bearer {token}
Content-Type: application/json

{
  "cliente": {
    "nombre": "Juan Pérez",
    "email": "juan.perez@email.com",
    "telefono": "351-123-4567"
  },
  "contenedor": {
    "identificacion": "CONT-2024-001",
    "pesoKg": 500.00,
    "volumenM3": 2.5,
    "largoM": 2.0,
    "anchoM": 1.2,
    "altoM": 1.0,
    "descripcion": "Equipos electrónicos"
  },
  "ubicacionOrigen": {
    "direccion": "Av. Colón 1234, Córdoba",
    "latitud": -31.4167,
    "longitud": -64.1833
  },
  "ubicacionDestino": {
    "direccion": "Av. 9 de Julio 500, CABA",
    "latitud": -34.6037,
    "longitud": -58.3816
  }
}
```

### ✅ Validaciones Automáticas

1. **Cliente:**
   - Si existe el email → Reutiliza cliente existente
   - Si no existe → Crea nuevo cliente

2. **Contenedor:**
   - Valida peso y volumen
   - Genera identificación única si no se provee

3. **Ubicaciones:**
   - Valida coordenadas GPS
   - Crea ubicaciones en `logistica-service` si no existen

4. **Disponibilidad:**
   - Verifica que existan camiones con capacidad suficiente

### 📤 Respuesta Exitosa

```json
{
  "id": 1,
  "numero": "SOL-2024-001",
  "estado": "SOLICITADA",
  "clienteId": 1,
  "contenedorId": 1,
  "origenId": 1,
  "destinoId": 2,
  "costoEstimado": null,
  "tiempoEstimadoMin": null,
  "rutaId": null,
  "createdAt": "2024-11-22T10:00:00"
}
```

---

## 3. Asignación de Rutas mediante Rutas Alternativas

### 🗺️ Descripción
El sistema calcula múltiples rutas alternativas considerando depósitos intermedios y permite seleccionar la más conveniente.

### 🔄 Flujo de Asignación de Ruta

```
┌─────────┐         ┌──────────────────┐         ┌─────────────────┐
│ Usuario │         │ Logística        │         │ Seguimiento     │
│ Interno │         │ Service          │         │ Service         │
└─────────┘         └──────────────────┘         └─────────────────┘
     │                      │                            │
     │ 1. Calcular Rutas    │                            │
     │    Alternativas      │                            │
     ├────────────────────> │                            │
     │                      │                            │
     │                      │ 2. GeoAPI: Distancias      │
     │                      ├──> Origen → Destino        │
     │                      ├──> Origen → Depósito1 → Destino
     │                      ├──> Origen → Depósito2 → Destino
     │                      ├──> Origen → Dep1 → Dep2 → Destino
     │                      │                            │
     │                      │ 3. Calcular Costos/Tiempos │
     │                      │    para cada alternativa   │
     │                      │                            │
     │  Lista de Rutas      │                            │
     │  Tentativas          │                            │
     │ <──────────────────  │                            │
     │                      │                            │
     │ 4. Seleccionar Ruta  │                            │
     │    + solicitudId     │                            │
     ├────────────────────> │                            │
     │                      │                            │
     │                      │ 5. Crear Ruta Real         │
     │                      ├──> Estado: PROPUESTA       │
     │                      │    Crear Tramos            │
     │                      │                            │
     │                      │ 6. Asignar a Solicitud     │
     │                      │    (HTTP PATCH)            │
     │                      ├──────────────────────────> │
     │                      │                            │
     │                      │ 7. Update solicitud.rutaId │
     │                      │    Estado: EN_PLANIFICACION│
     │                      │ <────────────────────────  │
     │                      │                            │
     │  RutaDTO creada      │                            │
     │ <──────────────────  │                            │
```

### 📍 Endpoints

**Paso 1: Calcular Rutas Alternativas**

```http
POST http://localhost:8080/api/logistica/rutas/calcular-tentativa
Authorization: Bearer {token}
Content-Type: application/json

{
  "ubicacionOrigenId": 1,
  "ubicacionDestinoId": 2,
  "depositoIntermedioId": null,  // Opcional: null = todas las alternativas
  "pesoKg": 500.00,
  "volumenM3": 2.5
}
```

**Respuesta: Múltiples Alternativas**

```json
[
  {
    "tipoRuta": "DIRECTA",
    "cantidadTramos": 1,
    "cantidadDepositos": 0,
    "costoEstimado": 35000.00,
    "tiempoEstimadoMin": 420,
    "distanciaTotal": 700.0,
    "tramos": [
      {
        "orden": 1,
        "tipo": "ORIGEN_DESTINO",
        "origenLatitud": -31.4167,
        "origenLongitud": -64.1833,
        "destinoLatitud": -34.6037,
        "destinoLongitud": -58.3816,
        "distanciaKm": 700.0,
        "costoEstimado": 35000.00,
        "tiempoEstimadoMin": 420
      }
    ]
  },
  {
    "tipoRuta": "CON_1_DEPOSITO",
    "cantidadTramos": 2,
    "cantidadDepositos": 1,
    "costoEstimado": 38000.00,
    "tiempoEstimadoMin": 480,
    "distanciaTotal": 750.0,
    "tramos": [
      {
        "orden": 1,
        "tipo": "ORIGEN_DEPOSITO",
        "destinoDepositoId": 1,
        "distanciaKm": 350.0,
        "costoEstimado": 17500.00,
        "tiempoEstimadoMin": 210
      },
      {
        "orden": 2,
        "tipo": "DEPOSITO_DESTINO",
        "origenDepositoId": 1,
        "distanciaKm": 400.0,
        "costoEstimado": 20500.00,
        "tiempoEstimadoMin": 270
      }
    ]
  }
]
```

**Paso 2: Crear Ruta Real con Tramos**

```http
POST http://localhost:8080/api/logistica/rutas/crear-con-tramos
Authorization: Bearer {token}
Content-Type: application/json

{
  "solicitudId": 1,
  "rutaTentativa": {
    // Copiar la ruta seleccionada del paso anterior
    "tipoRuta": "DIRECTA",
    "cantidadTramos": 1,
    "cantidadDepositos": 0,
    "costoEstimado": 35000.00,
    "tiempoEstimadoMin": 420,
    "distanciaTotal": 700.0,
    "tramos": [ /* ... */ ]
  },
  "observaciones": "Ruta directa más económica"
}
```

### 🎯 Lógica de Generación de Alternativas

**Sin depositoIntermedioId (todas las combinaciones):**
1. Ruta directa (0 depósitos)
2. Rutas con 1 depósito intermedio (N alternativas)
3. Rutas con 2 depósitos intermedios (N×M alternativas)

**Con depositoIntermedioId específico:**
1. Ruta directa (sin depósito)
2. Ruta pasando por el depósito especificado

### 💰 Cálculo de Costos

- **Costo base por km:** $50/km
- **Parada en depósito:** +$500 por depósito
- **Costo total = Σ(distancia_tramo × costo_km) + (num_depositos × 500)**

---

## 4. Asignación de Camión y Operatoria del Transportista

### 🚚 Descripción
Los transportistas asignan camiones a tramos específicos y gestionan el ciclo de vida del transporte (iniciar, finalizar).

### 🔄 Flujo de Operatoria

```
┌──────────────┐         ┌──────────────────┐         ┌─────────────────┐
│ Transportista│         │ Logística        │         │ Seguimiento     │
│   (Usuario)  │         │ Service          │         │ Service         │
└──────────────┘         └──────────────────┘         └─────────────────┘
     │                           │                            │
     │ 1. Asignar Camión         │                            │
     │    a Tramo                │                            │
     ├─────────────────────────> │                            │
     │                           │                            │
     │                           │ Validaciones:              │
     │                           ├─> Camión disponible?       │
     │                           ├─> Capacidad suficiente?    │
     │                           ├─> Tramo no INICIADO?       │
     │                           │                            │
     │  Asignación OK            │                            │
     │  Estado: ASIGNADO         │                            │
     │ <───────────────────────  │                            │
     │                           │                            │
     │ 2. Iniciar Tramo          │                            │
     ├─────────────────────────> │                            │
     │                           │                            │
     │                           │ 3. Update Estado           │
     │                           ├──> ASIGNADO → INICIADO     │
     │                           │    fechaInicioReal = NOW   │
     │                           │    camion.disponible=false │
     │                           │                            │
     │                           │ 4. Notificar Cambio Estado │
     │                           │    (si es 1er tramo)       │
     │                           ├──────────────────────────> │
     │                           │                            │
     │                           │ 5. Solicitud → EN_TRANSITO │
     │                           │ <────────────────────────  │
     │                           │                            │
     │  Inicio Confirmado        │                            │
     │ <───────────────────────  │                            │
     │                           │                            │
     │ 3. Finalizar Tramo        │                            │
     ├─────────────────────────> │                            │
     │                           │                            │
     │                           │ 6. Update Estado           │
     │                           ├──> INICIADO → FINALIZADO   │
     │                           │    fechaFinReal = NOW      │
     │                           │    camion.disponible=true  │
     │                           │    Calcular costo real     │
     │                           │                            │
     │                           │ 7. ¿Último tramo?          │
     │                           ├──> SÍ: Ruta FINALIZADA     │
     │                           │                            │
     │                           │ 8. Notificar Finalización  │
     │                           ├──────────────────────────> │
     │                           │                            │
     │                           │ 9. Solicitud → ENTREGADA   │
     │                           │    Copiar costos/tiempos   │
     │                           │ <────────────────────────  │
     │                           │                            │
     │  Finalización OK          │                            │
     │ <───────────────────────  │                            │
```

### 📍 Endpoints

**Asignar Camión a Tramo**

```http
PATCH http://localhost:8080/api/logistica/tramos/{tramoId}/asignar-camion
Authorization: Bearer {token}
Content-Type: application/json

{
  "camionId": 1
}
```

**Validaciones:**
- ✅ Camión debe estar disponible
- ✅ Camión debe tener capacidad suficiente
- ✅ Tramo no debe estar INICIADO o FINALIZADO
- ❌ Si el tramo ya inició, **no permite reasignación**

**Iniciar Tramo**

```http
PATCH http://localhost:8080/api/logistica/tramos/{tramoId}/iniciar
Authorization: Bearer {token}
```

**Efectos:**
- Estado: `ASIGNADO` → `INICIADO`
- `fechaInicioReal` = timestamp actual
- Camión: `disponible = false`
- Si es el primer tramo → Solicitud: `SOLICITADA` → `EN_TRANSITO`

**Finalizar Tramo**

```http
PATCH http://localhost:8080/api/logistica/tramos/{tramoId}/finalizar
Authorization: Bearer {token}
```

**Efectos:**
- Estado: `INICIADO` → `FINALIZADO`
- `fechaFinReal` = timestamp actual
- Calcula `costoReal` basado en tiempo/distancia real
- Camión: `disponible = true`
- Si es el último tramo → Solicitud: `EN_TRANSITO` → `ENTREGADA`

### 🔒 Reglas de Negocio

1. **Secuencialidad:** Los tramos deben iniciarse/finalizarse en orden
2. **No Reasignación:** No se puede cambiar el camión de un tramo ya iniciado
3. **Disponibilidad:** El camión queda bloqueado durante el transporte
4. **Cálculo Real:** El costo real se calcula al finalizar usando tiempos reales

---

## 5. Seguimiento de Solicitud para el Cliente

### 📱 Descripción
Los clientes pueden consultar el estado en tiempo real de sus solicitudes, incluyendo ubicación actual y progreso de tramos.

### 🔄 Flujo de Consulta

```
┌─────────┐         ┌─────────────────┐         ┌──────────────────┐
│ Cliente │         │ Seguimiento     │         │ Logística        │
│  (APP)  │         │ Service         │         │ Service          │
└─────────┘         └─────────────────┘         └──────────────────┘
     │                      │                           │
     │ GET /solicitudes/    │                           │
     │     {id}/detalle     │                           │
     ├────────────────────> │                           │
     │                      │                           │
     │                      │ 1. Obtener Solicitud      │
     │                      ├──> + Cliente              │
     │                      │   + Contenedor            │
     │                      │                           │
     │                      │ 2. Obtener Ubicaciones    │
     │                      │    (HTTP GET)             │
     │                      ├─────────────────────────> │
     │                      │                           │
     │                      │ 3. Ubicaciones            │
     │                      │ <───────────────────────  │
     │                      │                           │
     │                      │ 4. ¿Tiene rutaId?         │
     │                      ├──> SÍ: Obtener Tracking   │
     │                      │    (HTTP GET)             │
     │                      ├─────────────────────────> │
     │                      │                           │
     │                      │ 5. Tracking Info          │
     │                      │    - Estado ruta          │
     │                      │    - Ubicación actual     │
     │                      │    - Lista tramos c/estado│
     │                      │ <───────────────────────  │
     │                      │                           │
     │  SolicitudDetalleDTO │                           │
     │  + tracking          │                           │
     │ <──────────────────  │                           │
```

### 📍 Endpoint Principal

```http
GET http://localhost:8080/api/seguimiento/solicitudes/{id}/detalle
Authorization: Bearer {token}
```

### 📤 Respuesta Completa con Tracking

```json
{
  "id": 1,
  "numero": "SOL-2024-001",
  "estado": "EN_TRANSITO",
  "cliente": {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan.perez@email.com",
    "telefono": "351-123-4567"
  },
  "contenedor": {
    "id": 1,
    "identificacion": "CONT-001-2024",
    "pesoKg": 500.00,
    "volumenM3": 2.5,
    "largoM": 2.0,
    "anchoM": 1.2,
    "altoM": 1.0,
    "descripcion": "Equipos electrónicos",
    "clienteId": 1,
    "createdAt": "2024-11-20T10:00:00"
  },
  "origen": {
    "id": 1,
    "nombre": "Córdoba Capital",
    "direccion": "Av. Colón 1234, Córdoba",
    "latitud": -31.4167,
    "longitud": -64.1833,
    "tipo": "CLIENTE"
  },
  "destino": {
    "id": 2,
    "nombre": "Buenos Aires - Retiro",
    "direccion": "Av. 9 de Julio 500, CABA",
    "latitud": -34.6037,
    "longitud": -58.3816,
    "tipo": "CLIENTE"
  },
  "costoEstimado": 35000.00,
  "tiempoEstimadoMin": 420,
  "costoFinal": null,
  "tiempoRealMin": null,
  "tracking": {
    "rutaId": 1,
    "estadoRuta": "EN_EJECUCION",
    "ubicacionActual": {
      "id": 3,
      "nombre": "Depósito Rosario",
      "direccion": "Ruta 9 Km 305, Rosario",
      "latitud": -32.9442,
      "longitud": -60.6505,
      "tipo": "DEPOSITO"
    },
    "tramos": [
      {
        "id": 1,
        "rutaId": 1,
        "orden": 1,
        "tipo": "ORIGEN_DEPOSITO",
        "estado": "FINALIZADO",
        "origenLatitud": -31.4167,
        "origenLongitud": -64.1833,
        "destinoDepositoId": 3,
        "destinoLatitud": -32.9442,
        "destinoLongitud": -60.6505,
        "distanciaKm": 350.0,
        "costoAproximado": 17500.0,
        "costoReal": 16800.0,
        "fechaInicioEstimada": "2024-11-22T08:00:00",
        "fechaFinEstimada": "2024-11-22T14:00:00",
        "fechaInicioReal": "2024-11-22T08:15:00",
        "fechaFinReal": "2024-11-22T13:45:00",
        "camionId": 1
      },
      {
        "id": 2,
        "rutaId": 1,
        "orden": 2,
        "tipo": "DEPOSITO_DESTINO",
        "estado": "INICIADO",
        "origenDepositoId": 3,
        "origenLatitud": -32.9442,
        "origenLongitud": -60.6505,
        "destinoLatitud": -34.6037,
        "destinoLongitud": -58.3816,
        "distanciaKm": 400.0,
        "costoAproximado": 20500.0,
        "costoReal": null,
        "fechaInicioEstimada": "2024-11-22T15:00:00",
        "fechaFinEstimada": "2024-11-22T21:00:00",
        "fechaInicioReal": "2024-11-22T14:30:00",
        "fechaFinReal": null,
        "camionId": 2
      }
    ]
  }
}
```

### 🎯 Información de Tracking

**Estado de la Ruta:**
- `PROPUESTA`: Ruta creada, sin tramos iniciados
- `EN_EJECUCION`: Al menos un tramo iniciado
- `FINALIZADA`: Todos los tramos finalizados

**Ubicación Actual:**
- Si hay tramo `INICIADO` → Origen del tramo en curso
- Si todos `FINALIZADOS` → Destino del último tramo
- Si ninguno iniciado → Origen del primer tramo

**Estados de Tramos:**
- `PROGRAMADO`: Tramo creado sin camión
- `ASIGNADO`: Camión asignado, no iniciado
- `INICIADO`: En tránsito
- `FINALIZADO`: Completado

### 📊 Endpoint Alternativo (Solo Tracking)

```http
GET http://localhost:8080/api/logistica/rutas/{rutaId}/tracking
Authorization: Bearer {token}
```

Devuelve solo la información de tracking sin datos de solicitud/cliente.

---

## 6. Cálculos de Costos y Tiempos

### 💰 Descripción
El sistema calcula automáticamente costos estimados y reales, así como tiempos de viaje considerando distancias y paradas en depósitos.

### 🔄 Flujo de Cálculos

```
┌────────────────┐         ┌──────────────────┐         ┌─────────────┐
│ CostoService   │         │ GeoAPI           │         │ Entidades   │
└────────────────┘         └──────────────────┘         └─────────────┘
        │                          │                          │
        │ 1. Calcular Ruta         │                          │
        │    Tentativa             │                          │
        │                          │                          │
        │ 2. Para cada alternativa:│                          │
        ├────────────────────────> │                          │
        │    GET /distancia        │                          │
        │    ?origen=...&destino=  │                          │
        │                          │                          │
        │ 3. Distancia (km)        │                          │
        │ <──────────────────────  │                          │
        │                          │                          │
        │ 4. Calcular Costo:       │                          │
        │    - Base = dist × 50    │                          │
        │    - Paradas = n × 500   │                          │
        │    - Total = Base+Paradas│                          │
        │                          │                          │
        │ 5. Calcular Tiempo:      │                          │
        │    - Velocidad = 100km/h │                          │
        │    - Paradas = 30min c/u │                          │
        │    - Total = (dist/vel)  │                          │
        │      × 60 + (n × 30)     │                          │
        │                          │                          │
        │ 6. Crear RutaTentativa   │                          │
        │    con costos/tiempos    │                          │
        ├────────────────────────────────────────────────────>│
        │                          │                          │
        │                          │      Al Finalizar Tramo: │
        │                          │                          │
        │ 7. Calcular Costo Real:  │                          │
        │    - Tiempo real (min)   │                          │
        │    - Factor ajuste       │                          │
        │    - Real = Estimado     │                          │
        │      × (1 + ajuste)      │                          │
        ├────────────────────────────────────────────────────>│
        │                          │        costoReal = X     │
```

### 📊 Fórmulas de Cálculo

#### **Costo Estimado**

```
COSTO_BASE_KM = 50 pesos/km
COSTO_PARADA_DEPOSITO = 500 pesos

Para un tramo:
  costo_tramo = distancia_km × COSTO_BASE_KM

Para una ruta completa:
  costo_ruta = Σ(costo_tramo) + (num_depositos × COSTO_PARADA_DEPOSITO)
```

**Ejemplo:**
- Ruta: Córdoba → Depósito Rosario (350km) → CABA (400km)
- Costo tramo 1: 350 × 50 = 17,500
- Costo tramo 2: 400 × 50 = 20,000
- Parada depósito: 1 × 500 = 500
- **Total: 38,000 pesos**

#### **Tiempo Estimado**

```
VELOCIDAD_PROMEDIO = 100 km/h
TIEMPO_PARADA_DEPOSITO = 30 minutos

Para un tramo:
  tiempo_tramo = (distancia_km / VELOCIDAD_PROMEDIO) × 60

Para una ruta completa:
  tiempo_ruta = Σ(tiempo_tramo) + (num_depositos × TIEMPO_PARADA_DEPOSITO)
```

**Ejemplo:**
- Tramo 1: (350 / 100) × 60 = 210 min
- Tramo 2: (400 / 100) × 60 = 240 min
- Parada: 1 × 30 = 30 min
- **Total: 480 minutos (8 horas)**

#### **Costo Real (al finalizar)**

```
tiempo_real_min = fechaFinReal - fechaInicioReal
tiempo_estimado_min = fechaFinEstimada - fechaInicioEstimada

factor_ajuste = (tiempo_real - tiempo_estimado) / tiempo_estimado

costo_real = costo_estimado × (1 + (factor_ajuste × 0.1))
```

**Ejemplo:**
- Costo estimado: 17,500
- Tiempo estimado: 210 min
- Tiempo real: 195 min
- Factor ajuste: (195 - 210) / 210 = -0.071
- **Costo real: 17,500 × (1 - 0.0071) = 17,375**

### 📍 Endpoints de Costo

**Costo Estimado (al crear ruta):**
```http
GET http://localhost:8080/api/logistica/rutas/{id}
```
Respuesta incluye: `costoEstimado`, `tiempoEstimadoMin`

**Costo Real (al finalizar ruta):**
```http
GET http://localhost:8080/api/logistica/rutas/{id}/costo-real
```

```json
{
  "rutaId": 1,
  "costoEstimado": 38000.00,
  "costoReal": 36500.00,
  "tiempoEstimadoMin": 480,
  "tiempoRealMin": 455,
  "ahorro": 1500.00,
  "eficiencia": 94.7
}
```

### 💾 Persistencia de Costos

**Al crear solicitud:**
- Se guarda `costoEstimado` y `tiempoEstimadoMin` en la solicitud

**Al finalizar último tramo:**
- Se calcula y guarda `costoFinal` y `tiempoRealMin` en la solicitud
- Se actualiza el estado a `ENTREGADA`

### 🧮 Tiempo en Depósito

Para calcular cuánto tiempo estuvo un contenedor en un depósito:

```
Tramo llegada al depósito: fechaFinReal
Tramo salida del depósito: fechaInicioReal

tiempo_en_deposito = salida.fechaInicioReal - llegada.fechaFinReal
```

**Endpoint:**
```http
GET http://localhost:8080/api/logistica/depositos/{id}/contenedores?diasMinimo=3
```

Devuelve contenedores en el depósito con al menos N días de estadía.

---

## 📚 Resumen de Arquitectura

### 🏗️ Microservicios

1. **seguimiento-service** (Puerto 8081)
   - Gestión de solicitudes
   - Gestión de clientes y contenedores
   - Estados de solicitud

2. **logistica-service** (Puerto 8082)
   - Gestión de rutas y tramos
   - Gestión de camiones
   - Cálculos de costos y tiempos
   - Operatoria de transportistas

3. **api-gateway** (Puerto 8080)
   - Punto de entrada único
   - Autenticación JWT con Keycloak
   - Enrutamiento de requests

4. **geoapi** (Puerto 8083)
   - Cálculos de distancias GPS
   - Servicio externo simulado

5. **keycloak** (Puerto 8180)
   - Autenticación y autorización
   - Gestión de usuarios y roles

### 🔗 Comunicación Entre Servicios

- **API Gateway → Servicios:** Gateway HTTP (Spring Cloud Gateway)
- **Seguimiento ↔ Logística:** HTTP REST (RestClient)
- **Logística → GeoAPI:** HTTP REST
- **Todos → Keycloak:** OAuth2/OIDC (JWT validation)

### 💾 Bases de Datos

- **H2 In-Memory** (desarrollo)
- Una BD independiente por microservicio
- DataSeeders para datos de prueba

### 🔐 Seguridad

- Todos los endpoints requieren JWT Bearer token
- Tokens obtenidos desde Keycloak
- Validación automática en API Gateway

---

## 🚀 Guía Rápida de Uso

### 1️⃣ Autenticación
```bash
curl -X POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=tpi-backend-client&username=admin&password=admin"
```

### 2️⃣ Crear Solicitud
```bash
curl -X POST http://localhost:8080/api/seguimiento/solicitudes \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### 3️⃣ Calcular Rutas
```bash
curl -X POST http://localhost:8080/api/logistica/rutas/calcular-tentativa \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{...}'
```

### 4️⃣ Crear Ruta Real
```bash
curl -X POST http://localhost:8080/api/logistica/rutas/crear-con-tramos \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{...}'
```

### 5️⃣ Asignar Camión
```bash
curl -X PATCH http://localhost:8080/api/logistica/tramos/{id}/asignar-camion \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{"camionId": 1}'
```

### 6️⃣ Iniciar/Finalizar Tramo
```bash
curl -X PATCH http://localhost:8080/api/logistica/tramos/{id}/iniciar \
  -H "Authorization: Bearer {TOKEN}"

curl -X PATCH http://localhost:8080/api/logistica/tramos/{id}/finalizar \
  -H "Authorization: Bearer {TOKEN}"
```

### 7️⃣ Consultar Seguimiento
```bash
curl http://localhost:8080/api/seguimiento/solicitudes/{id}/detalle \
  -H "Authorization: Bearer {TOKEN}"
```

---

## 📞 Soporte

Para más detalles técnicos, consultar:
- `README.md`: Instrucciones de instalación y ejecución
- `INICIO_RAPIDO.md`: Guía rápida de inicio
- `DATOS_SEED.md`: Datos de prueba disponibles
- `POSTMAN_COLLECTION.json`: Colección de requests de ejemplo
