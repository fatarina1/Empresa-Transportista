# 🧪 Guía Completa de Pruebas - Gateway y Backend

## 📋 Objetivo

Verificar que el API Gateway esté enrutando correctamente las peticiones a cada microservicio y que todo el sistema funcione de manera integrada.

---

## 🚀 PASO 1: Iniciar los Servicios

### Orden de Inicio (IMPORTANTE):

Abre **4 terminales** y ejecuta en orden:

```bash
# Terminal 1 - GeoAPI (puerto 8083)
cd geoapi
./mvnw spring-boot:run
# Esperar: "Started Application in X seconds"

# Terminal 2 - Logística Service (puerto 8082)
cd logistica-service
./mvnw spring-boot:run
# Esperar: "Started Application in X seconds"

# Terminal 3 - Seguimiento Service (puerto 8081)
cd seguimiento-service
./mvnw spring-boot:run
# Esperar: "Started Application in X seconds"

# Terminal 4 - API Gateway (puerto 8080)
cd api-gateway
./mvnw spring-boot:run
# Esperar: "Started GatewayApplication in X seconds"
```

---

## 🏥 PASO 2: Verificar Health Checks

### 2.1 Health Check de Cada Servicio Individual

```powershell
# Verificar GeoAPI
Invoke-RestMethod http://localhost:8083/actuator/health

# Verificar Logística
Invoke-RestMethod http://localhost:8082/actuator/health

# Verificar Seguimiento
Invoke-RestMethod http://localhost:8081/actuator/health

# Verificar Gateway
Invoke-RestMethod http://localhost:8080/actuator/health
```

**Resultado esperado:** Todos deben retornar `{"status":"UP"}`

### 2.2 Verificar Rutas del Gateway

```powershell
# Ver todas las rutas configuradas
Invoke-RestMethod http://localhost:8080/actuator/gateway/routes | ConvertTo-Json -Depth 5
```

**Resultado esperado:** Deberías ver rutas para:
- `logistica-camiones`
- `logistica-depositos`
- `logistica-ubicaciones`
- `seguimiento-clientes`
- `seguimiento-contenedores`
- `seguimiento-solicitudes`
- `geoapi-calcular`

---

## 🌱 PASO 3: Seedear Datos Iniciales

Ejecuta el script de seedeo:

```powershell
.\seed-data.ps1
```

Esto creará:
- 3 Clientes
- 4 Ubicaciones (2 depósitos, 2 clientes)
- 2 Depósitos
- 3 Camiones
- 3 Contenedores
- 3 Solicitudes

---

## 🧪 PASO 4: Probar Enrutamiento del Gateway

### 4.1 Probar Acceso Directo vs Gateway

#### Test 1: Acceso a Seguimiento Service

```powershell
# Acceso DIRECTO al servicio (sin gateway)
$directoClientes = Invoke-RestMethod http://localhost:8081/clientes
Write-Host "Acceso directo - Total clientes: $($directoClientes.Count)" -ForegroundColor Cyan

# Acceso a través del GATEWAY
$gatewayClientes = Invoke-RestMethod http://localhost:8080/api/seguimiento/clientes
Write-Host "A través del Gateway - Total clientes: $($gatewayClientes.Count)" -ForegroundColor Green

# Comparar
if ($directoClientes.Count -eq $gatewayClientes.Count) {
    Write-Host "✓ El Gateway está enrutando correctamente!" -ForegroundColor Green
} else {
    Write-Host "✗ Hay un problema con el enrutamiento" -ForegroundColor Red
}
```

#### Test 2: Acceso a Logística Service

```powershell
# Acceso DIRECTO al servicio (sin gateway)
$directoCamiones = Invoke-RestMethod http://localhost:8082/camiones
Write-Host "Acceso directo - Total camiones: $($directoCamiones.Count)" -ForegroundColor Cyan

# Acceso a través del GATEWAY
$gatewayCamiones = Invoke-RestMethod http://localhost:8080/api/logistica/camiones
Write-Host "A través del Gateway - Total camiones: $($gatewayCamiones.Count)" -ForegroundColor Green

# Comparar
if ($directoCamiones.Count -eq $gatewayCamiones.Count) {
    Write-Host "✓ El Gateway está enrutando correctamente!" -ForegroundColor Green
} else {
    Write-Host "✗ Hay un problema con el enrutamiento" -ForegroundColor Red
}
```

