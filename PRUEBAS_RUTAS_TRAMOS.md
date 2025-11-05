# Pruebas de Rutas, Tramos y Asignación de Camiones

## 🎯 Flujo Completo Implementado (RF4, RF6, RF7)

Este documento te guía para probar la funcionalidad completa de gestión de rutas y tramos.

---

## ✅ **PASO 0: Iniciar los servicios**

```bash
# Terminal 1 - API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 2 - Logística Service
cd logistica-service
mvn spring-boot:run

# Terminal 3 - Seguimiento Service
cd seguimiento-service
mvn spring-boot:run

# Terminal 4 - GeoAPI
cd geoapi
mvn spring-boot:run
```

Espera que todos los servicios estén activos:
- Gateway: http://localhost:8080
- Seguimiento: http://localhost:8081
- Logística: http://localhost:8082
- GeoAPI: http://localhost:8083

---

## ✅ **PASO 1: Crear Solicitud Externa (RF1)**

```bash
curl -X POST http://localhost:8080/api/seguimiento/solicitudes \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "productoId": 1,
    "cantidad": 100,
    "origenDireccion": "Av. Corrientes 1234, CABA",
    "origenLatitud": -34.603722,
    "origenLongitud": -58.381592,
    "destinoDireccion": "Av. Santa Fe 5678, CABA",
    "destinoLatitud": -34.593841,
    "destinoLongitud": -58.402344,
    "detalles": "Entrega urgente"
  }'
```

**Respuesta esperada**: Status 201, devuelve la solicitud con `id: 1`

---

## ✅ **PASO 2: Crear Ruta para la Solicitud (RF4)**

```bash
curl -X POST http://localhost:8080/api/logistica/rutas \
  -H "Content-Type: application/json" \
  -d '{
    "solicitudExternaId": 1,
    "cantidadTramos": 2,
    "cantidadDepositos": 1,
    "observaciones": "Ruta con depósito intermedio"
  }'
```

**Respuesta esperada**: Status 201, devuelve ruta con:
- `id: 1`
- `solicitudExternaId: 1`
- `estado: "PROPUESTA"`

---

## ✅ **PASO 3: Crear Tramos de la Ruta**

### Tramo 1: Origen → Depósito

```bash
curl -X POST http://localhost:8080/api/logistica/tramos \
  -H "Content-Type: application/json" \
  -d '{
    "rutaId": 1,
    "orden": 1,
    "tipo": "ORIGEN_DEPOSITO",
    "origenDepositoId": null,
    "destinoDepositoId": 1,
    "origenLatitud": -34.603722,
    "origenLongitud": -58.381592,
    "destinoLatitud": -34.606111,
    "destinoLongitud": -58.373889,
    "fechaInicioEstimada": "2025-06-01T08:00:00",
    "fechaFinEstimada": "2025-06-01T10:00:00",
    "costoAproximado": 5000
  }'
```

**Nota**: La distancia se calcula automáticamente usando GeoAPI (Haversine).

### Tramo 2: Depósito → Destino

```bash
curl -X POST http://localhost:8080/api/logistica/tramos \
  -H "Content-Type: application/json" \
  -d '{
    "rutaId": 1,
    "orden": 2,
    "tipo": "DEPOSITO_DESTINO",
    "origenDepositoId": 1,
    "destinoDepositoId": null,
    "origenLatitud": -34.606111,
    "origenLongitud": -58.373889,
    "destinoLatitud": -34.593841,
    "destinoLongitud": -58.402344,
    "fechaInicioEstimada": "2025-06-01T11:00:00",
    "fechaFinEstimada": "2025-06-01T13:00:00",
    "costoAproximado": 6000
  }'
```

---

## ✅ **PASO 4: Consultar Tramos de la Ruta**

```bash
curl http://localhost:8080/api/logistica/tramos/ruta/1
```

**Respuesta esperada**: Array con 2 tramos ordenados, cada uno con:
- `estado: "ESTIMADO"`
- `distanciaKm` calculada automáticamente
- `camionId: null` (sin asignar aún)

---

## ✅ **PASO 5: Asignar Camión al Tramo 1 (RF6)**

Primero verifica que el camión 1 existe y está disponible:

```bash
curl http://localhost:8080/api/logistica/camiones/1
```

**Debe devolver**: `disponible: true`, `capacidadCarga >= 100`

Asigna el camión al primer tramo:

```bash
curl -X PATCH http://localhost:8080/api/logistica/tramos/1/asignar-camion \
  -H "Content-Type: application/json" \
  -d '{"camionId": 1}'
```

**Validaciones automáticas**:
- ✅ Camión disponible (`disponible = true`)
- ✅ Capacidad suficiente
- ✅ Tramo en estado `ESTIMADO`

**Resultado**:
- Tramo cambia a estado `ASIGNADO`
- `camionId: 1`
- Camión pasa a `disponible: false`

**Verifica que el camión quedó bloqueado**:

```bash
curl http://localhost:8080/api/logistica/camiones/1
# Debe mostrar: disponible: false
```

