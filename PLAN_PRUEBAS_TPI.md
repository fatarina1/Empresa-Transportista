# 📋 Plan de Pruebas Completo - TPI Backend 2025

## 🎯 Objetivo
Verificar que el sistema cumple con TODOS los requerimientos funcionales mínimos del enunciado.

---

## ✅ CHECKLIST DE REQUERIMIENTOS FUNCIONALES

### RF1: Registrar nueva solicitud de transporte (Cliente)
- [ ] Crear cliente nuevo si no existe
- [ ] Crear contenedor nuevo
- [ ] Crear solicitud de transporte
- [ ] Verificar estados: [BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA]
- [ ] Validar que la solicitud incluye origen y destino

### RF2: Consultar estado del transporte (Cliente)
- [ ] Obtener solicitud por ID
- [ ] Obtener solicitud por número
- [ ] Ver estado actual
- [ ] Ver costo estimado y tiempo estimado

### RF3: Consultar rutas tentativas con tramos, tiempo y costo estimado (Operador/Admin)
- [ ] Calcular ruta entre origen y destino
- [ ] Incluir depósitos intermedios
- [ ] Mostrar tramos individuales
- [ ] Calcular distancia total usando API externa (GeoAPI)
- [ ] Estimar tiempo de viaje
- [ ] Estimar costo total

### RF4: Asignar ruta con tramos a una solicitud (Operador/Admin)
- [ ] Crear ruta para una solicitud
- [ ] Crear tramos (origen → depósito → destino)
- [ ] Cambiar estado de solicitud a PROGRAMADA

### RF5: Consultar contenedores pendientes de entrega (Operador/Admin)
- [ ] Listar contenedores en tránsito
- [ ] Filtrar por estado de solicitud
- [ ] Ver ubicación actual (último tramo)

### RF6: Asignar camión a tramo (Operador/Admin)
- [ ] Verificar disponibilidad del camión
- [ ] Validar capacidad de peso del camión vs contenedor
- [ ] Validar capacidad de volumen del camión vs contenedor
- [ ] Asignar camión al tramo
- [ ] Marcar camión como no disponible

### RF7: Registrar inicio o fin de tramo (Transportista)
- [ ] Registrar fecha/hora de inicio de tramo
- [ ] Registrar fecha/hora de fin de tramo
- [ ] Cambiar estado del tramo
- [ ] Actualizar disponibilidad del camión
- [ ] Cambiar estado de solicitud si es último tramo

### RF8: Calcular costo total del envío
- [ ] Calcular distancia total (origen → depósitos → destino)
- [ ] Aplicar tarifa por km según volumen del contenedor
- [ ] Calcular costo de combustible (consumo camión × distancia × precio litro)
- [ ] Calcular costo de estadía en depósitos (días × tarifa depósito)
- [ ] Calcular cargo de gestión por cantidad de tramos
- [ ] Sumar costo base del camión
- [ ] Registrar costo estimado y costo real

### RF9: Registrar y actualizar depósitos, camiones y tarifas
- [ ] CRUD de depósitos
- [ ] CRUD de camiones
- [ ] CRUD de tarifas de volumen
- [ ] Actualizar parámetros de tarifación

### RF10: Validar que un camión no supere su capacidad máxima
- [ ] Validar peso del contenedor ≤ capacidad peso del camión
- [ ] Validar volumen del contenedor ≤ capacidad volumen del camión
- [ ] Rechazar asignación si excede capacidad

---

## 🧪 CASOS DE PRUEBA DETALLADOS

### 📦 CASO 1: Flujo Completo - Cliente Solicita Transporte

