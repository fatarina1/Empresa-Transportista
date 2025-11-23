# 🌱 Datos de Seed - Base de Datos

Este documento describe todos los datos precargados en la base de datos para facilitar las pruebas de TODOS los flujos del sistema.

---

## 📊 SEGUIMIENTO-SERVICE

### 👥 Clientes (3)

| ID | Nombre | Email | Teléfono | Documento |
|----|--------|-------|----------|-----------|
| 1 | Juan Pérez | juan.perez@email.com | 351-123-4567 | 30123456 |
| 2 | María González | maria.gonzalez@email.com | 261-987-6543 | 27987654 |
| 3 | Empresa Logística SA | contacto@logistica.com.ar | 341-555-1234 | CUIT 30-71234567-8 |

### 📦 Contenedores (10)

| ID | Identificación | Peso (kg) | Volumen (m³) | Cliente | Descripción | Estado |
|----|----------------|-----------|--------------|---------|-------------|--------|
| 1 | CONT-001-2024 | 500.50 | 2.5 | Juan Pérez | Mercadería general - Electrónica | 🔄 En tránsito |
| 2 | CONT-002-2024 | 1200.00 | 5.0 | María González | Alimentos no perecederos | 🔄 En tránsito |
| 3 | CONT-003-2024 | 800.75 | 3.2 | Empresa Logística SA | Repuestos automotrices | 📋 Programado |
| 4 | CONT-004-2024 | 650.00 | 3.8 | Juan Pérez | Equipamiento médico | ✅ Entregado |
| 5 | CONT-005-2024 | 1450.00 | 6.5 | María González | Maquinaria industrial | ✅ Entregado |
| 6 | CONT-006-2024 | 320.00 | 2.0 | Empresa Logística SA | Productos farmacéuticos | ✅ Entregado |
| 7 | CONT-007-2024 | 890.00 | 4.2 | Juan Pérez | Textiles y confecciones | 📝 Borrador |
| 8 | CONT-008-2024 | 450.00 | 2.8 | María González | Material de oficina | ✨ Disponible |
| 9 | CONT-009-2024 | 1100.00 | 5.5 | Empresa Logística SA | Electrodomésticos | ✨ Disponible |
| 10 | CONT-010-2024 | 280.00 | 1.5 | Juan Pérez | Herramientas pequeñas | ✨ Disponible |

### 📋 Solicitudes (7)

#### 🚚 Solicitudes EN TRÁNSITO (2) - **Prueba RF5: Contenedores en Depósito**

**Solicitud 1** - `SOL-2024-001`
- **Cliente:** Juan Pérez
- **Contenedor:** CONT-001-2024 (500kg, 2.5m³)
- **Ruta:** Córdoba → **[Depósito Rosario]** → Buenos Aires
- **Estado:** `EN_TRANSITO`
- **Situación:** ⏳ **Contenedor esperando en Depósito Rosario hace 3 días**
- **Tramo 1:** ✅ FINALIZADO (Córdoba → Rosario, 320km)
- **Tramo 2:** 📋 ASIGNADO (Rosario → Buenos Aires, 300km)
  - 🕐 Inicio estimado: Incluye **24h de estadía** en depósito
  - 💰 Costo estadía: $600/día
- **Costo estimado:** $45,000
- **Tiempo estimado:** 12 horas + 24h estadía = **36 horas**
- **Endpoint:** `GET /api/logistica/depositos/3/contenedores`

**Solicitud 2** - `SOL-2024-002`
- **Cliente:** María González
- **Contenedor:** CONT-002-2024 (1200kg, 5.0m³)
- **Ruta:** Buenos Aires → **[Depósito Mendoza]** → Córdoba
- **Estado:** `EN_TRANSITO`
- **Situación:** ⚠️ **Contenedor esperando en Depósito Mendoza hace 7 días**
- **Tramo 1:** ✅ FINALIZADO (Buenos Aires → Mendoza, 1050km)
- **Tramo 2:** 📋 ASIGNADO (Mendoza → Córdoba, 608km)
  - 🕐 Inicio estimado: Incluye **24h de estadía** en depósito
  - 💰 Costo estadía: $450/día (⚠️ ya lleva 7 días = $3,150)
