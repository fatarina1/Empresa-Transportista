# 📋 Tareas Pendientes del TPI - Backend de Aplicaciones

**Fecha:** 15 de Noviembre de 2025  
**Proyecto:** Sistema de Logística de Transporte de Contenedores

---

## 🎯 Resumen Ejecutivo

Este documento lista todas las funcionalidades que faltan implementar para cumplir completamente con el enunciado del TPI. Cada tarea incluye un **prompt específico** que puedes usar con el asistente para resolverla.

**Estado Actual:** 65% completado  
**Prioridad:** Completar tareas CRÍTICAS antes de la entrega

---

## 🔴 TAREAS CRÍTICAS (Obligatorias para aprobar)

### 1. Implementar Cálculo de Costos Estimados (RF3)

**Descripción:**  
Crear endpoint que calcule rutas tentativas con costos aproximados antes de asignar la solicitud. Debe usar tarifas por volumen y promedios de camiones elegibles.

**Archivos a modificar:**
- `logistica-service/src/main/java/ar/edu/tpi/logistica/services/RutaService.java`
- `logistica-service/src/main/java/ar/edu/tpi/logistica/controllers/RutaController.java`
- `logistica-service/src/main/java/ar/edu/tpi/logistica/services/CostoService.java` (crear nuevo)

**Prompt para el asistente:**
```
Implementa el endpoint POST /api/rutas/calcular-tentativa en logistica-service que:
1. Reciba un CalcularRutaRequest con origen, destino, lista de depositosIntermedios, pesoKg y volumenM3 del contenedor
2. Llame a GeoAPI para calcular la distancia de cada tramo (origen->deposito1, deposito1->deposito2, ..., ultimoDeposito->destino)
3. Filtre camiones elegibles según peso y volumen del contenedor
4. Calcule el costo aproximado por tramo usando:
   - Tarifa base por volumen (TarifaVolumen)
   - Consumo promedio de combustible de camiones elegibles × distancia × VALOR_LITRO_COMBUSTIBLE
   - Costo promedio por km de camiones elegibles × distancia
5. Devuelva un RutaTentativaDTO con:
   - Lista de tramos con distancias y costos individuales
   - costoEstimadoTotal
   - tiempoEstimadoTotal (en minutos)
   - cantidadTramos
   - cantidadDepositos

Usa RestClient para llamar a GeoAPI. Incluye manejo de errores si GeoAPI no responde.
```

---

### 2. Implementar Cálculo de Costos Reales (RF8, RN2)

**Descripción:**  
Completar la lógica de cálculo de costo real cuando se finalizan todos los tramos de una ruta. Debe incluir todos los componentes del enunciado.

**Archivos a modificar:**
- `logistica-service/src/main/java/ar/edu/tpi/logistica/services/CostoService.java` (crear)
- `logistica-service/src/main/java/ar/edu/tpi/logistica/services/TramoService.java`
- `seguimiento-service/src/main/java/ar/edu/tpi/seguimiento/controllers/SolicitudController.java`

**Prompt para el asistente:**
```
Implementa el cálculo de costo real en logistica-service que incluya:

1. Crea CostoService.java con el método calcularCostoReal(Long rutaId) que:
   - Obtenga todos los tramos de la ruta ordenados
   - Para cada tramo FINALIZADO calcule:
     a) Costo por kilómetro del camión específico × distancia real
     b) Costo de combustible: camion.consumoPromedioLitrosKm × distanciaKm × VALOR_LITRO_COMBUSTIBLE
     c) Costo de estadía en depósito si aplica:
        - Si el tramo termina en depósito: calcular días entre fechaFinReal de este tramo y fechaInicioReal del siguiente tramo
        - Multiplicar días por deposito.costoEstadiaDiaria
   - Sume un cargo de gestión fijo por tramo (definir en TarifaVolumen o constante)
   - Retorne un CostoRealDTO con:
     - costoTotal
     - detalles por componente (costoPorKm, costoCombustible, costoEstadias, cargosGestion)

2. Modifica TramoService.finalizarTramo() para que:
   - Al finalizar un tramo, verifique si es el último de la ruta
   - Si es el último, llame a CostoService.calcularCostoReal()
   - Actualice la Ruta con el costoFinal calculado
   - Llame a seguimiento-service (crear endpoint PATCH /solicitudes/{id}/costos) para actualizar Solicitud.costoFinal y tiempoReal

3. Crea en seguimiento-service el endpoint PATCH /api/solicitudes/{id}/costos que:
   - Reciba { costoFinal, tiempoRealMin }
   - Actualice la solicitud
   - Devuelva la solicitud actualizada

Incluye logs detallados del cálculo y valida que todos los tramos estén FINALIZADO antes de calcular.
```

