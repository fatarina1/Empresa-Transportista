# 🚨 Funcionalidades FALTANTES según el Enunciado TPI

## ❌ REQUERIMIENTOS NO IMPLEMENTADOS

### 1. RF4: Asignar Ruta con Tramos a Solicitud ❌
**Enunciado requiere:**
- Crear ruta con múltiples tramos
- Especificar depósitos intermedios
- Calcular costos y tiempos por tramo

**Estado actual:**
- ❌ No existe endpoint para crear rutas
- ❌ No existe endpoint para crear tramos
- ❌ No hay relación Solicitud → Ruta → Tramos

**Necesitas crear:**
```java
// En logistica-service
POST /rutas
{
  "solicitudId": 1,
  "cantidadTramos": 2,
  "cantidadDepositos": 1
}

POST /tramos
{
  "rutaId": 1,
  "origenId": 1,
  "destinoId": 2,
  "orden": 1,
  "tipo": "DIRECTO|CON_DEPOSITO",
  "estado": "PENDIENTE|EN_PROGRESO|COMPLETADO",
  "distanciaKm": 350.5,
  "tiempoEstimadoMin": 240,
  "costoEstimado": 7500.0
}
```

---

### 2. RF5: Consultar Contenedores Pendientes de Entrega ❌
**Enunciado requiere:**
- Ver contenedores en estado EN_TRANSITO
- Ver ubicación actual del contenedor
- Filtrar por depósito

**Estado actual:**
- ❌ No existe endpoint para filtrar contenedores por estado
- ❌ No existe forma de saber en qué depósito está un contenedor
- ❌ No hay tracking de ubicación actual

**Necesitas crear:**
```java
// En seguimiento-service
GET /contenedores?estado=EN_TRANSITO
GET /contenedores?depositoId=1
GET /solicitudes/{id}/ubicacion-actual
GET /solicitudes/pendientes-entrega
```

---

### 3. RF6: Asignar Camión a Tramo ❌
**Enunciado requiere:**
- Asignar camión específico a un tramo
- Validar disponibilidad del camión
- Validar capacidad del camión
- Marcar camión como no disponible

**Estado actual:**
- ❌ No existe relación Tramo → Camión
- ❌ No existe endpoint para asignar camión
- ✅ Sí existe validación de capacidad (`verificar-disponibilidad`)

**Necesitas crear:**
```java
// En logistica-service
PATCH /tramos/{id}/asignar-camion
{
  "camionId": 1
}

// Debe:
// 1. Validar que el camión existe
// 2. Validar que está disponible
// 3. Validar capacidad vs contenedor
// 4. Asignar camión al tramo
// 5. Marcar camión como no disponible
```

---

### 4. RF7: Registrar Inicio/Fin de Tramo ❌
**Enunciado requiere:**
- Transportista registra inicio de tramo
- Transportista registra fin de tramo
- Actualizar estado del tramo
- Calcular tiempo real
- Liberar camión al finalizar

**Estado actual:**
- ❌ No existe endpoint para iniciar tramo
- ❌ No existe endpoint para finalizar tramo
- ❌ No se registran fechas reales

**Necesitas crear:**
```java
// En logistica-service
PATCH /tramos/{id}/iniciar
{
  "fechaHoraInicio": "2025-11-04T08:00:00"
}

PATCH /tramos/{id}/finalizar
{
  "fechaHoraFin": "2025-11-04T17:00:00"
}

// Debe:
// 1. Cambiar estado del tramo
// 2. Registrar fecha/hora
// 3. Calcular duración real
// 4. Liberar camión (disponible = true) al finalizar
// 5. Actualizar estado de solicitud si es último tramo
```

---

### 5. RF8: Calcular Costo Total del Envío ❌
**Enunciado requiere:**
- Cálculo detallado con fórmula específica:
  ```
  Costo Total = 
    Cargo de Gestión (por cantidad de tramos) +
    Costo por Km (distancia × tarifa volumen) +
    Costo Combustible (consumo × distancia × precio litro) +
    Costo Estadía (días × tarifa depósito) +
    Costo Base Camión
  ```

**Estado actual:**
- ❌ No existe endpoint para calcular costo
- ❌ No se almacena costo estimado vs costo real
- ❌ No se calcula estadía en depósitos
- ❌ No hay tarifa de combustible configurable

**Necesitas crear:**
```java
// En logistica-service
POST /calcular-costo
{
  "solicitudId": 1,
  "distanciaTotal": 700.5,
  "cantidadTramos": 2,
  "diasEnDeposito": 3,
  "volumenContenedor": 40.0,
  "camionId": 1
}

Response:
{
  "costoTotal": 85600.0,
  "desglose": {
    "cargoGestion": 200.0,      // 2 tramos × 100
    "costoPorKm": 8750.0,       // 700 × 12.5
    "costoCombustible": 36750.0, // 700 × 0.35 × 150
    "costoEstadia": 1500.0,      // 3 días × 500
    "costoBaseCamion": 35000.0   // 700 × 50
  }
}
```

