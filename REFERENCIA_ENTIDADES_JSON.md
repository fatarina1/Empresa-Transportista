# 📋 Referencia Rápida de Entidades y JSONs

Documento de referencia para crear requests en Postman con los campos correctos de cada entidad.

---

## 🚛 Servicio de Logística

### 1. Camión (Camion)

**Endpoint:** `POST /api/logistica/camiones`  
**Autorización:** Solo ADMIN

```json
{
  "dominio": "AB123CD",
  "capacidadPesoKg": 25000.0,
  "capacidadVolumenM3": 80.0,
  "costoKmBase": 150.0,
  "consumoPromedioLitrosKm": 0.35,
  "disponible": true
}
```

**Campos:**
- ✅ `dominio` (String, 40 chars, único) - Patente del camión
- ✅ `capacidadPesoKg` (Double) - Capacidad máxima de peso en kg
- ✅ `capacidadVolumenM3` (Double) - Capacidad máxima de volumen en m³
- ✅ `costoKmBase` (Double) - Costo base por kilómetro
- ✅ `consumoPromedioLitrosKm` (Double) - Consumo promedio en litros/km
- ✅ `disponible` (Boolean) - Si está disponible para asignación
- ⚪ `transportistaId` (Long) - ID del transportista asignado (opcional)

---

### 2. Depósito (Deposito)

**Endpoint:** `POST /api/logistica/depositos`  
**Autorización:** Solo ADMIN

```json
{
  "nombre": "Depósito Central Norte",
  "ubicacionId": 1,
  "costoEstadiaDiaria": 250.0,
  "activo": true
}
```

**Campos:**
- ✅ `nombre` (String, 120 chars) - Nombre del depósito
- ✅ `ubicacionId` (Long) - ID de la ubicación donde está el depósito
- ✅ `costoEstadiaDiaria` (Double) - Costo diario de estadía
- ✅ `activo` (Boolean) - Si el depósito está operativo

---

### 3. Ubicación (Ubicacion)

**Endpoint:** `POST /api/logistica/ubicaciones`  
**Autorización:** Solo ADMIN

```json
{
  "nombre": "Buenos Aires Centro",
  "descripcion": "Centro de distribución CABA",
  "direccion": "Av. Corrientes 1500, CABA",
  "latitud": -34.603722,
  "longitud": -58.381592,
  "tipo": "DEPOSITO",
  "notas": "Horario: Lunes a Viernes 8-18hs"
}
```

**Campos:**
- ✅ `nombre` (String, 120 chars) - Nombre de la ubicación
- ✅ `latitud` (Double) - Coordenada GPS latitud
- ✅ `longitud` (Double) - Coordenada GPS longitud
- ⚪ `descripcion` (String, 120 chars) - Descripción breve
- ⚪ `direccion` (String, 240 chars) - Dirección postal
- ⚪ `tipo` (String, 30 chars) - Tipo: DEPOSITO, CLIENTE, etc.
- ⚪ `notas` (String, TEXT) - Notas adicionales

---

### 4. Tarifa por Volumen (TarifaVolumen)

**Endpoint:** `POST /api/logistica/tarifas`  
**Autorización:** Solo ADMIN

```json
{
  "volumenMinM3": 0.0,
  "volumenMaxM3": 10.0,
  "costoKmBase": 50.0
}
```

**Campos:**
- ✅ `volumenMinM3` (BigDecimal) - Volumen mínimo del rango en m³
- ✅ `volumenMaxM3` (BigDecimal) - Volumen máximo del rango en m³
- ✅ `costoKmBase` (BigDecimal) - Costo base por km para este rango

---

### 5. Ruta (Ruta)

**Endpoint:** `POST /api/logistica/rutas`  
**Autorización:** Solo ADMIN

```json
{
  "solicitudId": 1,
  "solicitudExternaId": 101,
  "estado": "PROPUESTA",
  "observaciones": "Ruta optimizada para delivery urgente"
}
```