- **Costo estimado:** $78,000
- **Tiempo estimado:** 20 horas + 24h estadía = **44 horas**
- **Endpoint:** `GET /api/logistica/depositos/2/contenedores`

#### 📝 Solicitud PROGRAMADA (1)

**Solicitud 3** - `SOL-2024-003`
- **Cliente:** Empresa Logística SA
- **Contenedor:** CONT-003-2024 (800kg, 3.2m³)
- **Ruta:** Córdoba → Mendoza (directa, sin depósitos)
- **Estado:** `PROGRAMADA`
- **Tramo 1:** 📋 ESTIMADO (Córdoba → Mendoza, 608km)
  - 🕐 Inicio estimado: Hoy + 2 horas
  - 🕐 Fin estimado: Hoy + 12 horas
- **Costo estimado:** $35,000
- **Tiempo estimado:** 10 horas (sin estadía)

#### ✅ Solicitudes ENTREGADAS (3) - **Prueba RF8: Costo Real**

**Solicitud 4** - `SOL-2024-004` ⭐ **(Eficiente)**
- **Cliente:** Juan Pérez
- **Contenedor:** CONT-004-2024 (650kg, 3.8m³)
- **Ruta:** Córdoba → Buenos Aires (directa, 710km)
- **Estado:** `ENTREGADA` (hace 2 días)
- **Costo:** $48,500 (estimado: $52,000) ✅ **$3,500 menos**
- **Tiempo:** 11h 20min (estimado: 12h) ✅ **40min más rápido**
- **Bonificación:** Aplicada por eficiencia
- **Observaciones:** Entrega completada exitosamente
- **Endpoint:** `GET /api/logistica/rutas/4/costo-real` ⭐

**Solicitud 5** - `SOL-2024-005` ⚠️ **(Con demoras)**
- **Cliente:** María González
- **Contenedor:** CONT-005-2024 (1450kg, 6.5m³)
- **Ruta:** Buenos Aires → Córdoba → Mendoza (2 tramos, 1 depósito)
- **Estado:** `ENTREGADA` (hace 5 días)
- **Costo:** $108,500 (estimado: $95,000) ⚠️ **$13,500 más**
- **Tiempo:** 23h (estimado: 20h) ⚠️ **3h de retraso**
- **Penalización:** Aplicada por demoras
- **Estadía:** 24h en Depósito Córdoba ($500/día)
- **Observaciones:** Entrega completada con demoras por tráfico
- **Endpoint:** `GET /api/logistica/rutas/5/costo-real` ⚠️

**Solicitud 6** - `SOL-2024-006` 🎯 **(Perfecta)**
- **Cliente:** Empresa Logística SA
- **Contenedor:** CONT-006-2024 (320kg, 2.0m³)
- **Ruta:** Córdoba → Mendoza (directa, 608km)
- **Estado:** `ENTREGADA` (hace 7 días)
- **Costo:** $42,000 (estimado: $42,000) 🎯 **Exacto**
- **Tiempo:** 10h (estimado: 10h) 🎯 **Perfecto**
- **Sin ajustes:** Ni bonificación ni penalización
- **Observaciones:** Entrega impecable
- **Endpoint:** `GET /api/logistica/rutas/6/costo-real` 🎯

#### 📄 Solicitud BORRADOR (1)

**Solicitud 7** - `SOL-2024-007`
- **Cliente:** Juan Pérez
- **Contenedor:** CONT-007-2024 (890kg, 4.2m³)
- **Ruta:** Rosario → Córdoba
- **Estado:** `BORRADOR` (creada hace 4 horas)
- **Costo estimado:** $28,000
- **Tiempo estimado:** 8 horas
- **Observaciones:** Solicitud nueva - Pendiente de planificación de ruta

---

## 🚛 LOGISTICA-SERVICE

### 🚚 Camiones (3)

| ID | Dominio | Capacidad Peso | Capacidad Volumen | Costo/km | Consumo L/km | Disponible |
|----|---------|----------------|-------------------|----------|--------------|------------|
| 1 | AB123CD | 10,000 kg | 50 m³ | $80 | 0.35 | ✅ Sí |
| 2 | EF456GH | 15,000 kg | 75 m³ | $120 | 0.45 | ✅ Sí |
| 3 | IJ789KL | 8,000 kg | 40 m³ | $60 | 0.30 | ❌ No (en uso) |