---

### 6. Parámetros de Tarifación Configurables ❌
**Enunciado requiere:**
- Tarifa por km según volumen
- Valor del litro de combustible
- Consumo promedio general
- Costo de estadía por depósito
- Costo base y consumo por camión

**Estado actual:**
- ✅ Existe `TarifaVolumen` (costo por km según volumen)
- ❌ No existe parámetro global de precio de combustible
- ❌ No existe tarifa de estadía en `Deposito`
- ✅ Existe `costoKmBase` y `consumoPromedioLitrosKm` en `Camion`

**Necesitas crear:**
```java
// En logistica-service
@Entity
public class ParametroTarifacion {
    @Id
    private Long id;
    private String clave; // "PRECIO_COMBUSTIBLE", "CARGO_GESTION_POR_TRAMO"
    private BigDecimal valor;
}

// Endpoints
GET /parametros-tarifacion
POST /parametros-tarifacion
PUT /parametros-tarifacion/{id}

// Agregar a Deposito
private BigDecimal tarifaEstadiaDiaria;
```

---

### 7. Gestión de Estados de Solicitud ⚠️ PARCIAL
**Enunciado requiere:**
- Estados: [BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA]
- Transiciones controladas
- Timeline de cambios de estado

**Estado actual:**
- ✅ Existe enum con estados
- ✅ Existe endpoint para cambiar estado
- ❌ No hay validación de transiciones
- ❌ No hay timeline/historial de cambios

**Necesitas mejorar:**
```java
// En seguimiento-service
@Entity
public class CambioEstadoSolicitud {
    @Id
    private Long id;
    private Long solicitudId;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
    private String observaciones;
}

// Endpoint
GET /solicitudes/{id}/historial-estados
```

---

### 8. Relación Solicitud → Ruta → Tramos ❌
**Enunciado requiere:**
- Una solicitud tiene UNA ruta
- Una ruta tiene MÚLTIPLES tramos
- Tramos conectan origen → depósitos → destino

**Estado actual:**
- ❌ No existe entidad `Ruta`
- ❌ No existe entidad `Tramo`
- ❌ No existe relación entre ellas

**Necesitas crear:**
```java
// En logistica-service
@Entity
public class Ruta {
    @Id
    private Long id;
    private Long solicitudId;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private Double distanciaTotal;
    private Integer tiempoTotalMin;
    
    @OneToMany(mappedBy = "ruta")
    private List<Tramo> tramos;
}

@Entity
public class Tramo {
    @Id
    private Long id;
    
    @ManyToOne
    private Ruta ruta;
    
    @ManyToOne
    private Ubicacion origen;
    
    @ManyToOne
    private Ubicacion destino;
    
    @ManyToOne
    private Camion camion;
    
    private Integer orden; // 1, 2, 3...
    private String tipo; // DIRECTO, CON_DEPOSITO
    private String estado; // PENDIENTE, EN_PROGRESO, COMPLETADO
    private Double distanciaKm;
    private Integer tiempoEstimadoMin;
    private BigDecimal costoEstimado;
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
    private Integer tiempoRealMin;
    private BigDecimal costoReal;
}
```

---

### 9. Gestión de Transportistas ❌
**Enunciado requiere:**
- Rol TRANSPORTISTA
- Ver tramos asignados
- Registrar inicio/fin de tramo

**Estado actual:**
- ❌ No existe entidad `Transportista`
- ❌ Camión tiene solo `transportistaId` pero no está mapeado
- ❌ No existe endpoint para ver tramos de un transportista

**Necesitas crear:**
```java
// En logistica-service
@Entity
public class Transportista {
    @Id
    private Long id;
    private String nombre;
    private String telefono;
    private String documento;
    
    @OneToMany(mappedBy = "transportista")
    private List<Camion> camiones;
}

// Endpoints
GET /transportistas/{id}/tramos-asignados
GET /transportistas/{id}/tramos-pendientes
```

---

### 10. Integración con Keycloak (Seguridad) ❌
**Enunciado requiere:**
- Keycloak como proveedor de identidad
- Validación de token JWT en todos los servicios
- Endpoints restringidos por rol (CLIENTE, OPERADOR, TRANSPORTISTA)

**Estado actual:**
- ✅ Spring Security está en los servicios
- ❌ No está configurado Keycloak
- ❌ No hay validación de JWT
- ❌ No hay restricción por roles

