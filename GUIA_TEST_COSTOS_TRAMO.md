# 🧪 Guía de Prueba: Flujo Completo de Costos de Tramo

Esta guía te permitirá probar el flujo completo desde la creación de una solicitud hasta la finalización del tramo, verificando que los costos (`costoAproximado` y `costoReal`) se calculen correctamente.

## 📋 Pre-requisitos

1. ✅ Servicios Docker levantados: `docker compose up -d`
2. ✅ Importar en Postman: `POSTMAN_TEST_FLUJO_COMPLETO.json`

## 🎯 Solicitud de Prueba

Se creó automáticamente en el seed:

- **ID:** 11
- **Número:** SOL-TEST-2024
- **Cliente:** Juan Pérez (ID=1)
- **Contenedor:** CONT-TEST-2024 (600kg, 3.0m³)
- **Origen:** Córdoba (ID=1)
- **Destino:** Buenos Aires (ID=4)
- **Estado inicial:** BORRADOR
- **Ruta:** Ninguna (se creará en el paso 3)

## 📝 Flujo de Prueba (12 Pasos)

### 1️⃣ Listar Solicitud (BORRADOR)
```http
GET http://localhost:8081/api/solicitudes/11
```

**Verificar:**
- ✅ `estado: "BORRADOR"`
- ✅ `rutaId: null`
- ✅ `costoEstimado: 50000.00`
- ✅ `costoFinal: null`

---

### 2️⃣ Calcular Ruta Directa (Córdoba → Buenos Aires)
```http
POST http://localhost:8082/api/costos/ruta-tentativa
Content-Type: application/json

{
  "ubicacionOrigenId": 1,
  "ubicacionDestinoId": 4,
  "pesoKg": 600,
  "volumenM3": 3.0
}
```

**Verificar:**
- ✅ Devuelve 2 opciones: `rutaDirecta` y `rutaConDepositos`
- ✅ `rutaDirecta` tiene 1 tramo (~710 km)
- 📝 **Anotar el `costoEstimado` de la ruta directa**

---

### 3️⃣ Crear Ruta Directa y Asignarla a Solicitud
```http
POST http://localhost:8082/api/rutas
Content-Type: application/json

{
  "solicitudId": 11,
  "tramos": [
    {
      "orden": 1,
      "origenLatitud": -31.4167,
      "origenLongitud": -64.1833,
      "destinoLatitud": -34.5875,
      "destinoLongitud": -58.3736,
      "distanciaKm": 710.0,
      "tipo": "ORIGEN_DESTINO"
    }
  ]
}
```

**Verificar:**
- ✅ HTTP 201 Created
- ✅ Respuesta contiene `tramos` array con 1 elemento
- 🚨 **IMPORTANTE: COPIAR el `id` del tramo de la respuesta**

**Ejemplo de respuesta:**
```json
{
  "id": 7,
  "estado": "ESTIMADA",
  "cantidadTramos": 1,
  "tramos": [
    {
      "id": 123,  ← 🔴 COPIAR ESTE NÚMERO
      "orden": 1,
      "estado": "ESTIMADO",
      "distanciaKm": 710.0,
      ...
    }
  ]
}
```

📝 **Reemplazar la variable `tramoId` en Postman:**
- Click en "Variables" (arriba a la derecha)
- Cambiar `tramoId` de `REEMPLAZAR_CON_ID_DEL_PASO_3` al número copiado

---

### 4️⃣ Verificar Solicitud (Debe estar EN_PLANIFICACION)
```http
GET http://localhost:8081/api/solicitudes/11
```

**Verificar:**
- ✅ `estado: "EN_PLANIFICACION"` (cambió automáticamente)
- ✅ `rutaId: 7` (o el ID de la ruta creada)
- ✅ `costoEstimado: 50000.00`
- ✅ `costoFinal: null`

---

### 5️⃣ Listar Camiones Disponibles
```http
GET http://localhost:8082/api/camiones?disponible=true
```

**Verificar:**
- ✅ Lista camiones con `disponible: true`
- 📝 **Usar camionId = 1 (Scania R500)** para las siguientes pruebas

---

### 6️⃣ Asignar Camión al Tramo
```http
PUT http://localhost:8082/api/tramos/{{tramoId}}/asignar-camion
Content-Type: application/json

{
  "camionId": 1
}
```

**⚠️ ASEGURARSE** de haber reemplazado `{{tramoId}}` con el ID real del paso 3

**Verificar:**
- ✅ `estado: "ASIGNADO"`
- ✅ `camionId: 1`
- 🎯 **`costoAproximado: 45000.00`** (o valor calculado) ✅
- ✅ `costoReal: null` (aún no finalizado)

---

### 7️⃣ Ver Tramo (Debe tener costoAproximado)
```http
GET http://localhost:8082/api/tramos/{{tramoId}}
```

**Verificar:**
```json
{
  "id": 123,
  "orden": 1,
  "estado": "ASIGNADO",
  "distanciaKm": 710.0,
  "costoAproximado": 45000.00,  ← ✅ DEBE TENER VALOR
  "costoReal": null,              ← ❌ Todavía null (normal)
  "camionId": 1
}
```

---

### 8️⃣ Iniciar Tramo
```http
PUT http://localhost:8082/api/tramos/{{tramoId}}/iniciar
```

**Verificar:**
- ✅ `estado: "INICIADO"`
- ✅ `fechaInicioReal: "2024-11-23T19:05:00.123"` (timestamp actual)

---

