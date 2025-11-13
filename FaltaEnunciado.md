# Falta implementar según el Enunciado

Este documento lista y prioriza las funcionalidades, validaciones y artefactos que faltan o están sólo parcialmente implementados en el repositorio `tpi-microservices` con referencias a archivos actuales y sugerencias concretas de implementación.

## Resumen ejecutivo (prioridad)

1. Implementar cálculo y persistencia automática del costo estimado y costo real de una solicitud (ALTA).
2. Endpoint agregador para "rutas tentativas" que devuelva tramos con tiempos y costos estimados (ALTA).
3. Validación de capacidad (peso/volumen) al asignar camión a un tramo (ALTA).
4. Autorización por roles (CLIENTE / OPERADOR / TRANSPORTISTA) en rutas/acciones críticas (MEDIA/ALTA).
5. Endpoints para consultar contenedores en depósito y la ubicación actual del contenedor (MEDIA).
6. Componer un `docker-compose.yml` que levante todos los servicios (API Gateway, seguimiento, logística, geoapi, Keycloak) para despliegue integrado (ALTA).
7. Gestión segura de secretos (Google Maps API key) y eliminar claves embebidas en el repo (ALTA).
8. Tests automáticos (unit/integración) para reglas de negocio críticas: asignación, cálculo de costos, finalización de tramos (MEDIA).

---

## Detalle de lo que falta y por qué es necesario

1) Cálculo y persistencia del costo estimado y costeo final de la Solicitud

- Qué falta: no existe una rutina que agregue automáticamente los costos de los tramos y registre `Solicitud.costoEstimado` ni `Solicitud.costoFinal` cuando la ruta se asigna o se finaliza.
- Por qué: el enunciado exige calcular costo estimado (para mostrarlo al cliente) y costo real al finalizar. Sin esta agregación no se cumple la regla de negocio que exige registrar el costo final y tiempo real.
- Archivos relevantes: 
  - `seguimiento-service/src/main/java/ar/edu/tpi/seguimiento/entities/Solicitud.java`
  - `seguimiento-service/.../SolicitudService.java`
  - `logistica-service/.../TramoService.java`
  - `logistica-service/.../RutaService.java`
- Recomendación de implementación:
  - Crear un servicio en `logistica-service` (p. ej. `CostoService`) que, dado una `rutaId` o `solicitudId`, recorra tramos y sume: costo por km (según `TarifaVolumen` o `Camion.costo_km_base`), costo de combustible (consumoPromedioLitrosKm × distanciaKm × precio_litro), y costo de estadía por depósitos (días estimados × `Deposito.costoEstadiaDiaria`). Añadir además cargos de gestión por tramo.
  - Exponer endpoint `POST /rutas/{id}/calcular-costo` o `POST /rutas/calcular` para estimados y una rutina que al finalizar la última etapa de la ruta persista el resultado en la `Solicitud` asociada (posible llamada al `seguimiento-service` para actualizar `Solicitud.costoFinal` y `tiempoRealMin`).
  - Guardar `Tramo.costoReal` cuando se finaliza cada tramo; cuando todos los tramos estén `FINALIZADO`, sumar y persistir en `Solicitud.costoFinal`.

2) Endpoint agregador para rutas tentativas (Calcular rutas, tiempos y estimados)

- Qué falta: no hay en `logistica-service` un endpoint que reciba `CalcularRutaRequest` y devuelva la ruta completa con tramos, distancia y costo estimado (el `geoapi` sí calcula distancias entre 2 puntos, pero falta el agregador que arme la ruta y calcule costos).
- Archivos relevantes:
  - `logistica-service/src/main/java/ar/edu/tpi/logistica/dto/CalcularRutaRequest.java`
  - `geoapi/src/.../GeoApiController.java` y `GoogleMapsService.java` (proveen distancias/duración)