**Campos:**
- ✅ `solicitudId` (Long) - ID interno de la solicitud
- ✅ `solicitudExternaId` (Long) - ID externo de la solicitud
- ⚪ `estado` (String) - PROPUESTA, ASIGNADA, EN_EJECUCION, FINALIZADA, CANCELADA
- ⚪ `cantidadTramos` (Integer) - Se calcula automáticamente
- ⚪ `cantidadDepositos` (Integer) - Se calcula automáticamente
- ⚪ `observaciones` (String, 400 chars) - Notas sobre la ruta

---

### 6. Tramo (Tramo)

**Endpoint:** `POST /api/logistica/tramos`  
**Autorización:** Solo ADMIN

```json
{
  "rutaId": 1,
  "orden": 1,
  "origenDepositoId": 1,
  "destinoDepositoId": 2,
  "origenLatitud": -34.603722,
  "origenLongitud": -58.381592,
  "destinoLatitud": -32.889459,
  "destinoLongitud": -68.845839,
  "tipo": "DEPOSITO_A_DEPOSITO",
  "estado": "PENDIENTE",
  "distanciaKm": 1050.5,
  "costoAproximado": 15750.0
}
```

**Campos:**
- ✅ `rutaId` (Long) - ID de la ruta padre
- ✅ `orden` (Integer) - Orden del tramo en la ruta
- ⚪ `origenDepositoId` (Long) - ID del depósito origen
- ⚪ `destinoDepositoId` (Long) - ID del depósito destino
- ⚪ `origenLatitud/origenLongitud` (Double) - Coordenadas origen
- ⚪ `destinoLatitud/destinoLongitud` (Double) - Coordenadas destino
- ⚪ `tipo` (String, 40 chars) - Tipo de tramo
- ⚪ `estado` (String, 30 chars) - Estado del tramo
- ⚪ `distanciaKm` (Double) - Distancia en kilómetros
- ⚪ `costoAproximado` (Double) - Costo estimado
- ⚪ `costoReal` (Double) - Costo real tras completar

---

## 📦 Servicio de Seguimiento

### 7. Cliente (Cliente)

**Endpoint:** `POST /api/seguimiento/clientes`  
**Autorización:** ADMIN

```json
{
  "nombre": "Empresa ABC S.A.",
  "email": "contacto@empresaabc.com",
  "telefono": "+54 261 4567890",
  "documento": "30-71234567-8",
  "direccion": "Av. San Martín 1000, Mendoza"
}
```

**Campos:**
- ✅ `nombre` (String, 120 chars) - Nombre o razón social
- ✅ `email` (String, 160 chars) - Email de contacto
- ⚪ `telefono` (String, 40 chars) - Teléfono
- ⚪ `documento` (String, 50 chars) - CUIT/DNI
- ⚪ `direccion` (String, 240 chars) - Dirección fiscal

---

### 8. Contenedor (Contenedor)

**Endpoint:** `POST /api/seguimiento/contenedores`  
**Autorización:** CLIENTE, ADMIN

```json
{
  "identificacion": "CONT-2025-001",
  "pesoKg": 1500.50,
  "volumenM3": 12.5,
  "largoM": 3.0,
  "anchoM": 2.0,
  "altoM": 2.1,
  "descripcion": "Productos electrónicos - frágil",
  "clienteId": 1
}
```

**Campos:**
- ✅ `identificacion` (String, 80 chars, único) - Código identificador
- ✅ `pesoKg` (BigDecimal) - Peso en kilogramos
- ✅ `volumenM3` (BigDecimal) - Volumen en metros cúbicos
- ✅ `clienteId` (Long) - ID del cliente propietario
- ⚪ `largoM` (BigDecimal) - Largo en metros
- ⚪ `anchoM` (BigDecimal) - Ancho en metros
- ⚪ `altoM` (BigDecimal) - Alto en metros
- ⚪ `descripcion` (String, 240 chars) - Descripción del contenido

---