### 📍 Ubicaciones (4)

| ID | Nombre | Dirección | Coordenadas |
|----|--------|-----------|-------------|
| 1 | Córdoba Capital | Av. Colón 1234, Córdoba | -31.4167, -64.1833 |
| 2 | Mendoza Capital | San Martín 500, Mendoza | -32.8908, -68.8272 |
| 3 | Rosario | Pellegrini 1000, Rosario | -32.9468, -60.6393 |
| 4 | Buenos Aires - Retiro | Av. Ramos Mejía 1302, CABA | -34.5875, -58.3736 |

### 🏢 Depósitos (3) - **Con costos de estadía**

| ID | Nombre | Ubicación | Costo Estadía/día | Activo |
|----|--------|-----------|-------------------|--------|
| 1 | Depósito Central Córdoba | Córdoba | $500 | ✅ |
| 2 | Depósito Cuyo | Mendoza | $450 | ✅ |
| 3 | Depósito Litoral | Rosario | $600 | ✅ |

**Importante:** 
- Cada depósito cobra estadía diaria cuando un contenedor permanece allí
- El sistema calcula automáticamente **24 horas de estadía** entre tramos que pasan por depósitos
- Los costos de estadía se incluyen en los cálculos de rutas tentativas y costos reales

### 🛣️ Rutas (6)

#### 🚚 Rutas EN EJECUCIÓN (2) - **Para probar RF5: Contenedores en Depósito**

**Ruta 1** - Solicitud 1 (Contenedor CONT-001-2024)
- **Tramos:** 2 (Córdoba → Rosario → Buenos Aires)
- **Estado:** `EN_EJECUCION`
- **Depósitos:** 1 (Rosario)
- **Tramo 1:** ✅ FINALIZADO (hace 3 días)
  - Córdoba → Depósito Rosario (320km)
  - Fecha inicio real: Hace 4 días
  - Fecha fin real: Hace 3 días 18h
  - ⏳ Contenedor esperando en Rosario
- **Tramo 2:** 🔵 ASIGNADO (Rosario → Buenos Aires, 300km)
  - Camión: EF456GH
  - Fecha inicio estimada: Incluye 24h de estadía
  - Fecha fin estimada: + 5h de viaje
- **Endpoint:** `GET /api/logistica/depositos/3/contenedores`

**Ruta 2** - Solicitud 2 (Contenedor CONT-002-2024)
- **Tramos:** 2 (Buenos Aires → Mendoza → Córdoba)
- **Estado:** `EN_EJECUCION`
- **Depósitos:** 1 (Mendoza)
- **Tramo 1:** ✅ FINALIZADO (hace 7 días)
  - Buenos Aires → Depósito Mendoza (1050km)
  - Fecha inicio real: Hace 9 días
  - Fecha fin real: Hace 7 días
  - ⚠️ Contenedor esperando en Mendoza hace 7 días
- **Tramo 2:** 🔵 ASIGNADO (Mendoza → Córdoba, 608km)
  - Fecha inicio estimada: Incluye 24h de estadía
  - Fecha fin estimada: + 10h de viaje
- **Endpoint:** `GET /api/logistica/depositos/2/contenedores`

#### 📝 Ruta PROPUESTA (1)

**Ruta 3** - Solicitud 3
- **Tramos:** 1 (Córdoba → Mendoza directo)
- **Estado:** `PROPUESTA`
- **Depósitos:** 0 (ruta directa)
- **Tramo 1:** 📋 ESTIMADO
  - Córdoba → Mendoza (608km)
  - Fecha inicio estimada: Hoy + 2h
  - Fecha fin estimada: Hoy + 12h
  - Sin estadía (ruta directa)

#### ✅ Rutas FINALIZADAS (3) - **Para probar RF8 (Costo Real)**

**Ruta 4** - Solicitud 4 ⭐ **(Eficiente)**
- **Estado:** `FINALIZADA` (hace 2 días)
- **Costo Final:** $48,500 (estimado: $52,000) ✅
- **Tramos:** 1 (Córdoba → Buenos Aires, 710km)
- **Sin depósitos intermedios**
- **Tiempo real:** 11h 20min (estimado: 12h)
- **Bonificación:** Por llegar antes
- **Endpoint:** `GET /api/logistica/rutas/4/costo-real` ⭐