---

### 3. Validar Capacidad al Asignar Camión (RN1)

**Descripción:**  
Mejorar TramoService.asignarCamion() para validar que el camión tenga capacidad suficiente para el contenedor de la solicitud.

**Archivos a modificar:**
- `logistica-service/src/main/java/ar/edu/tpi/logistica/services/TramoService.java`
- `logistica-service/src/main/java/ar/edu/tpi/logistica/clients/SeguimientoServiceClient.java` (crear)

**Prompt para el asistente:**
```
Mejora la validación de asignación de camión en logistica-service:

1. Crea SeguimientoServiceClient.java que use RestClient para:
   - Método obtenerContenedorPorSolicitudId(Long solicitudId) que llame a GET seguimiento-service/api/solicitudes/{id}
   - Extraiga el contenedorId de la respuesta
   - Llame a GET seguimiento-service/api/contenedores/{contenedorId}
   - Retorne un ContenedorDTO con pesoKg y volumenM3

2. Modifica TramoService.asignarCamion(Long tramoId, Long camionId) para:
   - Obtener el tramo y validar que existe
   - Obtener la ruta asociada al tramo
   - Obtener la solicitudId de la ruta
   - Llamar a SeguimientoServiceClient.obtenerContenedorPorSolicitudId()
   - Validar:
     - camion.capacidadPesoKg >= contenedor.pesoKg
     - camion.capacidadVolumenM3 >= contenedor.volumenM3
   - Si no cumple, lanzar IllegalArgumentException con mensaje descriptivo
   - Si cumple, continuar con la asignación normal

3. Configura la URL de seguimiento-service en application.properties:
   SEGUIMIENTO_SERVICE_URL=${SEGUIMIENTO_URL:http://localhost:8081}

Incluye manejo de errores si seguimiento-service no responde (log error y lanzar exception).
```

---

### 4. Crear Docker Compose Completo

**Descripción:**  
Actualizar docker-compose.yml para levantar todos los servicios con un solo comando.

**Archivos a modificar:**
- `tpi-microservices/infra/docker-compose.yml`

**Prompt para el asistente:**
```
Actualiza tpi-microservices/infra/docker-compose.yml para incluir todos los servicios:

1. Mantén el servicio keycloak actual

2. Agrega servicio api-gateway:
   - build: context: ../api-gateway
   - ports: "8080:8080"
   - environment:
     - SPRING_PROFILES_ACTIVE=default
     - KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/tpi-backend
     - LOGISTICA_URL=http://logistica-service:8082
     - SEGUIMIENTO_URL=http://seguimiento-service:8081
     - GEOAPI_URL=http://geoapi:8083
   - depends_on: keycloak, logistica-service, seguimiento-service, geoapi
   - networks: app-net

3. Agrega servicio logistica-service:
   - build: context: ../logistica-service
   - ports: "8082:8082" (solo para debug, comentado en producción)
   - environment:
     - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/tpi-backend
     - GEOAPI_URL=http://geoapi:8083
     - SEGUIMIENTO_URL=http://seguimiento-service:8081
     - VALOR_LITRO_COMBUSTIBLE=450.0
   - depends_on: keycloak, geoapi
   - networks: app-net

4. Agrega servicio seguimiento-service:
   - build: context: ../seguimiento-service
   - ports: "8081:8081" (solo para debug, comentado)
   - environment:
     - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/tpi-backend
     - LOGISTICA_URL=http://logistica-service:8082
   - depends_on: keycloak, logistica-service
   - networks: app-net

5. Agrega servicio geoapi:
   - build: context: ../geoapi
   - ports: "8083:8083" (solo para debug, comentado)
   - environment:
     - GOOGLE_MAPS_APIKEY=${GOOGLE_MAPS_APIKEY}
     - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/tpi-backend
   - depends_on: keycloak
   - networks: app-net

6. Asegúrate de:
   - Usar la red app-net (bridge) para todos
   - Agregar healthchecks a cada servicio
   - Configurar restart: unless-stopped

7. Crea un archivo .env.example en infra/ con:
   GOOGLE_MAPS_APIKEY=TU_API_KEY_AQUI
   VALOR_LITRO_COMBUSTIBLE=450.0

No incluyas la API key real en el archivo. Agrega .env al .gitignore.
```

---

### 5. Agregar Documentación Swagger Completa

**Descripción:**  
Documentar todos los endpoints de logistica-service y seguimiento-service con anotaciones OpenAPI.

