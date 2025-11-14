# 📋 Estado de Implementación del Trabajo Práctico Integrador (TPI)

**Fecha de Revisión:** 2025-11-14

**Conclusión:** La arquitectura de Microservicios está **completa y funcional**. El proyecto cumple con la mayoría de los requisitos técnicos y de infraestructura del TPI. Los puntos pendientes son de **lógica de negocio específica** para el cálculo de costos.

---

## ✅ Lo Bien Implementado (Cumplimiento de Requisitos)

| Área de Cumplimiento | Requisito TPI Cumplido | Archivos Clave |
| :--- | :--- | :--- |
| **Arquitectura** | 3 Microservicios + API Gateway. | `docker-compose.yml`, `GatewayConfig.java`. |
| **Seguridad** | Keycloak, Validación JWT, Acceso Restringido por Rol. | `SecurityConfig.java`, `@PreAuthorize`, `realm-export.json`. |
| **API Externa** | Integración con API de Geolocalización (Distancias). | `GeoApiClient.java`, `geoapi-service` (incluye fallback Haversine). |
| **RF1 (Cliente)** | Registrar Solicitud (incl. creación dinámica de Cliente/Contenedor). | `SolicitudService.crearSolicitud`. |
| **RF6 & RF11** | Asignación de Camión + **Validación de Capacidad** (P/V). | `TramoService.asignarCamion`, `SeguimientoClient.obtenerContenedorDeSolicitud`. |
| **RF7 (Transportista)** | Registrar Inicio/Fin de Tramo. | `TramoController.iniciarTramo` / `finalizarTramo` (Restringido a `TRANSPORTISTA`). |
| **RF9** | Persistencia de Costo Final y Tiempo Real. | `TramoService.finalizarTramo` (Agregación), `SolicitudController.actualizarCostos` (Recepción). |
| **Seguimiento Cronológico** | Seguimiento de estados en orden. | Entidad `SolicitudCambioEstado` (Implementación de log de auditoría). |
| **Modelo de Datos** | Diseño de desacoplamiento (Database per Service) y *Snapshotting* de ubicaciones. | `Solicitud.java` (Almacena `origenId` + Snapshot de dirección/coordenadas). |

---

## ❌ Lo Pendiente (Cálculo de Costos y Planificación)
Los siguientes puntos son críticos para el 100% de cumplimiento del TPI, específicamente las reglas de negocio **RF3, RF8 y RN2**.

### 1. 🔴 Lógica de Cálculo de Ruta Tentativa (RF3 / RN4)

Falta toda la lógica de servicio para la cotización inicial.

| Requisito TPI | Acción Faltante |
| :--- | :--- |
| **RF3** Consultar rutas tentativas | Falta el *endpoint* y el método de servicio para recibir `CalcularRutaRequest` y devolver la ruta sugerida. |
| **RN4** Tarifa aproximada por promedio de camiones elegibles | Falta la lógica en `RutaService` para: **1)** Filtrar camiones elegibles (por P/V). **2)** Calcular el costo promedio/km y consumo promedio de ese subgrupo. **3)** Usar esos promedios para calcular el `costoEstimado` por tramo. |

### 2. 🔴 Cálculo de Costo Real Final (RF8 / RN2)

La fórmula de costo real en `CostoService.java` es incompleta y no cubre los componentes obligatorios del TPI.

| Componente de Costo TPI | Implementación Pendiente |
| :--- | :--- |
| **Costo por Estadía en Depósito** | Falta la lógica en `TramoService.finalizarTramo` para: **1)** Calcular la diferencia de días entre la llegada (tr. anterior) y la salida (tr. actual) en un depósito. **2)** Multiplicar los días por `Deposito.costoEstadiaDiaria` y sumarlo al costo total. |
| **Cargos de Gestión por Tramo** | Falta la lógica para obtener el cargo fijo por tramo (de la tabla de tarifas) y sumarlo al costo total de la ruta en `TramoService.finalizarTramo`. |

---

## ➡️ Próximo Paso Recomendado

El punto más crítico para completar el ciclo de negocio es implementar la **Lógica de Cálculo de Ruta Tentativa (RF3)**, ya que esta alimenta el campo `costoEstimado` de la `Solicitud`.

**Recomendación:** Implementar el *endpoint* `POST /rutas/calcular-tentativa` en `logistica-service`.