#### Paso 1: Cliente crea solicitud (RF1)
```bash
# 1.1. Crear cliente
curl -X POST http://localhost:8080/api/seguimiento/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan.perez@example.com",
    "documento": "12345678",
    "telefono": "+543511234567",
    "direccion": "Av. Colón 1234, Córdoba"
  }'
# Guardar ID del cliente: CLIENTE_ID

# 1.2. Crear contenedor
curl -X POST http://localhost:8080/api/seguimiento/contenedores \
  -H "Content-Type: application/json" \
  -d '{
    "identificacion": "CONT-001",
    "pesoKg": 2500.0,
    "volumenM3": 40.0,
    "largoM": 6.0,
    "anchoM": 2.4,
    "altoM": 2.6,
    "descripcion": "Contenedor estándar 20 pies",
    "clienteId": CLIENTE_ID
  }'
# Guardar ID del contenedor: CONTENEDOR_ID

# 1.3. Crear ubicación origen
curl -X POST http://localhost:8080/api/logistica/ubicaciones \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Depósito Central Córdoba",
    "direccion": "Ruta 9 Km 5, Córdoba",
    "latitud": -31.4201,
    "longitud": -64.1888,
    "tipo": "DEPOSITO"
  }'
# Guardar ID: ORIGEN_ID

# 1.4. Crear ubicación destino
curl -X POST http://localhost:8080/api/logistica/ubicaciones \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Cliente Buenos Aires",
    "direccion": "Av. Corrientes 5678, Buenos Aires",
    "latitud": -34.6037,
    "longitud": -58.3816,
    "tipo": "CLIENTE"
  }'
# Guardar ID: DESTINO_ID

# 1.5. Crear solicitud de transporte
curl -X POST http://localhost:8080/api/seguimiento/solicitudes \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": CLIENTE_ID,
    "contenedorId": CONTENEDOR_ID,
    "origenId": ORIGEN_ID,
    "destinoId": DESTINO_ID
  }'
# Guardar ID: SOLICITUD_ID
# Verificar estado inicial: BORRADOR
```

**✅ Resultado esperado:**
- Cliente creado con ID
- Contenedor creado y asociado al cliente
- Ubicaciones registradas
- Solicitud creada con estado BORRADOR

---

#### Paso 2: Cliente consulta estado (RF2)
```bash
# 2.1. Consultar solicitud por ID
curl http://localhost:8080/api/seguimiento/solicitudes/{SOLICITUD_ID}

# 2.2. Consultar detalle completo (incluye datos de cliente, contenedor, ubicaciones)
curl http://localhost:8080/api/seguimiento/solicitudes/{SOLICITUD_ID}/detalle

# 2.3. Consultar solicitudes del cliente
curl http://localhost:8080/api/seguimiento/solicitudes/cliente/{CLIENTE_ID}
```

**✅ Resultado esperado:**
- Ver número de solicitud
- Ver estado: BORRADOR
- Ver cliente, contenedor, origen y destino
- Ver costo estimado (si ya se calculó)

---

### 🚛 CASO 2: Operador Planifica Ruta y Asigna Camión

#### Paso 3: Operador consulta ruta tentativa (RF3)
```bash
# 3.1. Calcular distancia y tiempo entre origen y destino usando GeoAPI
curl "http://localhost:8080/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816"
# Resultado: distancia en km y duración en minutos

# 3.2. Si hay depósitos intermedios, calcular cada tramo
# Origen → Depósito Intermedio
curl "http://localhost:8080/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-32.9442&destinoLng=-60.6505"

# Depósito Intermedio → Destino
curl "http://localhost:8080/api/geoapi/distancia?origenLat=-32.9442&origenLng=-60.6505&destinoLat=-34.6037&destinoLng=-58.3816"

# 3.3. Consultar camiones disponibles
curl http://localhost:8080/api/logistica/camiones/disponibles

# 3.4. Verificar si hay camiones con capacidad suficiente
curl "http://localhost:8080/api/logistica/camiones/verificar-disponibilidad?peso=2500&volumen=40"
```

**✅ Resultado esperado:**
- Distancia total calculada
- Tiempo estimado calculado
- Lista de camiones disponibles
- Confirmación de disponibilidad por capacidad

---

#### Paso 4: Operador crea ruta con tramos (RF4)
```bash
# 4.1. Crear ruta para la solicitud
curl -X POST http://localhost:8080/api/logistica/rutas \
  -H "Content-Type: application/json" \
  -d '{
    "solicitudId": SOLICITUD_ID,
    "cantidadTramos": 1,
    "cantidadDepositos": 0
  }'
# Guardar ID: RUTA_ID

# 4.2. Crear tramo (origen → destino directo)
curl -X POST http://localhost:8080/api/logistica/tramos \
  -H "Content-Type: application/json" \
  -d '{
    "rutaId": RUTA_ID,
    "origenId": ORIGEN_ID,
    "destinoId": DESTINO_ID,
    "orden": 1,
    "tipo": "DIRECTO",
    "estado": "PENDIENTE",
    "distanciaKm": 700.5,
    "tiempoEstimadoMin": 540,
    "costoEstimado": 15000.0
  }'
# Guardar ID: TRAMO_ID

# 4.3. Actualizar estado de solicitud a PROGRAMADA
curl -X PATCH http://localhost:8080/api/seguimiento/solicitudes/{SOLICITUD_ID}/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "PROGRAMADA"}'
```