**Archivos a modificar:**
- Todos los controladores de logistica-service
- Todos los controladores de seguimiento-service

**Prompt para el asistente:**
```
Agrega documentación Swagger/OpenAPI a todos los controladores en logistica-service y seguimiento-service:

1. Para cada controlador, agrega:
   - @Tag(name = "Nombre", description = "Descripción del recurso")
   - En cada endpoint:
     - @Operation(summary = "Resumen", description = "Descripción detallada")
     - @ApiResponse para códigos de respuesta (200, 201, 400, 404, etc.)
     - @Parameter para parámetros de path/query

2. Controladores en logistica-service:
   - CamionController: Tag "Camiones", descripción "Gestión de camiones y transportistas"
   - DepositoController: Tag "Depósitos", descripción "Gestión de depósitos de almacenamiento temporal"
   - RutaController: Tag "Rutas", descripción "Planificación y gestión de rutas de transporte"
   - TramoController: Tag "Tramos", descripción "Gestión de tramos individuales de rutas"
   - TarifaVolumenController: Tag "Tarifas", descripción "Configuración de tarifas por volumen"
   - UbicacionController: Tag "Ubicaciones", descripción "Gestión de ubicaciones geográficas"

3. Controladores en seguimiento-service:
   - ClienteController: Tag "Clientes", descripción "Gestión de clientes"
   - ContenedorController: Tag "Contenedores", descripción "Gestión de contenedores"
   - SolicitudController: Tag "Solicitudes", descripción "Gestión de solicitudes de transporte"

4. Agrega en cada servicio un OpenApiConfig.java con:
   @OpenAPIDefinition con info (title, version, description, contact)
   Configuración de seguritySchemes para JWT

Usa como referencia la implementación de GeoApiController que ya tiene documentación completa.
```

---

## 🟡 TAREAS IMPORTANTES (Recomendadas)

### 6. Implementar Consulta de Contenedores en Depósito

**Descripción:**  
Crear endpoint para ver qué contenedores están actualmente en cada depósito.

**Prompt para el asistente:**
```
Implementa en logistica-service el endpoint GET /api/depositos/{depositoId}/contenedores:

1. Crea un método en TramoService.obtenerContenedoresEnDeposito(Long depositoId) que:
   - Busque todos los tramos que tienen destinoDepositoId = depositoId y estado = FINALIZADO
   - Obtenga las rutaIds de esos tramos
   - Para cada ruta, verifique si hay un tramo siguiente que NO haya iniciado (estado = ASIGNADO o ESTIMADO)
   - Si el tramo siguiente no inició, significa que el contenedor está en el depósito
   - Obtenga la solicitudExternaId de cada ruta
   - Llame a seguimiento-service para obtener los detalles del contenedor de cada solicitud
   - Devuelva una lista de ContenedorEnDepositoDTO con:
     - contenedorId
     - identificacion
     - solicitudNumero
     - fechaLlegada (fechaFinReal del tramo que terminó en el depósito)
     - diasEnDeposito (diferencia entre now y fechaLlegada)

2. Agrega el endpoint en DepositoController que use este servicio

3. Incluye filtros opcionales:
   - ?diasMinimo=X (solo contenedores con más de X días)
   - ?ordenar=dias (ordenar por días en depósito)

Documenta con Swagger.
```

---

### 7. Implementar Tracking de Ubicación Actual

**Descripción:**  
Agregar campo de ubicación actual en Solicitud para seguimiento en tiempo real.

**Prompt para el asistente:**
```
Implementa tracking de ubicación actual del contenedor en seguimiento-service:

1. Agrega a la entidad Solicitud:
   - ubicacionActual (String: "EN_ORIGEN", "EN_TRANSITO", "EN_DEPOSITO_{id}", "ENTREGADO")
   - ultimaActualizacionUbicacion (LocalDateTime)

2. Crea endpoint GET /api/solicitudes/{id}/ubicacion que devuelva:
   - ubicacionActual
   - ubicacionDetalle (ej: "Depósito Rosario" si está en depósito)
   - coordenadas aproximadas (si está en tránsito, las del último tramo)
   - ultimaActualizacion

3. Crea endpoint POST /api/solicitudes/{id}/webhook-ubicacion que:
   - Reciba actualizaciones de ubicación desde logistica-service
   - Actualice ubicacionActual y ultimaActualizacionUbicacion
   - Registre el cambio en SolicitudCambioEstado

4. Modifica TramoService en logistica-service para que:
   - Al iniciar un tramo, llame al webhook con ubicacion = "EN_TRANSITO"
   - Al finalizar un tramo en depósito, llame con ubicacion = "EN_DEPOSITO_{depositoId}"
   - Al finalizar el último tramo, llame con ubicacion = "ENTREGADO"

Usa RestClient para las llamadas y maneja errores si el webhook falla.
```

