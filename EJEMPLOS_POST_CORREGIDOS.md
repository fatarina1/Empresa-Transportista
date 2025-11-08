# ✅ Ejemplos POST Corregidos - Guía Definitiva

Este documento explica los ejemplos **correctos** para las peticiones POST que no funcionaban.

## 🔧 Problemas Identificados y Solucionados

### Problema 1: POST Tramos
**❌ Error original:** Faltaban campos requeridos (`origenDepositoId`, `destinoDepositoId`)
**✅ Solución:** Usar `TramoDTO` completo con todos los campos necesarios

### Problema 2: POST Solicitudes - Error crítico en el servicio
**❌ Error original:** El `SolicitudService` NO estaba estableciendo los campos `origenDireccion`, `origenLatitud`, `origenLongitud`, `destinoDireccion`, `destinoLatitud`, `destinoLongitud` que son **nullable=false** en la entidad `Solicitud`.
**✅ Solución:** Modifiqué `SolicitudService.crearSolicitud()` para obtener automáticamente direcciones y coordenadas de logística-service

### Problema 3: URLs incorrectas en LogisticaServiceClient
**❌ Error original:** El cliente usaba `/api/ubicaciones/{id}` y `/api/camiones/verificar-disponibilidad` pero los controllers solo exponen `/ubicaciones/{id}` y `/camiones/verificar-disponibilidad`
**✅ Solución:** Corregir las URIs en `LogisticaServiceClient` eliminando el prefijo `/api`

---

## 📝 POST Tramo - Ejemplo Correcto

### Endpoint
```http
POST http://localhost:8082/tramos
Content-Type: application/json
```

### Body JSON (Opción 1: Solo coordenadas)
```json
{
    "rutaId": 1,
    "orden": 2,
    "origenDepositoId": 1,
    "destinoDepositoId": 2,
    "origenLatitud": -31.4167,
    "origenLongitud": -64.1833,
    "destinoLatitud": -32.8908,
    "destinoLongitud": -68.8272,
    "tipo": "DEPOSITO_DEPOSITO"
}
```

### Body JSON (Opción 2: Sin depósitos)
```json
{
    "rutaId": 1,
    "orden": 3,
    "origenLatitud": -32.8908,
    "origenLongitud": -68.8272,
    "destinoLatitud": -33.7167,
    "destinoLongitud": -61.9667,
    "tipo": "ORIGEN_DESTINO"
}
```

### Campos Requeridos
- ✅ `rutaId` (Long): ID de la ruta a la que pertenece el tramo
- ✅ `orden` (Integer): Orden del tramo en la ruta (1, 2, 3...)
- ✅ `tipo` (String): Tipo de tramo
  - `"ORIGEN_DESTINO"` - Tramo directo sin depósitos
  - `"DEPOSITO_DEPOSITO"` - Tramo entre dos depósitos
  - `"DEPOSITO_DESTINO"` - Del depósito al destino final

### Campos Opcionales
- `origenDepositoId` (Long): ID del depósito origen (solo si tipo es DEPOSITO_*)
- `destinoDepositoId` (Long): ID del depósito destino (solo si tipo es *_DEPOSITO)
- `origenLatitud` (Double): Coordenada latitud origen
- `origenLongitud` (Double): Coordenada longitud origen
- `destinoLatitud` (Double): Coordenada latitud destino
- `destinoLongitud` (Double): Coordenada longitud destino

### ¿Qué sucede automáticamente?
1. El servicio valida que `rutaId` exista
2. Si hay coordenadas completas (origen y destino), llama a **GeoAPI** usando **RestClient**
3. GeoAPI calcula la distancia real usando Google Maps
4. El tramo se guarda con `distanciaKm` calculado automáticamente
5. El estado inicial es `"ESTIMADO"`

### Respuesta esperada
```json
{
    "id": 2,
    "rutaId": 1,
    "orden": 2,
    "origenDepositoId": 1,
    "destinoDepositoId": 2,
    "origenLatitud": -31.4167,
    "origenLongitud": -64.1833,
    "destinoLatitud": -32.8908,
    "destinoLongitud": -68.8272,
    "tipo": "DEPOSITO_DEPOSITO",
    "estado": "ESTIMADO",
    "distanciaKm": 608.353,
    "costoAproximado": null,
    "costoReal": null,
    "fechaInicioEstimada": null,
    "fechaFinEstimada": null,
    "fechaInicioReal": null,
    "fechaFinReal": null,
    "camionId": null,
    "createdAt": "2025-01-15T10:30:00"
}
```