- Recomendación:
  - Implementar `POST /rutas/calcular` en `logistica-service` que:
    - Reciba `CalcularRutaRequest` (origen, destino, peso, volumen, depositosIntermedios)
    - Llama a `geoapi` (`/api/geoapi/calcular-ruta` o `/api/geoapi/distancia`) por cada tramo necesario
    - Para cada tramo: determinar distancia (km) y duración estimada (min)
    - Calcular costo aproximado por tramo usando `TarifaVolumen` y/o camiones elegibles (o promedio)
    - Devolver `RutaDTO` con tramos y suma de `costoEstimado` y `tiempoEstimadoMin`
  - Incluir pruebas unitarias que cubran: caso sin depósitos intermedios y con varios depósitos.

3) Validación de capacidad al asignar camión a tramo

- Qué falta: `TramoService.asignarCamion` sólo verifica disponibilidad booleana (`camion.getDisponible()`), no compara `Camion.capacidadPesoKg` y `Camion.capacidadVolumenM3` con las medidas del contenedor.
- Archivos relevantes:
  - `logistica-service/src/main/java/ar/edu/tpi/logistica/services/TramoService.java`
  - `seguimiento-service` (donde está el contenedor y su peso/volumen)
- Recomendación:
  - Consultar el `contenedor` asociado a la `solicitud` (o recibir peso/volumen en la llamada) antes de asignar. Dos opciones:
    1. `TramoService` hace una llamada HTTP al `seguimiento-service` para obtener `Contenedor` (cliente `RestClient`) y validar capacidades.
    2. El UI/operador obtiene la lista de camiones válidos usando `GET /camiones/verificar-disponibilidad?peso=X&volumen=Y` (ya existe) y luego asigna un `camionId` válido. Aun así, es recomendable duplicar la validación server-side en `asignarCamion`.
  - Si la validación falla, devolver `400 Bad Request` con mensaje claro.

4) Seguimiento: contenedores en depósito / ubicación actual

- Qué falta: no hay un endpoint directo para "contenedores actualmente en depósito" ni un campo de ubicación actual en `Contenedor`/`Solicitud` actualizable durante la ejecución.
- Archivos relevantes: `seguimiento-service` entidades `Contenedor`, `Solicitud`
- Recomendación:
  - Añadir `Solicitud.ubicacionActual` o `Contenedor.ubicacionActual` (puede ser `depositoId` o `en_transito`) y actualizarlo cuando se finalice un tramo que deje el contenedor en depósito o en destino.
  - Exponer `GET /depositos/{id}/contenedores` en `logistica-service` (o en `seguimiento-service`) usando los estados de las solicitudes/tramos.

5) Autorización por roles (Keycloak / Spring Security)

- Qué falta: la seguridad JWT está activada (resource servers), pero no se aplican restricciones por rol (p. ej. quién puede crear tarifas, asignar camiones, o un transportista iniciar/finalizar tramos).
- Archivos relevantes: `logistica-service/src/main/java/ar/edu/tpi/logistica/config/SecurityConfig.java`, `seguimiento-service/.../SecurityConfig.java`, `api-gateway/src/.../KeycloakAuthController.java`
- Recomendación:
  - Añadir reglas en `SecurityConfig` o `@PreAuthorize` en controladores: por ejemplo:
    - Operador/ADMIN: CRUD de depósitos, tarifas, camiones, asignación de camión a tramo.
    - Transportista: endpoints para `PATCH /tramos/{id}/iniciar` y `/finalizar` (solo si es transportista y es el camión asignado).
    - Cliente: crear solicitud (`POST /solicitudes`) y ver su solicitud por `clienteId`.
  - Documentar cómo crear roles y usuarios (incluir un `realm-export.json` para Keycloak si se desea reproducibilidad).

6) Despliegue integrado con Docker Compose