**✅ Resultado esperado:**
- Ruta creada
- Tramo creado con datos estimados
- Estado de solicitud cambiado a PROGRAMADA

---

#### Paso 5: Operador asigna camión a tramo (RF6 + RF10)
```bash
# 5.1. Crear camión si no existe
curl -X POST http://localhost:8080/api/logistica/camiones \
  -H "Content-Type: application/json" \
  -d '{
    "dominio": "AA123BB",
    "capacidadPesoKg": 30000.0,
    "capacidadVolumenM3": 90.0,
    "costoKmBase": 50.0,
    "consumoPromedioLitrosKm": 0.35,
    "disponible": true,
    "transportistaId": 1
  }'
# Guardar ID: CAMION_ID

# 5.2. Verificar capacidad del camión (RF10)
# Peso contenedor: 2500 kg ≤ Capacidad camión: 30000 kg ✅
# Volumen contenedor: 40 m³ ≤ Capacidad camión: 90 m³ ✅

# 5.3. Asignar camión al tramo
curl -X PATCH http://localhost:8080/api/logistica/tramos/{TRAMO_ID}/asignar-camion \
  -H "Content-Type: application/json" \
  -d '{
    "camionId": CAMION_ID
  }'

# 5.4. Verificar que el camión ya NO está disponible
curl http://localhost:8080/api/logistica/camiones/{CAMION_ID}
# disponible: false
```

**✅ Resultado esperado:**
- Validación de capacidad exitosa
- Camión asignado al tramo
- Camión marcado como no disponible

---

### 🚚 CASO 3: Transportista Ejecuta el Viaje

#### Paso 6: Transportista registra inicio de tramo (RF7)
```bash
# 6.1. Registrar inicio del tramo
curl -X PATCH http://localhost:8080/api/logistica/tramos/{TRAMO_ID}/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "fechaHoraInicio": "2025-11-04T08:00:00"
  }'

# 6.2. Verificar estado del tramo
curl http://localhost:8080/api/logistica/tramos/{TRAMO_ID}
# estado: EN_PROGRESO

# 6.3. Actualizar estado de solicitud
curl -X PATCH http://localhost:8080/api/seguimiento/solicitudes/{SOLICITUD_ID}/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "EN_TRANSITO"}'
```

**✅ Resultado esperado:**
- Fecha/hora de inicio registrada
- Estado del tramo: EN_PROGRESO
- Estado de solicitud: EN_TRANSITO

---

#### Paso 7: Transportista registra fin de tramo (RF7)
```bash
# 7.1. Registrar fin del tramo
curl -X PATCH http://localhost:8080/api/logistica/tramos/{TRAMO_ID}/finalizar \
  -H "Content-Type: application/json" \
  -d '{
    "fechaHoraFin": "2025-11-04T17:00:00"
  }'

# 7.2. Verificar tiempo real
# Inicio: 08:00
# Fin: 17:00
# Duración real: 9 horas = 540 minutos

# 7.3. Marcar camión como disponible nuevamente
curl -X PATCH http://localhost:8080/api/logistica/camiones/{CAMION_ID}/disponibilidad \
  -H "Content-Type: application/json" \
  -d '{"disponible": true}'

# 7.4. Actualizar estado de solicitud a ENTREGADA (si es el último tramo)
curl -X PATCH http://localhost:8080/api/seguimiento/solicitudes/{SOLICITUD_ID}/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "ENTREGADA"}'
```

**✅ Resultado esperado:**
- Fecha/hora fin registrada
- Tiempo real calculado
- Camión disponible nuevamente
- Estado de solicitud: ENTREGADA

---

### 💰 CASO 4: Cálculo de Costo Final (RF8)