---

## ✅ **PASO 6: Iniciar el Tramo (RF7 - Inicio)**

```bash
curl -X PATCH http://localhost:8080/api/logistica/tramos/1/iniciar
```

**Validaciones automáticas**:
- ✅ Tramo debe estar en estado `ASIGNADO`

**Resultado**:
- Estado cambia a `INICIADO`
- Se registra `fechaInicioReal` (timestamp actual)

---

## ✅ **PASO 7: Finalizar el Tramo (RF7 - Fin)**

```bash
curl -X PATCH http://localhost:8080/api/logistica/tramos/1/finalizar
```

**Validaciones automáticas**:
- ✅ Tramo debe estar en estado `INICIADO`

**Resultado**:
- Estado cambia a `FINALIZADO`
- Se registra `fechaFinReal` (timestamp actual)
- Se calcula duración en minutos
- **Camión vuelve a `disponible: true`** (liberado para otro tramo)

**Verifica que el camión quedó liberado**:

```bash
curl http://localhost:8080/api/logistica/camiones/1
# Debe mostrar: disponible: true
```

---

## ✅ **PASO 8: Completar Tramo 2**

Ahora el camión 1 está disponible nuevamente. Asígnalo al segundo tramo:

```bash
# Asignar camión al tramo 2
curl -X PATCH http://localhost:8080/api/logistica/tramos/2/asignar-camion \
  -H "Content-Type: application/json" \
  -d '{"camionId": 1}'

# Iniciar tramo 2
curl -X PATCH http://localhost:8080/api/logistica/tramos/2/iniciar

# Finalizar tramo 2
curl -X PATCH http://localhost:8080/api/logistica/tramos/2/finalizar
```

---

## ✅ **PASO 9: Cambiar Estado de Ruta**

Una vez finalizados todos los tramos, actualiza el estado de la ruta:

```bash
curl -X PATCH http://localhost:8080/api/logistica/rutas/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "FINALIZADA"}'
```

---

## 📊 **Consultas Adicionales**

### Listar tramos por camión:

```bash
curl http://localhost:8080/api/logistica/tramos/camion/1
```

### Listar tramos por estado:

```bash
curl http://localhost:8080/api/logistica/tramos/estado/FINALIZADO
```

### Ver ruta completa con tramos:

```bash
curl http://localhost:8080/api/logistica/rutas/1
```

---

## 🎯 **Estados del Tramo (Flujo)**

```
ESTIMADO → ASIGNADO → INICIADO → FINALIZADO
   ↓           ↓          ↓          ↓
 Creado    +Camión    +FechaInicio  +FechaFin
          Camión      (timestamp)   (timestamp)
          bloqueado                 Camión liberado
```

---

## 🚨 **Errores Comunes y Soluciones**

### Error: "Camión no disponible"
- **Causa**: El camión ya está asignado a otro tramo activo
- **Solución**: Finaliza el tramo anterior o usa otro camión

### Error: "El tramo debe estar en estado ASIGNADO para iniciarse"
- **Causa**: Intentas iniciar un tramo sin asignar camión
- **Solución**: Ejecuta `PATCH /tramos/{id}/asignar-camion` primero

### Error: "El tramo debe estar en estado INICIADO para finalizarse"
- **Causa**: Intentas finalizar un tramo que no se inició
- **Solución**: Ejecuta `PATCH /tramos/{id}/iniciar` primero

### Error: "Ruta no encontrada"
- **Causa**: La `rutaId` en el tramo no existe
- **Solución**: Crea la ruta primero (PASO 2)

---

## ✅ **Swagger UI**

Puedes probar todo esto desde Swagger:

- **Logística Service**: http://localhost:8082/swagger-ui/index.html
- **Seguimiento Service**: http://localhost:8081/swagger-ui/index.html

---

## 📝 **Notas Técnicas**

1. **Cálculo de Distancia**: 
   - ✅ **Con Google Maps API Key configurada**: Usa Google Maps Directions API real (distancias por calles)
   - ⚠️ **Sin API Key**: Fallback automático a Haversine (línea recta, menos preciso)
   - Ver `geoapi/CONFIGURAR_GOOGLE_MAPS.md` para instrucciones de configuración
2. **Liberación de Camión**: Automática al finalizar tramo.
3. **Validación de Capacidad**: Se valida contra `producto.peso` de la solicitud.
4. **Timestamps**: Se registran automáticamente con `LocalDateTime.now()`.
5. **Transacciones**: Todas las operaciones son transaccionales (@Transactional).
6. **GeoAPI**: Funciona con o sin API Key. Revisa los logs para ver qué método se está usando.

---

## 🔜 **Próximos Pasos**

- [ ] Implementar cálculo de costo completo (RF8)
- [ ] Agregar autenticación con Keycloak
- [ ] Integrar Google Maps API real
- [ ] Crear Docker Compose para deployment
- [ ] Agregar notificaciones en tiempo real

---

¡Ahora puedes probar todo el flujo completo! 🚀