---

## 📦 POST Solicitud - Ejemplos Corregidos

### Endpoint
```http
POST http://localhost:8081/solicitudes
Content-Type: application/json
```

### Opción 1: Con Cliente y Contenedor Existentes ✅ RECOMENDADO
```json
{
    "clienteId": 1,
    "contenedorId": 1,
    "origenId": 1,
    "destinoId": 2
}
```

**Ventajas:**
- ✅ Mínimo de campos requeridos
- ✅ Usa datos ya cargados por los seeders
- ✅ Más rápido y simple

**Clientes disponibles en seeders:**
- ID 1: Juan Pérez
- ID 2: María González
- ID 3: Empresa LogiTrans

**Contenedores disponibles en seeders:**
- ID 1: CONT-001-2024 (Cliente 1)
- ID 2: CONT-002-2024 (Cliente 2)
- ID 3: CONT-003-2024 (Cliente 3)

**Ubicaciones disponibles en logística-service:**
- ID 1: Córdoba Capital
- ID 2: Mendoza
- ID 3: Rosario
- ID 4: Buenos Aires

### Opción 2: Crear Nuevo Cliente Y Nuevo Contenedor
```json
{
    "origenId": 1,
    "destinoId": 3,
    "clienteNombre": "Roberto Gómez Bolaños",
    "clienteEmail": "roberto.gomez@email.com",
    "clienteTelefono": "351-777-8888",
    "clienteDocumento": "27654321",
    "clienteDireccion": "Independencia 850, Córdoba",
    "contenedorIdentificacion": "CONT-015-2025",
    "contenedorPesoKg": 725.50,
    "contenedorVolumenM3": 4.8,
    "contenedorLargoM": 2.2,
    "contenedorAnchoM": 1.8,
    "contenedorAltoM": 1.2,
    "contenedorDescripcion": "Productos alimenticios envasados"
}
```

**Campos Siempre Requeridos:**
- ✅ `origenId` (Long): ID de ubicación origen (debe existir en logística-service)
- ✅ `destinoId` (Long): ID de ubicación destino (debe existir en logística-service)

**Campos para Cliente Nuevo (si NO usas `clienteId`):**
- ✅ `clienteNombre` (String)
- ✅ `clienteEmail` (String)
- `clienteTelefono` (String)
- `clienteDocumento` (String)
- `clienteDireccion` (String)

**Campos para Contenedor Nuevo (si NO usas `contenedorId`):**
- ✅ `contenedorIdentificacion` (String)
- ✅ `contenedorPesoKg` (BigDecimal)
- ✅ `contenedorVolumenM3` (BigDecimal)
- `contenedorLargoM` (BigDecimal)
- `contenedorAnchoM` (BigDecimal)
- `contenedorAltoM` (BigDecimal)
- `contenedorDescripcion` (String)

### ¿Qué sucede automáticamente? 🔄
1. Si no hay `clienteId`, crea un nuevo Cliente con los datos proporcionados
2. Si no hay `contenedorId`, crea un nuevo Contenedor (asociado al cliente)
3. **Llama a `logistica-service`** usando **RestClient** para obtener:
   - Ubicación origen (dirección + coordenadas)
   - Ubicación destino (dirección + coordenadas)
4. Verifica disponibilidad de camiones en logística
5. Crea la solicitud con:
   - Número auto-generado (formato: SOL-XXXXXXXX)
   - Estado inicial: `"BORRADOR"`
   - **Dirección y coordenadas del origen** (obtenidas de logística)
   - **Dirección y coordenadas del destino** (obtenidas de logística)
   - Fecha de solicitud: ahora

### Respuesta esperada
```json
{
    "id": 4,
    "numero": "SOL-A3F2B9C1",
    "clienteId": 4,
    "contenedorId": 4,
    "origenId": 1,
    "destinoId": 3,
    "estado": "BORRADOR",
    "costoEstimado": null,
    "tiempoEstimadoMin": null,
    "costoFinal": null,
    "tiempoRealMin": null,
    "fechaSolicitud": "2025-01-15T10:35:00",
    "fechaEntrega": null
}
```

---

## 🔍 Verificación de Logs