---

### 8. Agregar Autorización por Roles en Logistica-Service

**Descripción:**  
Proteger endpoints de logistica-service según roles de usuario.

**Prompt para el asistente:**
```
Agrega autorización por roles en logistica-service:

1. Modifica SecurityConfig.java para habilitar @EnableMethodSecurity

2. Agrega @PreAuthorize en los controladores:

   CamionController:
   - GET /camiones: permitAll (consulta pública)
   - POST /camiones: hasRole('ADMIN')
   - PUT /camiones/{id}: hasRole('ADMIN')
   - DELETE /camiones/{id}: hasRole('ADMIN')

   DepositoController:
   - GET /depositos: permitAll
   - POST /depositos: hasRole('ADMIN')
   - PUT /depositos/{id}: hasRole('ADMIN')
   - DELETE /depositos/{id}: hasRole('ADMIN')

   TarifaVolumenController:
   - GET /tarifas: permitAll
   - POST /tarifas: hasRole('ADMIN')
   - PUT /tarifas/{id}: hasRole('ADMIN')
   - DELETE /tarifas/{id}: hasRole('ADMIN')

   TramoController:
   - GET /tramos: hasAnyRole('ADMIN', 'TRANSPORTISTA')
   - POST /tramos: hasRole('ADMIN')
   - PATCH /tramos/{id}/asignar-camion: hasRole('ADMIN')
   - PATCH /tramos/{id}/iniciar: hasAnyRole('TRANSPORTISTA', 'ADMIN')
   - PATCH /tramos/{id}/finalizar: hasAnyRole('TRANSPORTISTA', 'ADMIN')

   RutaController:
   - GET /rutas: hasRole('ADMIN')
   - POST /rutas/calcular-tentativa: hasAnyRole('ADMIN', 'CLIENTE')
   - POST /rutas: hasRole('ADMIN')

3. Valida en TramoController.iniciarTramo() y finalizarTramo() que:
   - Si el usuario es TRANSPORTISTA, obtén su username del token
   - Verifica que el camion.nombreTransportista == username
   - Si no coincide, devuelve 403 Forbidden

Documenta los roles requeridos en la documentación Swagger de cada endpoint.
```

---

### 9. Mejorar Manejo de Secretos

**Descripción:**  
Eliminar API keys del código y usar variables de entorno.

**Prompt para el asistente:**
```
Mejora la gestión de secretos en geoapi:

1. En geoapi/src/main/resources/application.properties:
   - ELIMINA la línea que contiene la API key hardcodeada
   - Reemplaza por: google.maps.apikey=${GOOGLE_MAPS_APIKEY:}

2. En GoogleMapsService.java:
   - Valida que apiKey no sea null o vacío al iniciar
   - Si está vacío, log un WARNING y usa el modo fallback Haversine

3. Actualiza el README.md de geoapi para documentar:
   - Cómo obtener una API key de Google Maps
   - Cómo configurarla como variable de entorno
   - Que el servicio funciona en modo fallback sin la API key (con precisión reducida)

4. Agrega en .gitignore:
   - .env
   - *.env
   - application-local.properties

5. Crea application.properties.example con:
   google.maps.apikey=YOUR_API_KEY_HERE

NO incluyas la API key real en ningún archivo que se suba al repositorio.
```

---

### 10. Crear Tests Unitarios Básicos

**Descripción:**  
Agregar tests para las reglas de negocio críticas.

**Prompt para el asistente:**
```
Crea tests unitarios para las funcionalidades críticas:

1. En logistica-service/src/test/java/ar/edu/tpi/logistica/services:

   TramoServiceTest.java:
   - testAsignarCamion_ValidaCapacidadPeso()
   - testAsignarCamion_ValidaCapacidadVolumen()
   - testAsignarCamion_CamionNoDisponible()
   - testFinalizarTramo_CalculaCostoReal()
   - testFinalizarTramo_UltimoTramoActualizaSolicitud()

   CostoServiceTest.java:
   - testCalcularCostoReal_ConUnTramo()
   - testCalcularCostoReal_ConVariosTramos()
   - testCalcularCostoReal_IncluyeEstadiaDeposito()
   - testCalcularCostoReal_IncluyeCombustible()

2. Usa:
   - @SpringBootTest o @WebMvcTest según corresponda
   - Mockito para mockear llamadas a servicios externos (GeoAPI, seguimiento-service)
   - @MockBean para repositorios
   - AssertJ para assertions

3. Configura application-test.properties con:
   - Base de datos H2 en memoria
   - Deshabilitar seguridad OAuth para tests
   - URLs de servicios externos mockeadas

Asegúrate de que los tests pasen con `mvn test`.
```