#### Paso 8: Sistema calcula costo real del envío
```bash
# 8.1. Crear/consultar tarifas de volumen
curl -X POST http://localhost:8080/api/logistica/tarifas-volumen \
  -H "Content-Type: application/json" \
  -d '{
    "volumenMinM3": 0,
    "volumenMaxM3": 50,
    "costoPorKm": 12.50
  }'

# 8.2. Calcular costo del envío
# Fórmula según enunciado:
# Costo Total = Cargo Gestión + Costo por Km + Costo Combustible + Costo Estadía

# Componentes:
# - Cargo de gestión: cantidad_tramos × tarifa_fija
# - Costo por km: distancia_total × tarifa_volumen
# - Costo combustible: distancia × consumo_camion × precio_litro
# - Costo estadía: días_en_deposito × tarifa_deposito
# - Costo base camión: costo_base

# Ejemplo:
# 1 tramo × 100 (gestión) = 100
# 700 km × 12.50 (tarifa volumen) = 8,750
# 700 km × 0.35 L/km × 150 $/L = 36,750
# 0 días × 500 (estadía) = 0
# Costo base camión = 50 × 700 = 35,000
# TOTAL = 80,600

curl -X POST http://localhost:8080/api/logistica/calcular-costo \
  -H "Content-Type: application/json" \
  -d '{
    "solicitudId": SOLICITUD_ID,
    "distanciaTotal": 700.5,
    "cantidadTramos": 1,
    "diasEnDeposito": 0,
    "volumenContenedor": 40.0,
    "camionId": CAMION_ID
  }'
```

**✅ Resultado esperado:**
- Costo total calculado
- Desglose por componente
- Diferencia entre costo estimado y real
- Tiempo real vs tiempo estimado

---

### 📊 CASO 5: Consultas de Operador

#### Paso 9: Contenedores pendientes de entrega (RF5)
```bash
# 9.1. Listar solicitudes EN_TRANSITO
curl http://localhost:8080/api/seguimiento/solicitudes/estado/EN_TRANSITO

# 9.2. Listar contenedores en tránsito
curl http://localhost:8080/api/seguimiento/contenedores?enTransito=true

# 9.3. Ver ubicación actual (último depósito/tramo)
curl http://localhost:8080/api/seguimiento/solicitudes/{SOLICITUD_ID}/ubicacion-actual
```

**✅ Resultado esperado:**
- Lista de contenedores en tránsito
- Estado actual de cada uno
- Último tramo completado
- Próximo tramo pendiente

---

### 🔧 CASO 6: Gestión de Datos (RF9)

#### Paso 10: CRUD de depósitos, camiones y tarifas
```bash
# 10.1. CREAR depósito
curl -X POST http://localhost:8080/api/logistica/depositos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Depósito Rosario",
    "ubicacionId": UBICACION_ID,
    "capacidadMaxima": 8000.0,
    "capacidadActual": 3000.0,
    "tarifaEstadia": 500.0
  }'

# 10.2. ACTUALIZAR depósito
curl -X PUT http://localhost:8080/api/logistica/depositos/{DEPOSITO_ID} \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Depósito Rosario Norte",
    "capacidadMaxima": 10000.0,
    "capacidadActual": 3500.0,
    "tarifaEstadia": 600.0
  }'

# 10.3. ELIMINAR depósito
curl -X DELETE http://localhost:8080/api/logistica/depositos/{DEPOSITO_ID}

# 10.4. CRUD de camiones (similar)
curl -X GET http://localhost:8080/api/logistica/camiones
curl -X POST http://localhost:8080/api/logistica/camiones -d '{...}'
curl -X PUT http://localhost:8080/api/logistica/camiones/{ID} -d '{...}'
curl -X DELETE http://localhost:8080/api/logistica/camiones/{ID}

# 10.5. CRUD de tarifas de volumen
curl -X GET http://localhost:8080/api/logistica/tarifas-volumen
curl -X POST http://localhost:8080/api/logistica/tarifas-volumen -d '{...}'
curl -X PUT http://localhost:8080/api/logistica/tarifas-volumen/{ID} -d '{...}'
curl -X DELETE http://localhost:8080/api/logistica/tarifas-volumen/{ID}
```