**Necesitas configurar:**
1. Instalar y configurar Keycloak
2. Crear realms y roles
3. Configurar OAuth2 Resource Server en Spring
4. Agregar `@PreAuthorize` en controladores

---

### 11. Validaciones de Negocio Faltantes ⚠️

**Falta implementar:**
- [ ] No permitir asignar camión ocupado
- [ ] No permitir iniciar tramo sin camión asignado
- [ ] No permitir finalizar tramo sin haberlo iniciado
- [ ] No permitir cambiar estado de solicitud si hay tramos pendientes
- [ ] Validar que origen y destino sean diferentes
- [ ] Validar que fechas sean coherentes (fin > inicio)
- [ ] Validar capacidad de depósito al agregar contenedor

---

### 12. API Externa (Google Maps) ⚠️ PARCIAL
**Enunciado requiere:**
- Integración REAL con Google Maps Directions API
- Calcular distancias reales entre puntos
- Calcular tiempos reales de viaje

**Estado actual:**
- ✅ Existe GeoAPI con cálculo de distancia
- ⚠️ Usa fórmula de Haversine (no es Google Maps real)
- ❌ No calcula ruta óptima con múltiples puntos

**Necesitas:**
1. Obtener API Key de Google Maps
2. Integrar con Directions API
3. Calcular rutas con waypoints (depósitos intermedios)

---

## 📊 RESUMEN DE IMPLEMENTACIÓN

### ✅ Lo que SÍ tienes:
1. API Gateway funcionando
2. Microservicios independientes (seguimiento, logística, geoapi)
3. CRUD básico de entidades (clientes, contenedores, camiones, ubicaciones)
4. Validación de capacidad de camiones
5. Cálculo de distancia (Haversine)
6. Swagger en todos los servicios
7. Arquitectura de microservicios correcta

### ❌ Lo que FALTA:
1. **Rutas y Tramos** (RF4, RF6, RF7) - 🔴 CRÍTICO
2. **Cálculo de Costo Completo** (RF8) - 🔴 CRÍTICO
3. **Gestión de Transportistas** (RF7) - 🔴 CRÍTICO
4. **Timeline de Estados** (RF2, RF7) - 🟡 IMPORTANTE
5. **Consultas de Contenedores Pendientes** (RF5) - 🟡 IMPORTANTE
6. **Keycloak + JWT** (Seguridad) - 🔴 CRÍTICO
7. **Google Maps API Real** (API Externa) - 🟡 IMPORTANTE
8. **Validaciones de Negocio Completas** - 🟡 IMPORTANTE
9. **Docker Compose** (Despliegue) - 🔴 CRÍTICO
10. **Colección Postman Completa** - 🟡 IMPORTANTE

---

## 🎯 PRIORIDAD DE IMPLEMENTACIÓN

### FASE 1: Funcionalidad Core (CRÍTICO)
1. ✅ Crear entidades `Ruta` y `Tramo`
2. ✅ Endpoints para crear rutas y tramos (RF4)
3. ✅ Endpoint para asignar camión a tramo (RF6)
4. ✅ Endpoints iniciar/finalizar tramo (RF7)
5. ✅ Cálculo de costo completo con fórmula (RF8)
6. ✅ Agregar `transportistaId` funcional

### FASE 2: Seguridad (CRÍTICO)
1. ✅ Instalar y configurar Keycloak
2. ✅ Configurar JWT en todos los servicios
3. ✅ Restricción por roles en endpoints
4. ✅ Testing con tokens

### FASE 3: Consultas y Validaciones (IMPORTANTE)
1. ✅ Consultas de contenedores pendientes (RF5)
2. ✅ Timeline de estados
3. ✅ Validaciones de negocio completas
4. ✅ Manejo de errores mejorado

### FASE 4: Despliegue (CRÍTICO)
1. ✅ Docker Compose con todos los servicios
2. ✅ Colección Postman completa
3. ✅ Documentación final
4. ✅ Testing E2E

### FASE 5: Extras (OPCIONAL)
1. ⬜ Google Maps API real
2. ⬜ Frontend básico
3. ⬜ Monitoreo y logs avanzados

---

## 📝 Próximos Pasos Recomendados

1. **Crear entidades Ruta y Tramo** en logistica-service
2. **Implementar RF4, RF6, RF7** (asignar ruta y gestionar tramos)
3. **Implementar RF8** (cálculo de costo completo)
4. **Configurar Keycloak** y seguridad JWT
5. **Crear Docker Compose** para todo el sistema
6. **Testing exhaustivo** con Postman
7. **Documentar decisiones** y desafíos

¿Por cuál quieres empezar? 🚀