### 9️⃣ Verificar Solicitud (Debe estar EN_TRANSITO)
```http
GET http://localhost:8081/api/solicitudes/11
```

**Verificar:**
- ✅ `estado: "EN_TRANSITO"` (cambió automáticamente)
- ✅ `costoFinal: null` (todavía no finalizó)

---

### 🔟 Finalizar Tramo
```http
PUT http://localhost:8082/api/tramos/{{tramoId}}/finalizar
```

**🎯 ESTE ES EL PASO CLAVE - Calcula:**
1. ✅ `costoReal` del tramo individual (NUEVO)
2. ✅ Costo total de la ruta
3. ✅ Actualiza solicitud a `ENTREGADA`
4. ✅ Actualiza `costoFinal` y `tiempoReal` en solicitud
5. ✅ Libera el camión

**Verificar:**
- ✅ `estado: "FINALIZADO"`
- ✅ `fechaFinReal: "2024-11-23T19:06:00.456"` (timestamp actual)

---

### 1️⃣1️⃣ Ver Tramo FINALIZADO (con costoReal) ⭐
```http
GET http://localhost:8082/api/tramos/{{tramoId}}
```

**🎯 RESULTADO ESPERADO:**
```json
{
  "id": 123,
  "orden": 1,
  "estado": "FINALIZADO",
  "distanciaKm": 710.0,
  "costoAproximado": 45000.00,  ← ✅ CALCULADO en asignación
  "costoReal": 48500.00,         ← ✅ CALCULADO en finalización (era null antes)
  "fechaInicioReal": "2024-11-23T19:05:00.123",
  "fechaFinReal": "2024-11-23T19:06:00.456",
  "camionId": 1
}
```

**✅ PROBLEMA RESUELTO:** `costoReal` ahora se calcula para cada tramo individual

---

### 1️⃣2️⃣ Ver Solicitud ENTREGADA (con costoFinal) ⭐
```http
GET http://localhost:8081/api/solicitudes/11
```

**🎯 RESULTADO ESPERADO:**
```json
{
  "id": 11,
  "numero": "SOL-TEST-2024",
  "estado": "ENTREGADA",         ← ✅ Cambió automáticamente
  "rutaId": 7,
  "costoEstimado": 50000.00,
  "costoFinal": 48500.00,        ← ✅ Calculado con suma de tramos
  "tiempoEstimadoMin": 720,
  "tiempoRealMin": 1,            ← ✅ Calculado desde inicio hasta fin
  "fechaSolicitud": "2024-11-23T18:50:00",
  "updatedAt": "2024-11-23T19:06:00"  ← ✅ Actualizado
}
```

---

### 📊 BONUS: Ver Historial de Estados
```http
GET http://localhost:8081/api/solicitudes/11/historial
```

**Verificar flujo completo:**
1. ✅ BORRADOR → (creación inicial)
2. ✅ EN_PLANIFICACION → (al asignar ruta en paso 3)
3. ✅ EN_TRANSITO → (al iniciar tramo en paso 8)
4. ✅ ENTREGADA → (al finalizar último tramo en paso 10)

---

## 🎯 Resumen de Cambios Implementados

### Antes (Problema Original)
```json
{
  "id": 123,
  "estado": "FINALIZADO",
  "costoAproximado": null,  ❌
  "costoReal": null          ❌
}
```

### Después (Solución Implementada)
```json
{
  "id": 123,
  "estado": "FINALIZADO",
  "costoAproximado": 45000.00,  ✅ Calculado al asignar camión
  "costoReal": 48500.00         ✅ Calculado al finalizar tramo
}
```

---

## 🔧 Detalles Técnicos

### Nuevos Métodos en `CostoService.java`

1. **`calcularCostoAproximadoTramo(Tramo tramo)`**
   - Se ejecuta al asignar camión (`TramoService.asignarCamion()`)
   - Calcula: costo km + combustible + gestión + estadía estimada
   - Guarda en `tramo.costoAproximado`

2. **`calcularCostoRealTramo(Tramo tramo)`**
   - Se ejecuta al finalizar tramo (`TramoService.finalizarTramo()`)
   - Calcula: costo km + combustible + gestión + estadía REAL
   - Guarda en `tramo.costoReal`

### Flujo de Estados

```
BORRADOR
   ↓ (crear ruta)
EN_PLANIFICACION
   ↓ (iniciar tramo)
EN_TRANSITO
   ↓ (finalizar último tramo)
ENTREGADA
```

---

## 🐛 Troubleshooting

### Error: "No se puede asignar camión"
- ✅ Verificar que el camión esté disponible (`GET /api/camiones?disponible=true`)
- ✅ Verificar capacidad suficiente (>= 600kg y >= 3.0m³)

### Error: "El tramo debe estar en estado ASIGNADO"
- ✅ Ejecutar paso 6 (asignar camión) antes de paso 8 (iniciar)

### Error: "El tramo debe estar en estado INICIADO"
- ✅ Ejecutar paso 8 (iniciar) antes de paso 10 (finalizar)

### costoReal sigue siendo null
- ✅ Verificar que ejecutaste paso 10 (finalizar tramo)
- ✅ Verificar logs del contenedor: `docker logs infra-logistica-service-1`

---

## 📞 Soporte

Si encuentras algún problema, verifica los logs:

```bash
# Ver logs de logística
docker logs -f infra-logistica-service-1

# Ver logs de seguimiento
docker logs -f infra-seguimiento-service-1
```

**¡Feliz Testing! 🚀**
