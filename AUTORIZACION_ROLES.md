# Resumen de cambios — Autorización por roles

Este documento resume los cambios realizados para añadir autorización por roles (Keycloak -> Spring Security) en los servicios `logistica-service` y `seguimiento-service`.

## Objetivo

Habilitar comprobaciones por rol en endpoints críticos para cumplir con los requisitos del TPI: distinguir permisos de `CLIENTE`, `ADMIN` (que unifica "Operador/Administrador") y `TRANSPORTISTA`.

## Cambios principales realizados

- Habilitación de seguridad por método:
  - Se añadió `@EnableMethodSecurity` en los `SecurityConfig` de los servicios.

- Mapeo de roles desde el JWT de Keycloak:
  - Se agregó un `JwtAuthenticationConverter` en `SecurityConfig` para extraer `realm_access.roles` y mapearlos a autoridades Spring con prefijo `ROLE_`.
  - Archivos modificados:
    - `logistica-service/src/main/java/ar/edu/tpi/logistica/config/SecurityConfig.java`
    - `seguimiento-service/src/main/java/ar/edu/tpi/seguimiento/config/SecurityConfig.java`

- Restricciones por rol en controladores (anotaciones `@PreAuthorize`):
  - `logistica-service`:
    - `TramoController`:
      - Crear / Actualizar / Asignar camión / Eliminar → rol `ADMIN`.
      - Iniciar / Finalizar tramo → roles `TRANSPORTISTA` o `ADMIN`.
    - `CamionController`: crear/actualizar/eliminar → `ADMIN`.
    - `DepositoController`: crear/actualizar/eliminar → `ADMIN`.
    - `TarifaVolumenController`: crear/actualizar/eliminar → `ADMIN`.
  - `seguimiento-service`:
    - `SolicitudController`:
      - Crear solicitud → `CLIENTE` o `ADMIN`.
      - Actualizar estado / actualizar costos / eliminar → `ADMIN`.

## Archivos creados / modificados

- Modificados:
  - `logistica-service/src/main/java/ar/edu/tpi/logistica/config/SecurityConfig.java`
  - `seguimiento-service/src/main/java/ar/edu/tpi/seguimiento/config/SecurityConfig.java`
  - `logistica-service/src/main/java/ar/edu/tpi/logistica/controllers/TramoController.java`
  - `logistica-service/src/main/java/ar/edu/tpi/logistica/controllers/CamionController.java`
  - `logistica-service/src/main/java/ar/edu/tpi/logistica/controllers/DepositoController.java`
  - `logistica-service/src/main/java/ar/edu/tpi/logistica/controllers/TarifaVolumenController.java`
  - `seguimiento-service/src/main/java/ar/edu/tpi/seguimiento/controllers/SolicitudController.java`

## Notas técnicas y advertencias

- **Consolidación de Roles:** El rol `Operador/Administrador` del TPI se implementa bajo el único rol `ADMIN` de Keycloak. Se eliminaron las referencias al rol `OPERADOR` para evitar redundancia y alinearse con el `realm-export.json`.
- El convertidor JWT asume que Keycloak expone roles en el claim `realm_access.roles`.
- El uso de `.oauth2ResourceServer(oauth2 -> oauth2.jwt().jwtAuthenticationConverter(...))` puede reportar advertencias de deprecación; revisar y actualizar a la API recomendada si la versión de Spring lo requiere.
- Se añadieron casts y uso de `Map`/`Collection` sin tipado genérico en el conversor JWT; revisar con el compilador y corregir si su IDE o linter exige seguridad de tipos estricta.

## Cómo probar localmente

1.  Levantar Keycloak con un realm que contenga los roles: `cliente`, `transportista`, `admin`.
2.  Crear usuarios y asignarles roles apropiados (o usar `realm-export.json` en `infra/keycloak`).
3.  Generar un token JWT y añadirlo en `Authorization: Bearer <token>` a las solicitudes.
4.  Probar endpoints protegidos, por ejemplo:

```powershell
# Llamar endpoint protegido
curl -H "Authorization: Bearer <token_con_rol_ADMIN>" -X PATCH \
  http://localhost:8081/api/tramos/1/asignar-camion -d '{"camionId":1}' -H "Content-Type: application/json"