# 🧪 Guía de Pruebas con Postman - Sistema de Transporte

## 📋 Índice
1. [Prerequisitos](#prerequisitos)
2. [Orden de Inicio de Servicios](#orden-de-inicio)
3. [Verificación de Salud](#verificación-de-salud)
4. [Pruebas Funcionales](#pruebas-funcionales)
5. [Flujo Completo End-to-End](#flujo-completo)
6. [Troubleshooting](#troubleshooting)

---

## 🔧 Prerequisitos

### 1. Importar Colección de Postman
Descarga el archivo `Sistema-Transporte.postman_collection.json` e impórtalo en Postman.

### 2. Variables de Entorno (Opcional)
Crea un Environment en Postman con estas variables:
- `gateway_url` = `http://localhost:8080`
- `seguimiento_url` = `http://localhost:8081`
- `logistica_url` = `http://localhost:8082`
- `geoapi_url` = `http://localhost:8083`

---

## 🚀 Orden de Inicio de Servicios

**IMPORTANTE**: Iniciar en este orden para evitar errores de conexión:

```bash
# Terminal 1 - GeoAPI (puerto 8083)
cd geoapi
./mvnw spring-boot:run

# Esperar mensaje: "Started Application in X seconds"

# Terminal 2 - Logística Service (puerto 8082)
cd logistica-service
./mvnw spring-boot:run

# Esperar mensaje: "Started Application in X seconds"

# Terminal 3 - Seguimiento Service (puerto 8081)
cd seguimiento-service
./mvnw spring-boot:run

# Esperar mensaje: "Started Application in X seconds"

# Terminal 4 - API Gateway (puerto 8080)
cd api-gateway
./mvnw spring-boot:run

# Esperar mensaje: "Started GatewayApplication in X seconds"
```

---

## 🏥 Verificación de Salud

### Paso 1: Health Check del Gateway
```http
GET http://localhost:8080/actuator/health
```
**Respuesta Esperada:**
```json
{
  "status": "UP"
}
```

### Paso 2: Verificar Rutas Configuradas
```http
GET http://localhost:8080/actuator/gateway/routes
```
**Respuesta Esperada:** Lista de rutas configuradas

### Paso 3: Health Check de Servicios Individuales
```http
# Seguimiento
GET http://localhost:8081/actuator/health

# Logística
GET http://localhost:8082/actuator/health

# GeoAPI
GET http://localhost:8083/actuator/health
```

---

## 🧪 Pruebas Funcionales

## A. SEGUIMIENTO SERVICE (a través del Gateway)

### 1. Crear Cliente
```http
POST http://localhost:8080/api/seguimiento/clientes
Content-Type: application/json

{
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "documento": "12345678",
  "telefono": "+54351123456",
  "direccion": "Av. Colón 1234"
}
```
**Status Esperado:** `201 CREATED`
**Nota:** Guarda el `id` del cliente para siguientes pasos

### 2. Listar Clientes
```http
GET http://localhost:8080/api/seguimiento/clientes
```
**Status Esperado:** `200 OK`

### 3. Buscar Cliente por ID
```http
GET http://localhost:8080/api/seguimiento/clientes/1
```
**Status Esperado:** `200 OK`

### 4. Crear Contenedor
```http
POST http://localhost:8080/api/seguimiento/contenedores
Content-Type: application/json

{
  "identificacion": "CONT-001",
  "tipo": "REFRIGERADO",
  "peso": 1500.50,
  "volumen": 25.0,
  "clienteId": 1
}
```
**Status Esperado:** `201 CREATED`
**Nota:** Guarda el `id` del contenedor

### 5. Listar Contenedores
```http
GET http://localhost:8080/api/seguimiento/contenedores
```
**Status Esperado:** `200 OK`

---

## B. LOGÍSTICA SERVICE (a través del Gateway)

### 1. Crear Ubicación de Origen (Depósito)
```http
POST http://localhost:8080/api/logistica/ubicaciones
Content-Type: application/json

{
  "nombre": "Depósito Central",
  "latitud": -31.4201,
  "longitud": -64.1888,
  "tipo": "DEPOSITO"
}
```
**Status Esperado:** `201 CREATED`
**Nota:** Guarda el `id` (será ubicación origen)

### 2. Crear Ubicación de Destino (Cliente)
```http
POST http://localhost:8080/api/logistica/ubicaciones
Content-Type: application/json

{
  "nombre": "Cliente - Buenos Aires",
  "latitud": -34.6037,
  "longitud": -58.3816,
  "tipo": "CLIENTE"
}
```
**Status Esperado:** `201 CREATED`
**Nota:** Guarda el `id` (será ubicación destino)

### 3. Crear Depósito
```http
POST http://localhost:8080/api/logistica/depositos
Content-Type: application/json

{
  "nombre": "Depósito Central Córdoba",
  "capacidadMaxima": 10000.0,
  "capacidadActual": 2500.0,
  "costoEstadia": 150.0,
  "ubicacionId": 1
}
```
**Status Esperado:** `201 CREATED`

### 4. Crear Camión
```http
POST http://localhost:8080/api/logistica/camiones
Content-Type: application/json

{
  "patente": "ABC123",
  "marca": "Scania",
  "modelo": "R450",
  "capacidadCarga": 25000.0,
  "capacidadVolumen": 80.0,
  "disponible": true,
  "costoPorKm": 50.0
}
```
**Status Esperado:** `201 CREATED`

### 5. Listar Camiones Disponibles
```http
GET http://localhost:8080/api/logistica/camiones/disponibles
```
**Status Esperado:** `200 OK`

### 6. Verificar Disponibilidad de Camiones
```http
GET http://localhost:8080/api/logistica/camiones/verificar-disponibilidad?peso=1500&volumen=25
```
**Status Esperado:** `200 OK`
**Respuesta:** `true` o `false`

---

## C. GEOAPI SERVICE (a través del Gateway)

### 1. Calcular Distancia
```http
GET http://localhost:8080/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816
```
**Status Esperado:** `200 OK`
**Respuesta Esperada:**
```json
{
  "distanciaKm": 645.23,
  "duracionMinutos": 645
}
```

### 2. Calcular Ruta Completa
```http
POST http://localhost:8080/api/geoapi/calcular-ruta
Content-Type: application/json

{
  "origen": {
    "latitud": -31.4201,
    "longitud": -64.1888
  },
  "destino": {
    "latitud": -34.6037,
    "longitud": -58.3816
  }
}
```
**Status Esperado:** `200 OK`

---

## 🔄 Flujo Completo End-to-End

### Escenario: Crear una Solicitud de Transporte Completa

#### Paso 1: Crear Cliente (si no existe)
```http
POST http://localhost:8080/api/seguimiento/clientes
Content-Type: application/json

{
  "nombre": "María González",
  "email": "maria.gonzalez@example.com",
  "documento": "87654321",
  "telefono": "+54351987654",
  "direccion": "San Martín 500"
}
```
**Guardar:** `clienteId`

#### Paso 2: Crear Contenedor
```http
POST http://localhost:8080/api/seguimiento/contenedores
Content-Type: application/json

{
  "identificacion": "CONT-E2E-001",
  "tipo": "SECO",
  "peso": 2000.0,
  "volumen": 30.0,
  "clienteId": {{clienteId}}
}
```
**Guardar:** `contenedorId`

#### Paso 3: Verificar Disponibilidad de Camiones
```http
GET http://localhost:8080/api/logistica/camiones/verificar-disponibilidad?peso=2000&volumen=30
```
**Debe retornar:** `true`

#### Paso 4: Crear Solicitud
```http
POST http://localhost:8080/api/seguimiento/solicitudes
Content-Type: application/json

{
  "clienteId": {{clienteId}},
  "contenedorId": {{contenedorId}},
  "ubicacionOrigenId": 1,
  "ubicacionDestinoId": 2,
  "fechaSolicitud": "2025-11-04T10:00:00"
}
```
**Guardar:** `solicitudId`

#### Paso 5: Obtener Detalle Completo de Solicitud
```http
GET http://localhost:8080/api/seguimiento/solicitudes/{{solicitudId}}/detalle
```
**Respuesta:** Incluye datos del cliente, contenedor, ubicaciones

#### Paso 6: Actualizar Estado de Solicitud
```http
PATCH http://localhost:8080/api/seguimiento/solicitudes/{{solicitudId}}/estado?estado=PROGRAMADA
```

#### Paso 7: Verificar Estado
```http
GET http://localhost:8080/api/seguimiento/solicitudes/{{solicitudId}}
```
**Verificar:** `estado` = `PROGRAMADA`

---

## 🔍 Verificación del Gateway

Para verificar que el Gateway está funcionando correctamente:

```http
# 1. Verificar rutas configuradas
GET http://localhost:8080/actuator/gateway/routes

# 2. Hacer una petición a través del gateway
GET http://localhost:8080/api/seguimiento/clientes

# 3. Verificar que la respuesta viene del servicio correcto
```

**El gateway está funcionando si:**
- Puedes acceder a los servicios a través del puerto 8080
- Las rutas se reescriben correctamente (se remueve `/api/seguimiento`)
- Los servicios responden normalmente

---

## 🐛 Troubleshooting

### Error: "Connection refused"
**Causa:** Algún servicio no está iniciado
**Solución:** 
1. Verificar que los 4 servicios estén corriendo
2. Revisar los logs de cada terminal
3. Hacer health check de cada servicio

### Error: "404 Not Found"
**Causa:** Ruta incorrecta o servicio no registrado en gateway
**Solución:**
```http
# Verificar rutas configuradas
GET http://localhost:8080/actuator/gateway/routes
```

### Error: "503 Service Unavailable"
**Causa:** El microservicio destino está caído
**Solución:**
1. Verificar logs del microservicio
2. Reiniciar el servicio
3. Hacer health check directo al servicio

### Error: "CORS policy"
**Causa:** CORS mal configurado
**Solución:** Verificar `application.yml` y `CorsConfig.java`

### Los datos no se persisten entre reinicios
**Causa:** H2 en memoria
**Solución:** Normal, H2 limpia datos al reiniciar. Para persistir, cambiar a archivo en `application.properties`

---

## 📊 Pruebas de Integración Recomendadas

### 1. Test de Comunicación entre Servicios
- Crear solicitud → Verificar que seguimiento llama a logística
- Ver logs del gateway para trazabilidad

### 2. Test de Rate Limiting (Futuro)
- Realizar 100 requests rápidos
- Verificar límites

### 3. Test de Resiliencia
- Detener logistica-service
- Intentar crear solicitud
- Verificar error 503 y logs

---

## 📝 Checklist de Pruebas

- [ ] Gateway responde en puerto 8080
- [ ] Todos los servicios pasan health check
- [ ] Se pueden crear clientes vía gateway
- [ ] Se pueden crear contenedores vía gateway
- [ ] Se pueden crear ubicaciones vía gateway
- [ ] Se pueden crear depósitos vía gateway
- [ ] Se pueden crear camiones vía gateway
- [ ] Verificación de disponibilidad funciona
- [ ] GeoAPI calcula distancias correctamente
- [ ] Se puede crear solicitud completa
- [ ] Obtener detalle de solicitud funciona
- [ ] Actualizar estado de solicitud funciona
- [ ] Headers del gateway están presentes
- [ ] Request ID está en todas las respuestas
- [ ] CORS funciona desde frontend (si aplica)

---

## 🎯 Tips Adicionales

1. **Usa Postman Collections**: Organiza las requests en carpetas
2. **Variables**: Usa `{{clienteId}}` para referenciar IDs
3. **Tests automatizados**: Agrega scripts en Postman para verificar respuestas
4. **Logs**: Mantén abiertos los 4 terminales para ver logs en tiempo real
5. **Base de datos**: Usa H2 Console para verificar datos → `http://localhost:8081/h2-console`

---

¡Sistema listo para probar! 🚀
