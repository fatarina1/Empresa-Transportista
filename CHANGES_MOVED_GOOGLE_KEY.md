# Cambios: mover Google Maps API key fuera del repo

Fecha: 2025-11-13

Se realizaron los siguientes cambios para eliminar la API key de Google Maps del repositorio y
leerla desde la variable de entorno `GOOGLE_MAPS_APIKEY`:

- `geoapi/src/main/java/.../GeoService.java`:
  - Ahora lee la clave desde `@Value("${GOOGLE_MAPS_APIKEY:}")`.
  - Si la clave no está configurada lanza `IllegalStateException` para fallar rápido.

- `geoapi/src/main/resources/application.properties`:
  - Eliminada la línea `google.maps.apikey=...`.
  - Documentación añadida indicando que la clave se debe proporcionar por `GOOGLE_MAPS_APIKEY`.

- `infra/.env` y `infra/.env.example`:
  - `infra/.env` se actualizó para reemplazar la clave real por `REPLACE_WITH_YOUR_GOOGLE_MAPS_API_KEY`.
  - Se añadió `infra/.env.example` sin claves reales para que los desarrolladores la copien a `.env`.

- `geoapi/README.md`:
  - Actualizada la sección de configuración para indicar el uso de la variable de entorno `GOOGLE_MAPS_APIKEY`.

Recomendación inmediata:
- Si la API key encontrada en el repo es real, rotarla inmediatamente (crear nueva clave en Google Cloud
  Console y sustituir la antigua). No volver a subir claves a control de versiones.