### 9. Solicitud (Solicitud)

**Endpoint:** `POST /api/seguimiento/solicitudes`  
**Autorización:** CLIENTE, ADMIN

**Opción 1: Con IDs existentes**
```json
{
  "clienteId": 1,
  "contenedorId": 1,
  "origenId": 1,
  "destinoId": 2
}
```

**Opción 2: Creando cliente y contenedor nuevos**
```json
{
  "origenId": 1,
  "destinoId": 2,
  "clienteNombre": "Juan Pérez",
  "clienteEmail": "juan.perez@example.com",
  "clienteTelefono": "+54 9 261 5551234",
  "clienteDocumento": "20-12345678-9",
  "clienteDireccion": "Calle Falsa 123, Mendoza",
  "contenedorIdentificacion": "CONT-NEW-001",
  "contenedorPesoKg": 2500.75,
  "contenedorVolumenM3": 15.5,
  "contenedorLargoM": 3.5,
  "contenedorAnchoM": 2.2,
  "contenedorAltoM": 2.0,
  "contenedorDescripcion": "Maquinaria industrial"
}
```

**Campos obligatorios:**
- ✅ `origenId` (Long) - ID de ubicación origen
- ✅ `destinoId` (Long) - ID de ubicación destino
- ✅ `clienteId` O `clienteNombre + clienteEmail`
- ✅ `contenedorId` O `contenedorIdentificacion + contenedorPesoKg + contenedorVolumenM3`

La entidad `Solicitud` también almacena automáticamente:
- `numero` - Generado automáticamente (ej: SOL-2025-001)
- `estado` - PENDIENTE por defecto
- `origenDireccion`, `origenLatitud`, `origenLongitud` - Copiados de la ubicación
- `destinoDireccion`, `destinoLatitud`, `destinoLongitud` - Copiados de la ubicación
- `costoEstimado`, `tiempoEstimadoMin` - Calculados por el sistema

---

## 📝 Notas Importantes

### Tipos de datos:
- **String**: Texto con límite de caracteres
- **Long**: Número entero grande (IDs)
- **Integer**: Número entero (cantidades, tiempos)
- **Double**: Número decimal (distancias, costos)
- **BigDecimal**: Número decimal de alta precisión (pesos, volúmenes, dinero)
- **Boolean**: true o false
- **LocalDateTime**: Fecha y hora (generalmente auto-generado)

### Símbolos:
- ✅ Campo obligatorio (nullable = false)
- ⚪ Campo opcional (puede ser null)

### Convenciones:
- Los IDs no se envían en POST (se generan automáticamente)
- Los campos con `_id` son referencias a otras entidades
- Los timestamps (`createdAt`, `updatedAt`) se generan automáticamente
- Las coordenadas GPS usan notación decimal (ej: -34.603722, -58.381592)

---

## 🔐 Matriz de Autorización

| Entidad | GET | POST | PUT | DELETE |
|---------|-----|------|-----|--------|
| **Camión** | admin, transportista | admin | admin | admin |
| **Depósito** | admin | admin | admin | admin |
| **Ubicación** | admin | admin | admin | admin |
| **Tarifa** | admin | admin | admin | admin |
| **Ruta** | admin, transportista | admin | admin | admin |
| **Tramo** | admin, transportista | admin | admin | admin |
| **Cliente** | admin | admin | admin | admin |
| **Contenedor** | todos | cliente, admin | cliente, admin | admin |
| **Solicitud** | cliente, transportista, admin | cliente, admin | transportista, admin | admin |

---

## 🚀 Uso en Postman

1. **Importa** `POSTMAN_COLLECTION.json`
2. **Ejecuta** Login con el rol necesario (cliente01, admin01, transportista01)
3. **Copia** el JSON de ejemplo de este documento
4. **Pega** en el Body del request correspondiente
5. **Ajusta** los valores según tus necesidades
6. **Envía** el request

¡Los tokens se guardan automáticamente! 🎉