### 4.2 Probar StripPrefix

El gateway está configurado con `stripPrefix(2)`, lo que significa:
- `http://localhost:8080/api/seguimiento/clientes` → `http://localhost:8081/clientes`
- `http://localhost:8080/api/logistica/camiones` → `http://localhost:8082/camiones`

```powershell
# Esta petición debe funcionar (el gateway remueve /api/seguimiento)
Invoke-RestMethod http://localhost:8080/api/seguimiento/clientes

# Esta petición NO debe funcionar (acceso directo con el path del gateway)
try {
    Invoke-RestMethod http://localhost:8081/api/seguimiento/clientes
    Write-Host "✗ No debería funcionar" -ForegroundColor Red
} catch {
    Write-Host "✓ Correcto - El servicio no tiene /api/seguimiento en sus rutas" -ForegroundColor Green
}
```

---

## 🔍 PASO 5: Probar Comunicación Entre Servicios

### 5.1 Seguimiento → Logística

Cuando creas una solicitud, el servicio de seguimiento llama a logística para verificar disponibilidad:

```powershell
# Ver logs del servicio de seguimiento mientras se crea una solicitud
$nuevaSolicitud = @{
    clienteId = 1
    contenedorId = 1
    ubicacionOrigenId = 1
    ubicacionDestinoId = 2
    fechaSolicitud = "2025-11-04T14:00:00"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/seguimiento/solicitudes" `
    -Method POST `
    -ContentType "application/json" `
    -Body $nuevaSolicitud
```

**Revisa los logs** en la terminal del `seguimiento-service` - deberías ver llamadas a `http://localhost:8082`

### 5.2 Logística → GeoAPI

```powershell
# Calcular distancia a través del gateway (logística usa geoapi)
$distancia = Invoke-RestMethod "http://localhost:8080/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816"

Write-Host "Distancia calculada: $($distancia.distanciaKm) km" -ForegroundColor Cyan
Write-Host "Duración estimada: $($distancia.duracionMinutos) minutos" -ForegroundColor Cyan
```

---

## 📊 PASO 6: Pruebas Funcionales Completas

### 6.1 CRUD de Clientes

```powershell
# 1. CREAR (POST)
$nuevoCliente = @{
    nombre = "Pedro Martínez"
    email = "pedro@test.com"
    documento = "99887766"
    telefono = "+543511111111"
    direccion = "Test 123"
} | ConvertTo-Json

$cliente = Invoke-RestMethod -Uri "http://localhost:8080/api/seguimiento/clientes" `
    -Method POST -ContentType "application/json" -Body $nuevoCliente

Write-Host "Cliente creado con ID: $($cliente.id)" -ForegroundColor Green

# 2. LEER (GET)
$clienteLeido = Invoke-RestMethod "http://localhost:8080/api/seguimiento/clientes/$($cliente.id)"
Write-Host "Cliente leído: $($clienteLeido.nombre)" -ForegroundColor Cyan

