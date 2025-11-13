# Resumen de cambios: Validación de capacidad al asignar camión

Fecha: 2025-11-12

Este archivo resume los cambios implementados para añadir la validación de capacidad (peso y
volumen) al asignar un camión a un tramo en el servicio `logistica-service`.

## Objetivo

Evitar la asignación de un camión que no tenga capacidad suficiente para transportar el
contenedor asociado a la solicitud. La validación comprueba peso y volumen consultando el
servicio `seguimiento-service` para obtener los datos del contenedor.

## Archivos añadidos

- `src/main/java/ar/edu/tpi/logistica/dto/ContenedorRemotoDTO.java`
  - DTO mínimo para mapear la respuesta remota del `seguimiento-service` con los campos:
    `id`, `identificacion`, `pesoKg`, `volumenM3`.

- `src/main/java/ar/edu/tpi/logistica/clients/SeguimientoClient.java`
  - Cliente HTTP (usa `RestClient.Builder`) que consulta `GET /solicitudes/{id}/detalle`
    en `seguimiento-service` y extrae el objeto `contenedor` para su validación.
  - Propiedad configurable: `seguimiento.service.url` (por defecto `http://localhost:8082`).

## Archivos modificados

- `src/main/java/ar/edu/tpi/logistica/services/TramoService.java`
  - Se inyectó `SeguimientoClient`.
  - En el método `asignarCamion(Long tramoId, Long camionId)` se añadió la lógica:
    1. Recuperar la `Ruta` asociada al `Tramo` y su `solicitudExternaId`.
    2. Consultar `seguimiento-service` para obtener `ContenedorRemotoDTO` de la solicitud.
    3. Validar:
       - `contenedor.pesoKg <= camion.capacidadPesoKg`
       - `contenedor.volumenM3 <= camion.capacidadVolumenM3`
    4. Si alguna validación falla, se lanza `IllegalArgumentException` (el controller debe mapearlo a `400 Bad Request`).
    5. Si la validación es correcta, se procede con la asignación (marcar camión no disponible y guardar el tramo).

## Comportamiento en caso de error de red

- Si no se puede obtener la información del contenedor desde `seguimiento-service` (error de red o respuesta inválida),
  la implementación actual aborta la asignación devolviendo `IllegalArgumentException`. Esto evita asignaciones inseguras.

## Configuración y cómo probarlo

1. Asegurarse de tener levantado `seguimiento-service` y que el endpoint `GET /solicitudes/{id}/detalle`
   devuelva un JSON que incluya la propiedad `contenedor` con `pesoKg` y `volumenM3`.
2. Ajustar la propiedad `seguimiento.service.url` si el servicio escucha en otra URL/puerto.
   - Se puede poner en `application.properties` o como variable de entorno:

   ```properties
   seguimiento.service.url=http://seguimiento:8082
   ```

3. Crear una `Ruta` con `solicitudExternaId` apuntando a una `Solicitud` válida en el servicio de seguimiento.
4. Intentar asignar un camión que no cumpla con las capacidades: la API debe devolver `400` con mensaje explicativo.

Ejemplo (ajustar host/puerto y rutas reales del controlador):

```bash
curl -X POST "http://localhost:8081/api/tramos/{tramoId}/asignar?camionId={camionId}"
```