**✅ Resultado esperado:**
- Todas las operaciones CRUD funcionan
- Validaciones correctas
- Códigos HTTP apropiados

---

## 🚨 PRUEBAS DE VALIDACIÓN (RF10)

### Caso de Error: Camión insuficiente
```bash
# 1. Crear camión pequeño
curl -X POST http://localhost:8080/api/logistica/camiones \
  -H "Content-Type: application/json" \
  -d '{
    "dominio": "BB456CC",
    "capacidadPesoKg": 1000.0,
    "capacidadVolumenM3": 10.0,
    "costoKmBase": 30.0,
    "consumoPromedioLitrosKm": 0.25,
    "disponible": true
  }'

# 2. Intentar asignar a contenedor grande (2500 kg, 40 m³)
curl -X PATCH http://localhost:8080/api/logistica/tramos/{TRAMO_ID}/asignar-camion \
  -H "Content-Type: application/json" \
  -d '{"camionId": CAMION_PEQUEÑO_ID}'

# 3. Debería retornar HTTP 400 con mensaje:
# "El camión no tiene capacidad suficiente"
```

**✅ Resultado esperado:**
- Error 400 Bad Request
- Mensaje descriptivo
- No se asigna el camión

---

## 📈 PRUEBAS DE INTEGRACIÓN API EXTERNA

### Verificar integración con Google Maps / GeoAPI
```bash
# 1. Calcular distancia entre dos puntos reales
curl "http://localhost:8080/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816"

# 2. Verificar respuesta con:
# - distanciaKm: valor numérico
# - duracionMinutos: valor numérico
# - rutaTexto: descripción textual (opcional)

# 3. Calcular ruta completa con múltiples puntos
curl -X POST http://localhost:8080/api/geoapi/ruta \
  -H "Content-Type: application/json" \
  -d '{
    "puntos": [
      {"latitud": -31.4201, "longitud": -64.1888},
      {"latitud": -32.9442, "longitud": -60.6505},
      {"latitud": -34.6037, "longitud": -58.3816}
    ]
  }'
```

**✅ Resultado esperado:**
- Distancia calculada correctamente
- Tiempo estimado realista
- API externa invocada y responde

---

## 🎯 RESUMEN DE VERIFICACIÓN

### Checklist Final de Cumplimiento

| # | Requerimiento | Endpoint(s) | Estado |
|---|---------------|-------------|--------|
| RF1 | Registrar solicitud | POST /solicitudes | ⬜ |
| RF2 | Consultar estado | GET /solicitudes/{id} | ⬜ |
| RF3 | Rutas tentativas | GET /geoapi/distancia | ⬜ |
| RF4 | Asignar ruta | POST /rutas, POST /tramos | ⬜ |
| RF5 | Contenedores pendientes | GET /solicitudes/estado/EN_TRANSITO | ⬜ |
| RF6 | Asignar camión | PATCH /tramos/{id}/asignar-camion | ⬜ |
| RF7 | Inicio/fin tramo | PATCH /tramos/{id}/iniciar, /finalizar | ⬜ |
| RF8 | Calcular costo | POST /calcular-costo | ⬜ |
| RF9 | CRUD datos | POST/PUT/DELETE /depositos, /camiones, /tarifas | ⬜ |
| RF10 | Validar capacidad | GET /camiones/verificar-disponibilidad | ⬜ |

### Aspectos Técnicos a Verificar

- [ ] Gateway rutea correctamente a todos los microservicios
- [ ] Swagger UI accesible en todos los servicios
- [ ] Códigos HTTP correctos (200, 201, 400, 404, 500)
- [ ] Validaciones de negocio implementadas
- [ ] Logs de operaciones importantes
- [ ] API externa (GeoAPI) funciona
- [ ] Separación de responsabilidades entre servicios
- [ ] DTOs vs Entities correctamente usados

---

## 📝 Próximos Pasos

1. **Ejecuta cada caso de prueba** en orden
2. **Marca los checkboxes** conforme funcionen
3. **Documenta los errores** que encuentres
4. **Corrige los problemas** uno por uno
5. **Crea colección de Postman** con todos estos casos
6. **Genera el docker-compose** para despliegue final

¿Por dónde quieres empezar? 🚀
