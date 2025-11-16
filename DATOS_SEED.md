# 🌱 Datos de Seed - Base de Datos

Este documento describe todos los datos precargados en la base de datos para facilitar las pruebas.

---

## 📊 SEGUIMIENTO-SERVICE

### 👥 Clientes (3)

| ID | Nombre | Email | Teléfono | Documento |
|----|--------|-------|----------|-----------|
| 1 | Juan Pérez | juan.perez@email.com | 351-123-4567 | 30123456 |
| 2 | María González | maria.gonzalez@email.com | 261-987-6543 | 27987654 |
| 3 | Empresa Logística SA | contacto@logistica.com.ar | 341-555-1234 | CUIT 30-71234567-8 |

### 📦 Contenedores (7)

| ID | Identificación | Peso (kg) | Volumen (m³) | Cliente | Descripción |
|----|----------------|-----------|--------------|---------|-------------|
| 1 | CONT-001-2024 | 500.50 | 2.5 | Juan Pérez | Mercadería general - Electrónica |
| 2 | CONT-002-2024 | 1200.00 | 5.0 | María González | Alimentos no perecederos |
| 3 | CONT-003-2024 | 800.75 | 3.2 | Empresa Logística SA | Repuestos automotrices |
| 4 | CONT-004-2024 | 650.00 | 3.8 | Juan Pérez | Equipamiento médico |
| 5 | CONT-005-2024 | 1450.00 | 6.5 | María González | Maquinaria industrial |
| 6 | CONT-006-2024 | 320.00 | 2.0 | Empresa Logística SA | Productos farmacéuticos |
| 7 | CONT-007-2024 | 890.00 | 4.2 | Juan Pérez | Textiles y confecciones |

### 📋 Solicitudes (7)

#### 🚚 Solicitudes EN TRÁNSITO (2)

**Solicitud 1** - `SOL-2024-001`
- **Cliente:** Juan Pérez
- **Ruta:** Córdoba → Rosario → Buenos Aires
- **Estado:** `EN_TRANSITO`
- **Situación:** ⏳ **Contenedor esperando en Depósito Rosario hace 3 días**
- **Costo estimado:** $45,000
- **Tiempo estimado:** 12 horas

**Solicitud 2** - `SOL-2024-002`
- **Cliente:** María González
- **Ruta:** Buenos Aires → Mendoza → Córdoba
- **Estado:** `EN_TRANSITO`
- **Situación:** ⏳ **Contenedor esperando en Depósito Mendoza hace 7 días**
- **Costo estimado:** $78,000
- **Tiempo estimado:** 20 horas

#### 📝 Solicitud PROGRAMADA (1)

**Solicitud 3** - `SOL-2024-003`
- **Cliente:** Empresa Logística SA
- **Ruta:** Córdoba → Mendoza (directa)
- **Estado:** `PROGRAMADA`
- **Costo estimado:** $35,000
- **Tiempo estimado:** 10 horas

#### ✅ Solicitudes ENTREGADAS (3)

**Solicitud 4** - `SOL-2024-004` ⭐ **(Eficiente)**
- **Cliente:** Juan Pérez
- **Ruta:** Córdoba → Buenos Aires
- **Estado:** `ENTREGADA` (hace 2 días)
- **Costo:** $48,500 (estimado: $52,000) ✅ **Menor que estimado**
- **Tiempo:** 11h 20min (estimado: 12h) ✅ **Más rápido**
- **Observaciones:** Entrega completada exitosamente

**Solicitud 5** - `SOL-2024-005` ⚠️ **(Con demoras)**
- **Cliente:** María González
- **Ruta:** Buenos Aires → Mendoza
- **Estado:** `ENTREGADA` (hace 5 días)
- **Costo:** $108,500 (estimado: $95,000) ⚠️ **Mayor por demoras**
- **Tiempo:** 23h (estimado: 20h) ⚠️ **Más lento**
- **Observaciones:** Entrega completada con demoras por tráfico

**Solicitud 6** - `SOL-2024-006` 🎯 **(Perfecta)**
- **Cliente:** Empresa Logística SA
- **Ruta:** Córdoba → Mendoza
- **Estado:** `ENTREGADA` (hace 7 días)
- **Costo:** $42,000 (estimado: $42,000) 🎯 **Exacto**
- **Tiempo:** 10h (estimado: 10h) 🎯 **Perfecto**
- **Observaciones:** Entrega impecable

#### 📄 Solicitud BORRADOR (1)

**Solicitud 7** - `SOL-2024-007`
- **Cliente:** Juan Pérez
- **Ruta:** Rosario → Córdoba
- **Estado:** `BORRADOR` (creada hace 4 horas)
- **Costo estimado:** $28,000
- **Tiempo estimado:** 8 horas

---

## 🚛 LOGISTICA-SERVICE

### 🚚 Camiones (3)

| ID | Dominio | Capacidad Peso | Capacidad Volumen | Costo/km | Consumo L/km | Disponible |
|----|---------|----------------|-------------------|----------|--------------|------------|
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

### 1️⃣ RF5 - Consultar Contenedores en Depósito

```bash
# Contenedor 1 esperando en Rosario (3 días)
GET /api/logistica/depositos/3/contenedores
```

```bash
# Contenedor 2 esperando en Mendoza (7 días)
GET /api/logistica/depositos/2/contenedores
```

### 2️⃣ RF8 - Calcular Costo Real (Solo rutas FINALIZADAS)

```bash
# Ruta eficiente (costo menor que estimado)
GET /api/logistica/rutas/4/costo-real
```

```bash
# Ruta con demoras (costo mayor que estimado)
GET /api/logistica/rutas/5/costo-real
```

```bash
# Ruta perfecta (costo exacto)
GET /api/logistica/rutas/6/costo-real
```

### 3️⃣ RF7 - Finalizar Tramos

Para que funcione el endpoint de costo real en rutas 1 y 2, primero debes finalizar todos sus tramos:

```bash
# Finalizar tramo 2 de ruta 1 (Rosario → Buenos Aires)
PATCH /api/logistica/tramos/2/iniciar
PATCH /api/logistica/tramos/2/finalizar

# Luego consultar costo real
GET /api/logistica/rutas/1/costo-real
```

### 4️⃣ Flujo Completo de Solicitud

1. **Crear nueva solicitud** como CLIENTE
2. **Generar ruta** como ADMIN
3. **Asignar camiones** a tramos como TRANSPORTISTA
4. **Iniciar y finalizar tramos** como TRANSPORTISTA
5. **Consultar costo real** como ADMIN

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