---

## 🟢 TAREAS OPCIONALES (Mejoras)

### 11. Implementar Circuit Breaker para Llamadas Entre Servicios

**Prompt:**
```
Implementa Resilience4j Circuit Breaker en las llamadas entre microservicios para mejorar la resiliencia:

1. Agrega dependencia en pom.xml de logistica-service y seguimiento-service:
   spring-boot-starter-actuator
   resilience4j-spring-boot3

2. En logistica-service, envuelve las llamadas a geoapi y seguimiento-service con @CircuitBreaker

3. Configura fallbacks:
   - Si GeoAPI falla, usar cálculo Haversine local
   - Si seguimiento-service falla al actualizar costos, log error y continuar

4. Configura en application.yml:
   - Umbral de fallos: 50%
   - Tiempo en estado abierto: 30s
   - Tamaño de sliding window: 10 llamadas

5. Expón métricas de circuit breaker en /actuator/health
```

---

### 12. Agregar Paginación a Listados

**Prompt:**
```
Agrega paginación a los endpoints que devuelven listas en logistica-service y seguimiento-service:

1. Modifica los repositorios para extender PagingAndSortingRepository

2. Actualiza los métodos de servicio para usar Pageable

3. Cambia los controladores:
   - GET /camiones?page=0&size=20&sort=dominio,asc
   - GET /solicitudes?page=0&size=10&sort=createdAt,desc

4. Devuelve Page<DTO> en lugar de List<DTO>

5. Documenta los parámetros de paginación en Swagger
```

---

### 13. Implementar Logs Centralizados

**Prompt:**
```
Configura logs estructurados con ELK stack (Elasticsearch, Logstash, Kibana):

1. Agrega logback-logstash-encoder a cada microservicio

2. Configura logback-spring.xml para enviar logs en formato JSON

3. Actualiza docker-compose.yml para incluir:
   - Elasticsearch
   - Logstash (con pipeline para procesar logs)
   - Kibana

4. Configura cada microservicio para enviar logs a Logstash

5. Documenta cómo acceder a Kibana y crear dashboards
```

---

## 📊 Checklist de Progreso

- [ ] **CRÍTICA 1:** Cálculo de costos estimados
- [ ] **CRÍTICA 2:** Cálculo de costos reales
- [ ] **CRÍTICA 3:** Validación de capacidad
- [ ] **CRÍTICA 4:** Docker Compose completo
- [ ] **CRÍTICA 5:** Documentación Swagger
- [ ] **IMPORTANTE 6:** Contenedores en depósito
- [ ] **IMPORTANTE 7:** Tracking de ubicación
- [ ] **IMPORTANTE 8:** Autorización por roles
- [ ] **IMPORTANTE 9:** Gestión de secretos
- [ ] **IMPORTANTE 10:** Tests unitarios
- [ ] **OPCIONAL 11:** Circuit Breaker
- [ ] **OPCIONAL 12:** Paginación
- [ ] **OPCIONAL 13:** Logs centralizados

---

## 🚀 Orden Recomendado de Implementación

1. **Día 1-2:** CRÍTICA 3 (Validación capacidad) + IMPORTANTE 9 (Secretos)
2. **Día 3-4:** CRÍTICA 1 (Costos estimados)
3. **Día 5-6:** CRÍTICA 2 (Costos reales)
4. **Día 7:** CRÍTICA 5 (Swagger) + IMPORTANTE 8 (Roles)
5. **Día 8:** CRÍTICA 4 (Docker Compose)
6. **Día 9:** IMPORTANTE 10 (Tests)
7. **Día 10:** IMPORTANTE 6 y 7 (si hay tiempo)

---

## 💡 Notas Importantes

- **Antes de empezar:** Haz backup del código actual y crea una rama nueva
- **Testea cada cambio:** Compila con `mvn clean package` después de cada tarea
- **Commitea frecuentemente:** Commits pequeños y atómicos
- **Documenta decisiones:** Actualiza README.md con las decisiones técnicas tomadas

---

## 📞 Ayuda

Si tienes dudas sobre alguna tarea:
1. Copia el prompt de la tarea
2. Pégalo en la conversación con el asistente
3. Agrega contexto adicional si es necesario (archivos específicos, errores, etc.)

**¡Buena suerte con la finalización del TPI!** 🎓