# 3. ACTUALIZAR (PUT)
$clienteActualizado = @{
    nombre = "Pedro Martínez ACTUALIZADO"
    email = "pedro@test.com"
    documento = "99887766"
    telefono = "+543512222222"
    direccion = "Nueva Dirección 456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/seguimiento/clientes/$($cliente.id)" `
    -Method PUT -ContentType "application/json" -Body $clienteActualizado

Write-Host "Cliente actualizado" -ForegroundColor Green

# 4. LISTAR (GET)
$todosLosClientes = Invoke-RestMethod "http://localhost:8080/api/seguimiento/clientes"
Write-Host "Total de clientes: $($todosLosClientes.Count)" -ForegroundColor Cyan

# 5. ELIMINAR (DELETE)
Invoke-RestMethod -Uri "http://localhost:8080/api/seguimiento/clientes/$($cliente.id)" -Method DELETE
Write-Host "Cliente eliminado" -ForegroundColor Green
```

### 6.2 Verificar Disponibilidad de Camiones

```powershell
# Verificar si hay camiones disponibles para un peso/volumen específico
$disponible = Invoke-RestMethod "http://localhost:8080/api/logistica/camiones/verificar-disponibilidad?peso=2000&volumen=30"

if ($disponible -eq $true) {
    Write-Host "✓ Hay camiones disponibles para transportar 2000kg / 30m³" -ForegroundColor Green
} else {
    Write-Host "✗ No hay camiones disponibles" -ForegroundColor Red
}

# Ver camiones disponibles
$camionesDisponibles = Invoke-RestMethod "http://localhost:8080/api/logistica/camiones/disponibles"
Write-Host "Camiones disponibles: $($camionesDisponibles.Count)" -ForegroundColor Cyan
$camionesDisponibles | ForEach-Object { 
    Write-Host "  - $($_.marca) $($_.modelo) ($($_.patente))" -ForegroundColor Gray
}
```

### 6.3 Flujo Completo: Crear Solicitud y Obtener Detalle

```powershell
# 1. Obtener datos existentes
$clientes = Invoke-RestMethod "http://localhost:8080/api/seguimiento/clientes"
$contenedores = Invoke-RestMethod "http://localhost:8080/api/seguimiento/contenedores"
$ubicaciones = Invoke-RestMethod "http://localhost:8080/api/logistica/ubicaciones"

Write-Host "Datos disponibles:" -ForegroundColor Yellow
Write-Host "  Clientes: $($clientes.Count)"
Write-Host "  Contenedores: $($contenedores.Count)"
Write-Host "  Ubicaciones: $($ubicaciones.Count)"
Write-Host ""

# 2. Crear nueva solicitud
$nuevaSolicitud = @{
    clienteId = $clientes[0].id
    contenedorId = $contenedores[0].id
    ubicacionOrigenId = $ubicaciones[0].id
    ubicacionDestinoId = $ubicaciones[1].id
    fechaSolicitud = "2025-11-04T15:00:00"
} | ConvertTo-Json

$solicitud = Invoke-RestMethod -Uri "http://localhost:8080/api/seguimiento/solicitudes" `
    -Method POST -ContentType "application/json" -Body $nuevaSolicitud

Write-Host "Solicitud creada con ID: $($solicitud.id)" -ForegroundColor Green
Write-Host "Estado inicial: $($solicitud.estado)" -ForegroundColor Cyan

# 3. Obtener detalle completo (incluye datos de otros servicios)
$detalle = Invoke-RestMethod "http://localhost:8080/api/seguimiento/solicitudes/$($solicitud.id)/detalle"

Write-Host ""
Write-Host "=== DETALLE COMPLETO DE LA SOLICITUD ===" -ForegroundColor Yellow
Write-Host "Número: $($detalle.numeroSolicitud)" -ForegroundColor Cyan
Write-Host "Estado: $($detalle.estado)" -ForegroundColor Cyan
Write-Host "Cliente: $($detalle.cliente.nombre)" -ForegroundColor Cyan
Write-Host "Contenedor: $($detalle.contenedor.identificacion) ($($detalle.contenedor.tipo))" -ForegroundColor Cyan
Write-Host "Peso: $($detalle.contenedor.peso) kg" -ForegroundColor Cyan
Write-Host "Volumen: $($detalle.contenedor.volumen) m³" -ForegroundColor Cyan
Write-Host "Origen: $($detalle.ubicacionOrigen.nombre)" -ForegroundColor Cyan
Write-Host "Destino: $($detalle.ubicacionDestino.nombre)" -ForegroundColor Cyan

# 4. Actualizar estado de la solicitud
Invoke-RestMethod -Uri "http://localhost:8080/api/seguimiento/solicitudes/$($solicitud.id)/estado?estado=PROGRAMADA" -Method PATCH
Write-Host ""
Write-Host "Estado actualizado a PROGRAMADA" -ForegroundColor Green

# 5. Verificar el cambio
$solicitudActualizada = Invoke-RestMethod "http://localhost:8080/api/seguimiento/solicitudes/$($solicitud.id)"
Write-Host "Nuevo estado: $($solicitudActualizada.estado)" -ForegroundColor Green
```

---

## ✅ PASO 7: Checklist de Verificación

Marca cada ítem cuando funcione:

### Servicios
- [ ] GeoAPI responde en puerto 8083
- [ ] Logística responde en puerto 8082
- [ ] Seguimiento responde en puerto 8081
- [ ] Gateway responde en puerto 8080

### Gateway - Enrutamiento
- [ ] Gateway rutea correctamente a seguimiento-service
- [ ] Gateway rutea correctamente a logistica-service
- [ ] Gateway rutea correctamente a geoapi
- [ ] StripPrefix funciona (remueve `/api/seguimiento` y `/api/logistica`)

### Seguimiento Service (a través del Gateway)
- [ ] Crear cliente
- [ ] Listar clientes
- [ ] Obtener cliente por ID
- [ ] Actualizar cliente
- [ ] Eliminar cliente
- [ ] Crear contenedor
- [ ] Listar contenedores

### Logística Service (a través del Gateway)
- [ ] Crear ubicación
- [ ] Listar ubicaciones
- [ ] Crear depósito
- [ ] Listar depósitos
- [ ] Crear camión
- [ ] Listar camiones
- [ ] Verificar disponibilidad de camiones

### GeoAPI (a través del Gateway)
- [ ] Calcular distancia
- [ ] Calcular ruta completa

### Solicitudes (a través del Gateway)
- [ ] Crear solicitud
- [ ] Listar solicitudes
- [ ] Obtener solicitud por ID
- [ ] Obtener detalle completo (con datos de otros servicios)
- [ ] Actualizar estado de solicitud
- [ ] Filtrar solicitudes por estado
- [ ] Filtrar solicitudes por cliente

### Comunicación entre Servicios
- [ ] Seguimiento llama a Logística (verificar en logs)
- [ ] Logística llama a GeoAPI (verificar en logs)

---

## 🐛 Problemas Comunes

### Error: "Connection refused"
**Causa:** Algún servicio no está corriendo
**Solución:** Verificar que los 4 servicios estén iniciados en orden

### Error: 404 Not Found
**Causa:** Ruta incorrecta o servicio no registrado en gateway
**Solución:** Verificar rutas con:
```powershell
Invoke-RestMethod http://localhost:8080/actuator/gateway/routes
```

### Error: 503 Service Unavailable
**Causa:** El microservicio destino está caído
**Solución:** Reiniciar el microservicio específico

### Los datos no aparecen
**Causa:** Estás consultando el servicio directo en lugar del gateway, o viceversa
**Solución:** Asegúrate de usar `localhost:8080/api/...` para gateway

---

## 📝 Logs a Revisar

Mantén abiertas las 4 terminales y observa:

### Terminal Gateway
- Debe mostrar las peticiones entrantes
- No debe mostrar errores de enrutamiento

### Terminal Seguimiento
- Debe mostrar llamadas a logistica-service cuando creas solicitudes
- Debe mostrar las operaciones CRUD

### Terminal Logística
- Debe mostrar llamadas a geoapi cuando calculas distancias
- Debe mostrar las operaciones CRUD

### Terminal GeoAPI
- Debe mostrar los cálculos de distancia/ruta
- Debe usar fórmula de Haversine

---

## 🎯 Resultado Esperado

Si todo funciona correctamente:

✅ Puedes acceder a TODOS los servicios a través del puerto 8080
✅ El Gateway reescribe las rutas correctamente (stripPrefix)
✅ Los servicios se comunican entre sí (seguimiento → logística → geoapi)
✅ Las operaciones CRUD funcionan en todos los servicios
✅ El detalle de solicitud incluye datos de múltiples servicios

---

¡Ahora tienes un sistema completo funcionando! 🎉