**Ruta 5** - Solicitud 5 ⚠️ **(Con demoras)**
- **Estado:** `FINALIZADA` (hace 5 días)
- **Costo Final:** $108,500 (estimado: $95,000) ⚠️
- **Tramos:** 2 (Buenos Aires → Córdoba → Mendoza)
- **Depósitos:** 1 (Córdoba)
- **Estadía:** 24h en Depósito Córdoba ($500)
- **Tiempo real:** 23h (estimado: 20h)
- **Penalización:** Por demoras en tráfico
- **Endpoint:** `GET /api/logistica/rutas/5/costo-real` ⚠️

**Ruta 6** - Solicitud 6 🎯 **(Perfecta)**
- **Estado:** `FINALIZADA` (hace 7 días)
- **Costo Final:** $42,000 (estimado: $42,000) 🎯
- **Tramos:** 1 (Córdoba → Mendoza, 608km)
- **Sin depósitos intermedios**
- **Tiempo real:** 10h (estimado: 10h)
- **Sin ajustes:** Ni bonificación ni penalización
- **Endpoint:** `GET /api/logistica/rutas/6/costo-real` 🎯

### ⚙️ Configuración General

| Clave | Valor | Descripción |
|-------|-------|-------------|
| VALOR_LITRO_COMBUSTIBLE | $450.00 | Precio del combustible |
| CARGO_GESTION_POR_TRAMO | $5,000.00 | Cargo fijo por tramo |

---
| 1 | AB123CD | 10,000 kg | 50 m³ | $80 | 0.35 | ✅ Sí |
| 2 | EF456GH | 15,000 kg | 75 m³ | $120 | 0.45 | ✅ Sí |
| 3 | IJ789KL | 8,000 kg | 40 m³ | $60 | 0.30 | ❌ No |

### 📍 Ubicaciones (4)

| ID | Nombre | Dirección | Coordenadas |
|----|--------|-----------|-------------|
| 1 | Córdoba Capital | Av. Colón 1234, Córdoba | -31.4167, -64.1833 |
| 2 | Mendoza Capital | San Martín 500, Mendoza | -32.8908, -68.8272 |
| 3 | Rosario | Pellegrini 1000, Rosario | -32.9468, -60.6393 |
| 4 | Buenos Aires - Retiro | Av. Ramos Mejía 1302, CABA | -34.5875, -58.3736 |

### 🏢 Depósitos (3)

| ID | Nombre | Ubicación | Costo Estadía/día |
|----|--------|-----------|-------------------|
| 1 | Depósito Central Córdoba | Córdoba | $500 |
| 2 | Depósito Cuyo | Mendoza | $450 |
| 3 | Depósito Litoral | Rosario | $600 |

### 🛣️ Rutas (6)

#### 🚚 Rutas EN EJECUCIÓN (2)

**Ruta 1** - Solicitud 1
- **Tramos:** 2 (Córdoba → Rosario → Buenos Aires)
- **Estado:** `EN_EJECUCION`
- **Tramo 1:** ✅ FINALIZADO (hace 3 días) - Contenedor en Depósito Rosario
- **Tramo 2:** 🔵 ASIGNADO - Rosario → Buenos Aires

**Ruta 2** - Solicitud 2
- **Tramos:** 2 (Buenos Aires → Mendoza → Córdoba)
- **Estado:** `EN_EJECUCION`
- **Tramo 1:** ✅ FINALIZADO (hace 7 días) - Contenedor en Depósito Mendoza
- **Tramo 2:** 🔵 ASIGNADO - Mendoza → Córdoba

#### 📝 Ruta PROPUESTA (1)

**Ruta 3** - Solicitud 3
- **Tramos:** 1 (Córdoba → Mendoza directo)
- **Estado:** `PROPUESTA`
- **Tramo 1:** 📋 ESTIMADO

#### ✅ Rutas FINALIZADAS (3) - **Para probar RF8 (Costo Real)**

**Ruta 4** - Solicitud 4 ⭐
- **Estado:** `FINALIZADA`
- **Costo Final:** $48,500
- **Tramos:** 1 (Córdoba → Buenos Aires)
- **Endpoint:** `GET /api/logistica/rutas/4/costo-real` ✅