- Qué falta: `infra/docker-compose.yml` sólo levanta Keycloak. No hay compose para levantar todos los servicios con variables de entorno y red común.
- Recomendación:
  - Añadir servicios para `api-gateway`, `seguimiento-service`, `logistica-service`, `geoapi` en `infra/docker-compose.yml` (pueden construirse con `mvn package` y usar `openjdk:21-jdk` y `java -jar` o usar multistage Dockerfiles).
  - Inyectar variables: `SPRING_PROFILES_ACTIVE`, `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`, `GOOGLE_MAPS_APIKEY`, `LOGISTICA_SERVICE_URL` si es necesario.
  - Asegurar `depends_on` entre servicios (Keycloak levantarse antes que los services que validan tokens).

7) Gestión de secretos (Google Maps API key)

- Observación crítica: `geoapi/src/main/resources/application.properties` contiene una API key en texto plano (`google.maps.apikey=AIzaS...`). Esto debe evitarse.
- Recomendación:
  - Mover la API key a variable de entorno (`GOOGLE_MAPS_APIKEY`) y leer con `@Value("${GOOGLE_MAPS_APIKEY:}")` o `spring.config.import` desde Vault/archivo `.env` excluido del repo.
  - Eliminar la clave del repo y rotarla si es real.

8) Tests automáticos

- Qué falta: no vi tests que validen flujos críticos (asignación, cálculo de costo, finalización de ruta).
- Recomendación:
  - Añadir tests unitarios para `TramoService.asignarCamion` y `TramoService.finalizarTramo`.
  - Añadir tests de integración simples que levanten `logistica-service` (test slice o @SpringBootTest con perfiles H2) y mockear `geoapi` con `WireMock` o `MockRestServiceServer`.

---

## Ejemplos de cambios concretos (snippet de alto nivel)

- Validación en `TramoService.asignarCamion` (pseudo):

  1. Obtener `Solicitd` o `contenedorId` asociado al `tramo`.
  2. Llamar a `seguimiento-service` para obtener peso/volumen del contenedor (o recibirlos en request).
  3. Comparar con `camion.getCapacidadPesoKg()` y `camion.getCapacidadVolumenM3()`.
  4. Si no cumple -> lanzar IllegalArgumentException con mensaje "Camión no tiene capacidad suficiente".

- Endpoint `POST /rutas/calcular` (firma sugerida):

  Request: `CalcularRutaRequest` (ya existe en `logistica-service/dto`)
  Response: `RutaDTO` con lista de `TramoDTO` donde cada tramo contiene `distanciaKm`, `duracionMin`, `costoAproximado`.

---

## Prioridad y plan mínimo viable (MVP) para entrega

1. Añadir validación de capacidad en `assignarCamion` (rápido — 1 día). Tests unitarios.
2. Implementar `POST /rutas/calcular` que use `geoapi` y devuelva tramos + costo estimado (2–3 días).
3. Automatizar la agregación de `Solicitud.costoFinal` al finalizar todos los tramos de la ruta (1–2 días).
4. Completar `docker-compose` que levante Keycloak + microservicios (1–2 días). Documentar.
5. Añadir roles/authorities y restringir endpoints por rol (1 día).

---

## Notas finales y riesgos

- Evitar exponer claves reales en el repositorio. Rotar la clave si la que se subió fue real.
- Si se decide que `logistica-service` valide contenedores, habrá que añadir un cliente HTTP `SeguimientoServiceClient` y gestionar fallos (circuit breaker / timeouts) para no bloquear la asignación.
- El cálculo de costos tiene muchas variantes: acordar una fórmula (ej.: costo_km_base por tarifa de volumen + consumo × precio_litro + estadía diaria × días + cargo_gestion_por_tramo) y documentarla en README.

---

Si quieres, implemento ahora la tarea prioritaria que prefieras (p. ej. validación de capacidad en `TramoService.asignarCamion` o el endpoint `POST /rutas/calcular`) y corro el build para verificar que compila. Indica cuál quieres que haga primero.