### Logs de Tramos (logistica-service)
Cuando creas un tramo, deberías ver:
```
INFO  TramoService: Distancia calculada para tramo: 608.353 km (7 hours 45 mins)
INFO  TramoService: Tramo creado: 2
```

### Logs de Solicitudes (seguimiento-service)
Cuando creas una solicitud, deberías ver:
```
INFO  SolicitudService: Nuevo cliente creado: 4
INFO  SolicitudService: Nuevo contenedor creado: 4
INFO  SolicitudService: Solicitud creada: 4 (SOL-A3F2B9C1) - Origen: Córdoba Capital -> Destino: Rosario
```

---

## 🚨 Errores Comunes y Soluciones

### Error 1: "Ruta no encontrada"
**Causa:** El `rutaId` no existe en la BD
**Solución:** Verificar rutas disponibles con `GET http://localhost:8082/rutas`

### Error 2: "Ubicación de origen no encontrada"
**Causa:** El `origenId` no existe en logística-service
**Solución:** Verificar ubicaciones con `GET http://localhost:8082/ubicaciones`

### Error 3: "Bad Request" sin mensaje
**Causa:** Falta un campo requerido en el JSON
**Solución:** Revisar que tengas todos los campos marcados con ✅

### Error 4: "not-null property references a null"
**Causa:** El service no puede obtener la ubicación de logística
**Solución:** 
1. Verificar que logística-service esté ejecutándose en puerto 8082
2. Verificar que la ubicación exista con GET

---

## 📚 Resumen de Diferencias

### TramoDTO (POST Tramos)
- **Acepta:** `TramoDTO` completo
- **Mínimo requerido:** `rutaId`, `orden`, `tipo`
- **Recomendado incluir:** Coordenadas para cálculo automático de distancia

### CrearSolicitudRequest (POST Solicitudes)
- **Acepta:** `CrearSolicitudRequest` (NO `SolicitudDTO`)
- **Mínimo requerido:** `origenId`, `destinoId`
- **Opciones:**
  - Usar `clienteId` + `contenedorId` existentes (más simple)
  - Crear nuevos con datos completos

---

## 🎯 Prueba Recomendada para TPI

```http
### 1. Crear primer tramo (Córdoba → Mendoza)
POST http://localhost:8082/tramos
Content-Type: application/json

{
    "rutaId": 1,
    "orden": 2,
    "origenDepositoId": 1,
    "destinoDepositoId": 2,
    "origenLatitud": -31.4167,
    "origenLongitud": -64.1833,
    "destinoLatitud": -32.8908,
    "destinoLongitud": -68.8272,
    "tipo": "DEPOSITO_DEPOSITO"
}

### 2. Crear solicitud simple
POST http://localhost:8081/solicitudes
Content-Type: application/json

{
    "clienteId": 1,
    "contenedorId": 1,
    "origenId": 1,
    "destinoId": 2
}

### 3. Verificar que la solicitud tiene direcciones y coordenadas
GET http://localhost:8081/solicitudes/4
```

Esto demuestra:
- ✅ Comunicación entre microservicios (seguimiento → logística) usando **RestClient**
- ✅ Cálculo automático de distancia (logística → geoapi) usando **RestClient**
- ✅ Datos completos obtenidos de servicios externos
- ✅ Arquitectura de microservicios funcionando correctamente

---

## 💡 Explicación para la Presentación

**Profesor:** "¿Cómo se comunican los microservicios?"

**Tú:** 
> "Cuando creo una solicitud de transporte, el servicio de seguimiento necesita las direcciones y coordenadas de origen y destino. Estas ubicaciones están en el servicio de logística.
> 
> El `SolicitudService` usa `LogisticaServiceClient`, que implementa **RestClient** de Spring 6, para hacer llamadas HTTP GET a logística-service en el puerto 8082.
> 
> RestClient es la forma moderna de Spring para llamadas HTTP, reemplazando al deprecado RestTemplate. Es más declarativo y funcional.
> 
> Una vez obtenidas las ubicaciones, el servicio de seguimiento puede guardar la solicitud con todos los datos necesarios: direcciones completas, latitudes y longitudes."

**Flujo completo:**
1. POST `/solicitudes` → Seguimiento Service
2. Seguimiento llama (RestClient) → Logística `/ubicaciones/1`
3. Logística responde con ubicación completa
4. Seguimiento guarda solicitud con datos completos
5. Responde al cliente

✅ **Demuestra arquitectura de microservicios real con comunicación HTTP moderna.**