**Ruta 5** - Solicitud 5 ⚠️
- **Estado:** `FINALIZADA`
- **Costo Final:** $108,500
- **Tramos:** 2 (Buenos Aires → Córdoba → Mendoza)
- **Endpoint:** `GET /api/logistica/rutas/5/costo-real` ✅

**Ruta 6** - Solicitud 6 🎯
- **Estado:** `FINALIZADA`
- **Costo Final:** $42,000
- **Tramos:** 1 (Córdoba → Mendoza)
- **Endpoint:** `GET /api/logistica/rutas/6/costo-real` ✅

---

## 🧪 Casos de Prueba Disponibles

### 🎯 FLUJO COMPLETO - Solicitud de Punta a Punta

**Escenario:** Cliente crea solicitud → Admin calcula rutas → Admin asigna ruta → Transportista opera → Cliente consulta

1. **CLIENTE - Crear Solicitud** (usar contenedor 8, 9 o 10)
   ```
   POST /api/solicitudes
   {
     "clienteId": 1,
     "contenedorId": 8,
     "origenId": 1,
     "destinoId": 2,
     "observaciones": "Nueva solicitud de prueba"
   }
   ```

2. **ADMIN - Calcular Rutas Alternativas**
   ```
   POST /api/logistica/costos/rutas-tentativas
   {
     "ubicacionOrigenId": 1,
     "ubicacionDestinoId": 2,
     "pesoKg": 450,
     "volumenM3": 2.8
   }
   ```
   **Respuesta esperada:**
   - Ruta 1: Directa (Córdoba → Mendoza)
   - Ruta 2: Con depósitos automáticos (incluye 24h estadía por depósito)

3. **ADMIN - Asignar Ruta a Solicitud**
   ```
   PATCH /api/solicitudes/{id}/ruta
   {
     "ubicacionOrigenId": 1,
     "ubicacionDestinoId": 2,
     "pesoKg": 450,
     "volumenM3": 2.8
   }
   ```

4. **TRANSPORTISTA - Asignar Camión**
   ```
   PATCH /api/logistica/tramos/{tramoId}/camion
   {
     "camionId": 1
   }
   ```

5. **TRANSPORTISTA - Iniciar y Finalizar Tramo**
   ```
   PATCH /api/logistica/tramos/{tramoId}/iniciar
   PATCH /api/logistica/tramos/{tramoId}/finalizar
   ```

6. **CLIENTE - Consultar Estado**
   ```
   GET /api/solicitudes/{id}
   GET /api/solicitudes/{id}/historial-estados
   ```

---

### 1️⃣ RF5 - Consultar Contenedores en Depósito

```bash
# Contenedor 1 esperando en Rosario (3 días)
# Costo de estadía: $600/día × 3 días = $1,800
GET /api/logistica/depositos/3/contenedores
```

```bash
# Contenedor 2 esperando en Mendoza (7 días)
# Costo de estadía: $450/día × 7 días = $3,150
# ⚠️ ATENCIÓN: Estadía prolongada
GET /api/logistica/depositos/2/contenedores
```

**Respuesta esperada:**
- Identificación del contenedor
- Solicitud asociada
- Fecha de llegada al depósito
- Días en depósito
- Costo acumulado de estadía

---

### 2️⃣ RF3 - Rutas Alternativas con Depósitos Automáticos

**Caso A: Carga Liviana**
```bash
POST /api/logistica/costos/rutas-tentativas
{
  "ubicacionOrigenId": 1,    # Córdoba
  "ubicacionDestinoId": 4,   # Buenos Aires
  "pesoKg": 280,
  "volumenM3": 1.5
}
```
**Resultado esperado:**
- **Ruta 1 (Directa):** Córdoba → Buenos Aires (710km, ~12h)
- **Ruta 2 (Con depósito):** Córdoba → [Depósito Rosario - 24h estadía - $600] → Buenos Aires
  - Tiempo total: ~11h viaje + 24h estadía = **35h**
  - Costo incluye: transporte + combustible + estadía

**Caso B: Carga Media**
```bash
POST /api/logistica/costos/rutas-tentativas
{
  "ubicacionOrigenId": 4,    # Buenos Aires
  "ubicacionDestinoId": 1,   # Córdoba
  "pesoKg": 1100,
  "volumenM3": 5.5
}
```

