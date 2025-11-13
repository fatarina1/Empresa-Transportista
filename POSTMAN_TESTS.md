# 🧪 Guía de Pruebas con Postman

Esta guía contiene todas las pruebas que puedes hacer con Postman para verificar que tus microservicios funcionen correctamente.

## 📋 Índice
- [GeoAPI (Puerto 8080)](#geoapi-puerto-8080)
- [Logística Service (Puerto 8082)](#logística-service-puerto-8082)
- [Seguimiento Service (Puerto 8081)](#seguimiento-service-puerto-8081)
- [API Gateway (Puerto 8090)](#api-gateway-puerto-8090)
- [Pruebas de Integración](#pruebas-de-integración)

---

## 🌍 GeoAPI (Puerto 8080)

### 1. Calcular distancia con coordenadas
```http
GET http://localhost:8080/api/distancia?origen=-31.4167,-64.1833&destino=-32.8908,-68.8272
```
**Respuesta esperada:**
```json
{
    "origen": "-31.4167,-64.1833",
    "destino": "-32.8908,-68.8272",
    "kilometros": 608.353,
    "duracionTexto": "7 hours 45 mins"
}
```

### 2. Calcular distancia con nombres de ciudades
```http
GET http://localhost:8080/api/distancia?origen=Córdoba,Argentina&destino=Mendoza,Argentina
```

### 3. Distancia entre Buenos Aires y Rosario
```http
GET http://localhost:8080/api/distancia?origen=Buenos Aires&destino=Rosario
```

### 4. Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## 🚛 Logística Service (Puerto 8082)

### CAMIONES

#### 1. Listar todos los camiones
```http
GET http://localhost:8082/camiones
```
**Respuesta esperada:** Array con 3 camiones
```json
[
    {
        "id": 1,
        "dominio": "AB123CD",
        "capacidadPesoKg": 10000.0,
        "capacidadVolumenM3": 50.0,
        "costoKmBase": 80.0,
        "consumoPromedioLitrosKm": 0.35,
        "disponible": true
    },
    ...
]
```

#### 2. Obtener camión por dominio
```http
GET http://localhost:8082/camiones/AB123CD
```

#### 3. Crear un nuevo camión
```http
POST http://localhost:8082/camiones
Content-Type: application/json

{
    "dominio": "XY999ZZ",
    "capacidadPesoKg": 12000,
    "capacidadVolumenM3": 60,
    "costoKmBase": 95.0,
    "consumoPromedioLitrosKm": 0.40,
    "disponible": true
}
```

### UBICACIONES

#### 4. Listar todas las ubicaciones
```http
GET http://localhost:8082/ubicaciones
```
**Respuesta esperada:** Array con 4 ubicaciones
```json
[
    {
        "id": 1,
        "nombre": "Córdoba Capital",
        "direccion": "Av. Colón 1234, Córdoba",
        "latitud": -31.4167,
        "longitud": -64.1833
    },
    ...
]
```

#### 5. Obtener ubicación por ID
```http
GET http://localhost:8082/ubicaciones/1
```

#### 6. Crear nueva ubicación
```http
POST http://localhost:8082/ubicaciones
Content-Type: application/json

{
    "nombre": "La Plata",
    "direccion": "Calle 7 entre 47 y 48, La Plata",
    "latitud": -34.9214,
    "longitud": -57.9544
}
```

### DEPÓSITOS

#### 7. Listar todos los depósitos
```http
GET http://localhost:8082/depositos
```

#### 8. Obtener depósito por ID
```http
GET http://localhost:8082/depositos/1
```

#### 9. Crear nuevo depósito
```http
POST http://localhost:8082/depositos
Content-Type: application/json

{
    "nombre": "Depósito Norte",
    "ubicacionId": 1,
    "costoEstadiaDiaria": 550.0,
    "activo": true
}
```

### TARIFAS

#### 10. Listar tarifas por volumen
```http
GET http://localhost:8082/tarifas
```
**Respuesta esperada:**
```json
[
    {
        "id": 1,
        "volumenMinM3": 0,
        "volumenMaxM3": 10,
        "costoKmBase": 100.00
    },
    {
        "id": 2,
        "volumenMinM3": 10,
        "volumenMaxM3": 30,
        "costoKmBase": 150.00
    },
    {
        "id": 3,
        "volumenMinM3": 30,
        "volumenMaxM3": 100,
        "costoKmBase": 200.00
    }
]
```

### RUTAS

#### 11. Listar todas las rutas
```http
GET http://localhost:8082/rutas
```

#### 12. Obtener ruta por ID
```http
GET http://localhost:8082/rutas/1
```

### TRAMOS

#### 13. Listar todos los tramos
```http
GET http://localhost:8082/tramos
```

#### 14. Obtener tramos de una ruta específica
```http
GET http://localhost:8082/tramos?rutaId=1
```

#### 15. Crear un nuevo tramo (con cálculo automático de distancia vía GeoAPI)
```http
POST http://localhost:8082/tramos
Content-Type: application/json

{
    "rutaId": 1,
    "orden": 2,
    "origenDepositoId": 2,
    "destinoDepositoId": 3,
    "origenLatitud": -32.8908,
    "origenLongitud": -68.8272,
    "destinoLatitud": -33.7167,
    "destinoLongitud": -61.9667,
    "tipo": "DEPOSITO_DEPOSITO"
}
```
**Importante:** 
- Este endpoint llamará automáticamente a GeoAPI usando RestClient para calcular la distancia real
- Los depósitos con ID 2 (Depósito Cuyo en Mendoza) y 3 (Depósito Rosario) existen en la BD gracias a los seeders
- El sistema calculará automáticamente la distancia entre Mendoza y Rosario usando GeoAPI

---

## 📦 Seguimiento Service (Puerto 8081)

### CLIENTES

#### 16. Listar todos los clientes
```http
GET http://localhost:8081/clientes
```
**Respuesta esperada:** Array con 3 clientes
```json
[
    {
        "id": 1,
        "nombre": "Juan Pérez",
        "email": "juan.perez@email.com",
        "telefono": "351-123-4567",
        "documento": "30123456",
        "direccion": "Av. Colón 1500, Córdoba"
    },
    ...
]
```

#### 17. Obtener cliente por ID
```http
GET http://localhost:8081/clientes/1
```

#### 18. Crear nuevo cliente
```http
POST http://localhost:8081/clientes
Content-Type: application/json

{
    "nombre": "Carlos Rodríguez",
    "email": "carlos.rodriguez@email.com",
    "telefono": "351-555-9999",
    "documento": "33456789",
    "direccion": "Duarte Quirós 800, Córdoba"
}
```

### CONTENEDORES

#### 19. Listar todos los contenedores
```http
GET http://localhost:8081/contenedores
```
**Respuesta esperada:**
```json
[
    {
        "id": 1,
        "identificacion": "CONT-001-2024",
        "pesoKg": 500.50,
        "volumenM3": 2.5,
        "largoM": 2.0,
        "anchoM": 1.5,
        "altoM": 0.8,
        "descripcion": "Mercadería general - Electrónica",
        "clienteId": 1
    },
    ...
]
```

#### 20. Obtener contenedor por ID
```http
GET http://localhost:8081/contenedores/1
```

#### 21. Crear nuevo contenedor
```http
POST http://localhost:8081/contenedores
Content-Type: application/json

{
    "identificacion": "CONT-004-2024",
    "pesoKg": 650.25,
    "volumenM3": 3.8,
    "largoM": 2.3,
    "anchoM": 1.7,
    "altoM": 0.95,
    "descripcion": "Electrodomésticos",
    "clienteId": 1
}
```

### SOLICITUDES

#### 22. Listar todas las solicitudes
```http
GET http://localhost:8081/solicitudes
```
**Respuesta esperada:** Array con 3 solicitudes
```json
[
    {
        "id": 1,
        "numero": "SOL-2024-001",
        "clienteId": 1,
        "contenedorId": 1,
        "origenId": 1,
        "destinoId": 2,
        "origenDireccion": "Av. Colón 1234, Córdoba",
        "destinoDireccion": "San Martín 500, Mendoza",
        "estado": "PENDIENTE",
        "costoEstimado": 15000.00,
        "tiempoEstimadoMin": 465
    },
    ...
]
```

#### 23. Obtener solicitud por ID
```http
GET http://localhost:8081/solicitudes/1
```

#### 24. Obtener solicitudes por estado
```http
GET http://localhost:8081/solicitudes?estado=PENDIENTE
```

#### 25. Crear nueva solicitud (usando cliente y contenedor existentes)
```http
POST http://localhost:8081/solicitudes
Content-Type: application/json

{
    "clienteId": 1,
    "contenedorId": 1,
    "origenId": 1,
    "destinoId": 3
}
```
**Nota:** Este ejemplo usa un cliente (ID 1) y contenedor (ID 1) que ya existen en la BD gracias a los seeders.

#### 25b. Crear solicitud con nuevo cliente y nuevo contenedor
```http
POST http://localhost:8081/solicitudes
Content-Type: application/json

{
    "origenId": 1,
    "destinoId": 2,
    "clienteNombre": "Roberto Gómez",
    "clienteEmail": "roberto.gomez@email.com",
    "clienteTelefono": "351-888-7777",
    "clienteDocumento": "35678901",
    "clienteDireccion": "San Jerónimo 456, Córdoba",
    "contenedorIdentificacion": "CONT-010-2024",
    "contenedorPesoKg": 450.75,
    "contenedorVolumenM3": 3.2,
    "contenedorLargoM": 2.1,
    "contenedorAnchoM": 1.6,
    "contenedorAltoM": 0.95,
    "contenedorDescripcion": "Maquinaria industrial"
}
```
**Importante:** 
- El controlador usa `CrearSolicitudRequest`, NO `SolicitudDTO`
- Si proporcionas `clienteId` y `contenedorId`, los usa directamente
- Si NO los proporcionas, crea automáticamente el cliente y contenedor con los datos suministrados
- `origenId` y `destinoId` SIEMPRE son requeridos (deben existir en logistica-service)

---

## 🌐 API Gateway (Puerto 8090)

Todas las rutas anteriores también funcionan a través del Gateway, pero con un prefijo diferente:

### GeoAPI a través del Gateway

#### 26. Calcular distancia vía Gateway
```http
GET http://localhost:8090/geoapi/api/distancia?origen=Córdoba&destino=Rosario
```

### Logística a través del Gateway

#### 27. Listar camiones vía Gateway
```http
GET http://localhost:8090/api/logistica/camiones
```

#### 28. Listar ubicaciones vía Gateway
```http
GET http://localhost:8090/api/logistica/ubicaciones
```

#### 29. Listar depósitos vía Gateway
```http
GET http://localhost:8090/api/logistica/depositos
```

#### 30. Crear tramo vía Gateway (con cálculo automático de distancia)
```http
POST http://localhost:8090/api/logistica/tramos
Content-Type: application/json

{
    "rutaId": 1,
    "orden": 3,
    "origenDepositoId": 3,
    "destinoDepositoId": 1,
    "origenLatitud": -33.7167,
    "origenLongitud": -61.9667,
    "destinoLatitud": -31.4167,
    "destinoLongitud": -64.1833,
    "tipo": "DEPOSITO_DEPOSITO"
}
```
**Importante:** Este tramo conecta Rosario (Depósito 3) con Córdoba (Depósito 1), cerrando el circuito.

### Seguimiento a través del Gateway

#### 31. Listar clientes vía Gateway
```http
GET http://localhost:8090/api/seguimiento/clientes
```

#### 32. Listar solicitudes vía Gateway
```http
GET http://localhost:8090/api/seguimiento/solicitudes
```

#### 33. Crear solicitud vía Gateway (con cliente y contenedor nuevos)
```http
POST http://localhost:8090/api/seguimiento/solicitudes
Content-Type: application/json

{
    "origenId": 2,
    "destinoId": 4,
    "clienteNombre": "Transportes del Sur SRL",
    "clienteEmail": "contacto@transportesdelsur.com",
    "clienteTelefono": "261-777-8888",
    "clienteDocumento": "30-12345678-9",
    "clienteDireccion": "Belgrano 250, Mendoza",
    "contenedorIdentificacion": "CONT-GW-001",
    "contenedorPesoKg": 850.00,
    "contenedorVolumenM3": 5.5,
    "contenedorLargoM": 2.5,
    "contenedorAnchoM": 2.0,
    "contenedorAltoM": 1.1,
    "contenedorDescripcion": "Productos agrícolas - Vinos"
}
```
**Importante:** Esta solicitud creará automáticamente un nuevo cliente y un nuevo contenedor.

---

## 🔗 Pruebas de Integración

Estas pruebas demuestran la comunicación entre microservicios usando **RestClient**.

### Prueba 1: Logística llama a GeoAPI

#### 34. Crear tramo con cálculo automático de distancia
```http
POST http://localhost:8082/tramos
Content-Type: application/json

{
    "rutaId": 1,
    "orden": 4,
    "origenDepositoId": 1,
    "destinoDepositoId": 2,
    "origenLatitud": -31.4167,
    "origenLongitud": -64.1833,
    "destinoLatitud": -32.8908,
    "destinoLongitud": -68.8272,
    "tipo": "DEPOSITO_DEPOSITO"
}
```

**¿Qué sucede internamente?**
1. Logística recibe la petición POST en `TramoController`
2. `TramoService.crear()` convierte el DTO a entidad `Tramo`
3. Detecta que hay coordenadas completas (origen y destino)
4. `TramoService` llama a `GeoApiClient.calcularDistancia()`
5. `GeoApiClient` usa **RestClient** para hacer una llamada HTTP GET a GeoAPI:
   ```
   GET http://localhost:8080/api/distancia?origen=-31.4167,-64.1833&destino=-32.8908,-68.8272
   ```
6. GeoAPI llama internamente a Google Maps Distance Matrix API
7. GeoAPI calcula la distancia real: `608.353 km` y duración `7 hours 45 mins`
8. GeoAPI responde a Logística con un `DistanciaDTO`
9. Logística asigna automáticamente `tramo.distanciaKm = 608.353`
10. Logística guarda el tramo en la BD con la distancia calculada
11. Logística responde al cliente con el `TramoDTO` completo

**Verifica en los logs de logistica-service:**
```
INFO TramoService: Distancia calculada para tramo: 608.353 km (7 hours 45 mins)
INFO TramoService: Tramo creado: 2
```

**Respuesta esperada:**
```json
{
    "id": 2,
    "rutaId": 1,
    "orden": 4,
    "origenDepositoId": 1,
    "destinoDepositoId": 2,
    "origenLatitud": -31.4167,
    "origenLongitud": -64.1833,
    "destinoLatitud": -32.8908,
    "destinoLongitud": -68.8272,
    "tipo": "DEPOSITO_DEPOSITO",
    "estado": "ESTIMADO",
    "distanciaKm": 608.353,
    "costoAproximado": null,
    "costoReal": null,
    "fechaInicioEstimada": null,
    "fechaFinEstimada": null,
    "fechaInicioReal": null,
    "fechaFinReal": null,
    "camionId": null,
    "createdAt": "2024-01-15T10:30:00"
}
```

### Prueba 2: Seguimiento consulta Logística

#### 35. Obtener detalles completos de una solicitud
```http
GET http://localhost:8081/solicitudes/1/detalle
```

**¿Qué sucede internamente?** (si está implementado)
1. Seguimiento recibe la petición
2. `SolicitudService` llama a `LogisticaServiceClient`
3. `LogisticaServiceClient` usa **RestClient** para obtener ubicaciones:
   ```
   GET http://localhost:8082/ubicaciones/1
   GET http://localhost:8082/ubicaciones/2
   ```
4. Logística responde con los datos de las ubicaciones
5. Seguimiento combina toda la información y responde

---

## ✅ Verificaciones Importantes

### Health Checks

#### 36. Verificar salud de GeoAPI
```http
GET http://localhost:8080/actuator/health
```

#### 37. Verificar salud de Logística
```http
GET http://localhost:8082/actuator/health
```

#### 38. Verificar salud de Seguimiento
```http
GET http://localhost:8081/actuator/health
```

#### 39. Verificar salud del Gateway
```http
GET http://localhost:8090/actuator/health
```

### Headers del Gateway

Al llamar a través del Gateway, verifica los headers de respuesta:
```
X-Processed-By: API-Gateway
X-Gateway-Service: logistica (o seguimiento, geoapi)
```

---

## 📝 Notas para la Presentación del TPI

### Demostración de RestClient

Para demostrar que usas **RestClient** (Spring 6+) en lugar de RestTemplate:

1. **Muestra el código:**
   - `logistica-service/src/main/java/ar/edu/tpi/logistica/clients/GeoApiClient.java`
   - `seguimiento-service/src/main/java/ar/edu/tpi/seguimiento/clients/LogisticaServiceClient.java`

2. **Ejecuta la prueba 34** (crear tramo) y muestra los logs

3. **Explica el flujo:**
   - "Cuando creo un tramo, el servicio de logística automáticamente llama a GeoAPI usando RestClient"
   - "RestClient es la forma moderna de Spring 6+ para hacer llamadas HTTP"
   - "Reemplaza al deprecado RestTemplate"

### Demostración del Gateway

1. Ejecuta la misma petición **con** y **sin** Gateway
2. Muestra que el resultado es el mismo
3. Explica: "El Gateway centraliza el acceso y aplica filtros como autenticación, logging, etc."

---

## 🚀 Orden Recomendado para Probar

1. **GeoAPI** (pruebas 1-3): Verificar que calcula distancias
2. **Logística** (pruebas 4-15): Ver datos seeded y crear tramo (llama a GeoAPI)
3. **Seguimiento** (pruebas 16-25): Ver datos seeded
4. **Gateway** (pruebas 26-33): Verificar que rutea correctamente
5. **Integración** (pruebas 34-35): Demostrar comunicación entre servicios

---

## 💡 Tips

- Usa **Postman Collections** para organizar todas estas pruebas
- Guarda las respuestas exitosas como ejemplos
- Usa **Environment Variables** en Postman:
  - `{{geoapi_url}}` = `http://localhost:8080`
  - `{{logistica_url}}` = `http://localhost:8082`
  - `{{seguimiento_url}}` = `http://localhost:8081`
  - `{{gateway_url}}` = `http://localhost:8090`
