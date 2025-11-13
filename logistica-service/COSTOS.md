# Resumen de cambios: Cálculo y persistencia de costos

Fecha: 2025-11-13

Este documento resume las modificaciones implementadas para calcular el costo real de los tramos,
agregarlo a la ruta y persistir el `costoFinal` y `tiempoRealMin` en la `Solicitud` del servicio de
seguimiento.

## Objetivo

Automatizar el cálculo del costo real de cada `Tramo` al finalizarlo y, cuando todos los tramos de
una `Ruta` estén finalizados, sumar esos costos y notificar al `seguimiento-service` para que
persista `Solicitud.costoFinal` y `Solicitud.tiempoRealMin`.

## Cambios técnicos realizados

- Nuevo servicio en `logistica-service`:
  - `src/main/java/ar/edu/tpi/logistica/services/CostoService.java`
    - Calcula el `costoReal` de un tramo usando la fórmula simplificada:
      - `costo_km = camion.costoKmBase * distanciaKm`
      - `costo_combustible = camion.consumoPromedioLitrosKm * distanciaKm * VALOR_LITRO_COMBUSTIBLE`
      - `costoReal = costo_km + costo_combustible`
    - `VALOR_LITRO_COMBUSTIBLE` es configurable vía propiedad/variable de entorno.

- Cliente HTTP en `logistica-service`:
  - `src/main/java/ar/edu/tpi/logistica/clients/SeguimientoClient.java`
    - Método nuevo `actualizarCostosSolicitud(Long solicitudId, BigDecimal costoFinal, Integer tiempoRealMin, BigDecimal costoEstimado)`
      que hace `PATCH /solicitudes/{id}/costos` en `seguimiento-service`.

- Lógica de orquestación en `logistica-service`:
  - `src/main/java/ar/edu/tpi/logistica/services/TramoService.java` (método `finalizarTramo`)
    - Al finalizar un tramo se: registra `fechaFinReal`, marca estado `FINALIZADO`, calcula `costoReal`
      si hay camión asignado (vía `CostoService`) y libera el camión.
    - Guarda el `Tramo` con `costoReal`.
    - Comprueba si todos los tramos de la `Ruta` están finalizados; si es así:
      - Suma `Tramo.costoReal` de la ruta.
      - Suma los minutos reales de cada tramo para obtener `tiempoRealMinTotal`.
      - Llama a `SeguimientoClient.actualizarCostosSolicitud(...)` para persistir `costoFinal` y `tiempoRealMin` en `seguimiento-service`.

- Cambios en `seguimiento-service` (nuevo endpoint para aceptar actualizaciones de costos):
  - `src/main/java/ar/edu/tpi/seguimiento/controllers/SolicitudController.java`
    - `PATCH /solicitudes/{id}/costos` que acepta un body con `costoFinal`, `tiempoRealMin` y `costoEstimado`.
  - `src/main/java/ar/edu/tpi/seguimiento/services/SolicitudService.java`
    - Nuevo método `actualizarCostos(Long id, BigDecimal costoFinal, Integer tiempoRealMin, BigDecimal costoEstimado)`
      que persiste los valores en la entidad `Solicitud`.

## Comportamiento y supuestos

- La fórmula de costo es deliberadamente simple y **no** incorpora actualmente:
  - Costos por estadía en depósitos.
  - Tarifas variables por volumen/peso más allá del `costoKmBase` del camión.
  - Cargos adicionales por tramo o administración.
- Si la llamada al `seguimiento-service` falla, el flujo registra la falla y continúa; la notificación
  de actualización de la `Solicitud` puede necesitar reintentos o un mecanismo de compensación.
- El `seguimiento-service` espera recibir la llamada en la URL configurada por `seguimiento.service.url`
  en `logistica-service` (por defecto `http://localhost:8082` en la configuración agregada).

## Configuración relevante

- Propiedad/variable usada por `CostoService`:
  - `VALOR_LITRO_COMBUSTIBLE` (valor por defecto `450.0`) — se puede setear en `application.properties` o en el `docker-compose`.
- Endpoint de notificación en `seguimiento-service`:
  - `PATCH /solicitudes/{id}/costos` (body JSON con campos opcionales `costoFinal`, `tiempoRealMin`, `costoEstimado`).

## Cómo probar manualmente

1. Levantar los servicios `seguimiento-service` y `logistica-service` (local o via `docker-compose`).
2. Crear una `Solicitud` en `seguimiento-service` (POST `/solicitudes`) y anotar su `id`.
3. En `logistica-service` crear una `Ruta` que tenga `solicitudExternaId` con el id anterior y crear 1+ `Tramo` asociados.
4. Crear un `Camion` con `costoKmBase` y `consumoPromedioLitrosKm` y asignarlo al tramo (usando el endpoint existente para asignación).
5. Llamar al endpoint que inicia y luego finaliza el tramo (según los controladores existentes).
6. Verificar en la BD de `logistica-service` que `Tramo.costoReal` fue guardado.
7. Si todos los tramos de la `Ruta` fueron finalizados, verificar en `seguimiento-service` que `GET /solicitudes/{id}` refleja `costoFinal` y `tiempoRealMin` actualizados.

## Recomendaciones / siguientes pasos

- Añadir pruebas unitarias y de integración:
  - Unit tests para `CostoService` (varios escenarios: distancia nula, camion sin datos, etc.).
  - Unit tests para `TramoService.finalizarTramo` (mockear `CamionRepository`, `TramoRepository`, `SeguimientoClient`).
- Extender la fórmula de costos para incluir:
  - Tarifas por volumen/peso (tabla `TarifaVolumen` o atributos en `Camion`).
  - Estadías en depósitos (costo por día por depósito).
- Implementar retries / circuit-breaker (Resilience4j) para llamadas a `seguimiento-service`.
- Considerar usar eventos (Kafka/RabbitMQ) para desacoplar la notificación de actualización de la `Solicitud`.

---

Si querés, creo ahora los tests unitarios para `CostoService` y `TramoService.finalizarTramo`.