**Caso C: Ruta Larga**
```bash
POST /api/logistica/costos/rutas-tentativas
{
  "ubicacionOrigenId": 3,    # Rosario
  "ubicacionDestinoId": 2,   # Mendoza
  "pesoKg": 800,
  "volumenM3": 3.2
}
```
**Puede usar 1-2 depósitos intermedios:**
- Cada depósito suma 24h de tiempo
- Cada depósito suma su costo de estadía diaria

---

### 3️⃣ RF8 - Calcular Costo Real (Solo rutas FINALIZADAS)

```bash
# ⭐ Ruta eficiente (costo menor que estimado)
GET /api/logistica/rutas/4/costo-real
```
**Resultado esperado:**
- Costo estimado: $52,000
- Costo real: $48,500
- **Bonificación:** -$3,500
- Motivo: Llegó 40min antes

```bash
# ⚠️ Ruta con demoras (costo mayor que estimado)
GET /api/logistica/rutas/5/costo-real
```
**Resultado esperado:**
- Costo estimado: $95,000
- Costo real: $108,500
- **Penalización:** +$13,500
- Motivo: 3h de retraso + estadía
- Incluye: Costo de 24h en Depósito Córdoba ($500)

```bash
# 🎯 Ruta perfecta (costo exacto)
GET /api/logistica/rutas/6/costo-real
```
**Resultado esperado:**
- Costo estimado: $42,000
- Costo real: $42,000
- Sin ajustes

---

### 4️⃣ RN1 - Validación de Capacidad de Camión

```bash
# ✅ Caso válido (dentro de capacidad)
POST /api/logistica/camiones/validar-capacidad
{
  "camionId": 1,
  "pesoKg": 8000,
  "volumenM3": 40
}
```
**Resultado:** `{"valido": true}`

```bash
# ❌ Caso inválido (excede capacidad)
POST /api/logistica/camiones/validar-capacidad
{
  "camionId": 3,    # Capacidad: 8000kg / 40m³
  "pesoKg": 8500,   # Excede peso
  "volumenM3": 45   # Excede volumen
}
```
**Resultado:** `{"valido": false, "razon": "Excede capacidad de peso y volumen"}`

---

### 5️⃣ Seguimiento de Solicitud para el Cliente

```bash
# Cliente consulta el detalle de su solicitud
GET /api/solicitudes/1
```
**Información mostrada:**
- Estado actual
- Costos (estimado y final si está entregada)
- Tiempos (estimado y real si está entregada)
- Ruta asignada

```bash
# Cliente consulta historial completo de estados
GET /api/solicitudes/1/historial-estados
```
**Información mostrada:**
- Todos los cambios de estado
- Fechas de cada cambio
- Detalles de cada transición

---

### 6️⃣ Operatoria del Transportista

```bash
# Ver tramos asignados a mis camiones
GET /api/logistica/tramos?estado=ASIGNADO
```

```bash
# Ver tramos en progreso
GET /api/logistica/tramos?estado=EN_PROGRESO
```

```bash
# Asignar camión a tramo
PATCH /api/logistica/tramos/2/camion
{
  "camionId": 2
}
```

```bash
# Iniciar transporte
PATCH /api/logistica/tramos/2/iniciar
```

```bash
# Finalizar transporte
PATCH /api/logistica/tramos/2/finalizar
```

---

## 📝 Notas Importantes

- **Base de datos en memoria:** Los datos se resetean al reiniciar los servicios
- **Sincronización:** Las solicitudes de seguimiento-service están vinculadas con rutas de logistica-service
- **Estados válidos:** Solo rutas FINALIZADAS tienen costo real calculado
- **Autenticación:** Todos los endpoints requieren token JWT válido

---

## 🔄 Reiniciar Datos

Para recargar los datos de seed:

```bash
# Opción 1: Reiniciar servicios
docker compose restart logistica-service seguimiento-service

# Opción 2: Reconstruir contenedores (si modificaste el seed)
docker compose down logistica-service seguimiento-service
docker build -t tpi/logistica-service:latest ./logistica-service
docker build -t tpi/seguimiento-service:latest ./seguimiento-service
docker compose up -d
```
