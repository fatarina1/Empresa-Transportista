# Script de Prueba Rápida del Sistema de Transporte (PowerShell)
# ==============================================================

Write-Host "🚀 Iniciando pruebas del Sistema de Transporte" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# URLs
$GATEWAY_URL = "http://localhost:8080"
$SEGUIMIENTO_URL = "http://localhost:8081"
$LOGISTICA_URL = "http://localhost:8082"
$GEOAPI_URL = "http://localhost:8083"

# Función para verificar respuesta
function Check-Response {
    param($StatusCode)
    if ($StatusCode -eq 200 -or $StatusCode -eq 201) {
        Write-Host "✓ OK (Status: $StatusCode)" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ FAIL (Status: $StatusCode)" -ForegroundColor Red
        return $false
    }
}

Write-Host "=== PASO 1: Health Checks ===" -ForegroundColor Yellow
Write-Host ""

Write-Host -NoNewline "Verificando Gateway... "
try {
    $response = Invoke-WebRequest -Uri "$GATEWAY_URL/actuator/health" -Method GET -UseBasicParsing
    Check-Response $response.StatusCode
} catch {
    Write-Host "✗ FAIL - Servicio no disponible" -ForegroundColor Red
}

Write-Host -NoNewline "Verificando Seguimiento Service... "
try {
    $response = Invoke-WebRequest -Uri "$SEGUIMIENTO_URL/actuator/health" -Method GET -UseBasicParsing
    Check-Response $response.StatusCode
} catch {
    Write-Host "✗ FAIL - Servicio no disponible" -ForegroundColor Red
}

Write-Host -NoNewline "Verificando Logistica Service... "
try {
    $response = Invoke-WebRequest -Uri "$LOGISTICA_URL/actuator/health" -Method GET -UseBasicParsing
    Check-Response $response.StatusCode
} catch {
    Write-Host "✗ FAIL - Servicio no disponible" -ForegroundColor Red
}

Write-Host -NoNewline "Verificando GeoAPI Service... "
try {
    $response = Invoke-WebRequest -Uri "$GEOAPI_URL/actuator/health" -Method GET -UseBasicParsing
    Check-Response $response.StatusCode
} catch {
    Write-Host "✗ FAIL - Servicio no disponible" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 2: Verificando Rutas del Gateway ===" -ForegroundColor Yellow
Write-Host ""
Write-Host -NoNewline "Obteniendo rutas configuradas... "
try {
    $routes = Invoke-RestMethod -Uri "$GATEWAY_URL/actuator/gateway/routes" -Method GET
    Write-Host "✓ OK" -ForegroundColor Green
    Write-Host "Total de rutas: $($routes.Count)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAIL" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 3: Crear Cliente (a través del Gateway) ===" -ForegroundColor Yellow
Write-Host ""
$clienteBody = @{
    nombre = "Test Cliente PowerShell"
    email = "test.ps@example.com"
    documento = "88888888"
    telefono = "+543511111111"
    direccion = "Test PS 123"
} | ConvertTo-Json

Write-Host -NoNewline "Creando cliente... "
try {
    $clienteResponse = Invoke-RestMethod -Uri "$GATEWAY_URL/api/seguimiento/clientes" `
        -Method POST `
        -ContentType "application/json" `
        -Body $clienteBody
    Write-Host "✓ OK (Status: 201)" -ForegroundColor Green
    $CLIENTE_ID = $clienteResponse.id
    Write-Host "Cliente ID: $CLIENTE_ID" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAIL" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 4: Crear Contenedor ===" -ForegroundColor Yellow
Write-Host ""
$contenedorBody = @{
    identificacion = "PS-CONT-001"
    tipo = "SECO"
    peso = 1000.0
    volumen = 20.0
    clienteId = $CLIENTE_ID
} | ConvertTo-Json

Write-Host -NoNewline "Creando contenedor... "
try {
    $contenedorResponse = Invoke-RestMethod -Uri "$GATEWAY_URL/api/seguimiento/contenedores" `
        -Method POST `
        -ContentType "application/json" `
        -Body $contenedorBody
    Write-Host "✓ OK (Status: 201)" -ForegroundColor Green
    $CONTENEDOR_ID = $contenedorResponse.id
    Write-Host "Contenedor ID: $CONTENEDOR_ID" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAIL" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 5: Crear Ubicaciones ===" -ForegroundColor Yellow
Write-Host ""
$origenBody = @{
    nombre = "Test Origen PS"
    latitud = -31.4201
    longitud = -64.1888
    tipo = "DEPOSITO"
} | ConvertTo-Json

Write-Host -NoNewline "Creando ubicación origen... "
try {
    $origenResponse = Invoke-RestMethod -Uri "$GATEWAY_URL/api/logistica/ubicaciones" `
        -Method POST `
        -ContentType "application/json" `
        -Body $origenBody
    Write-Host "✓ OK (Status: 201)" -ForegroundColor Green
    $ORIGEN_ID = $origenResponse.id
    Write-Host "Origen ID: $ORIGEN_ID" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAIL" -ForegroundColor Red
}

$destinoBody = @{
    nombre = "Test Destino PS"
    latitud = -34.6037
    longitud = -58.3816
    tipo = "CLIENTE"
} | ConvertTo-Json

Write-Host -NoNewline "Creando ubicación destino... "
try {
    $destinoResponse = Invoke-RestMethod -Uri "$GATEWAY_URL/api/logistica/ubicaciones" `
        -Method POST `
        -ContentType "application/json" `
        -Body $destinoBody
    Write-Host "✓ OK (Status: 201)" -ForegroundColor Green
    $DESTINO_ID = $destinoResponse.id
    Write-Host "Destino ID: $DESTINO_ID" -ForegroundColor Cyan
} catch {
    Write-Host "✗ FAIL" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 6: Verificar Disponibilidad de Camiones ===" -ForegroundColor Yellow
Write-Host ""
try {
    $disponibilidad = Invoke-RestMethod -Uri "$GATEWAY_URL/api/logistica/camiones/verificar-disponibilidad?peso=1000&volumen=20" -Method GET
    Write-Host "Disponibilidad para peso=1000, volumen=20: $disponibilidad" -ForegroundColor Cyan
} catch {
    Write-Host "Error verificando disponibilidad" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 7: Calcular Distancia (GeoAPI) ===" -ForegroundColor Yellow
Write-Host ""
try {
    $distancia = Invoke-RestMethod -Uri "$GATEWAY_URL/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816" -Method GET
    Write-Host "Distancia: $($distancia.distanciaKm) km" -ForegroundColor Cyan
    Write-Host "Duración: $($distancia.duracionMinutos) minutos" -ForegroundColor Cyan
} catch {
    Write-Host "Error calculando distancia" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== PASO 8: Crear Solicitud ===" -ForegroundColor Yellow
Write-Host ""
$solicitudBody = @{
    clienteId = $CLIENTE_ID
    contenedorId = $CONTENEDOR_ID
    ubicacionOrigenId = $ORIGEN_ID
    ubicacionDestinoId = $DESTINO_ID
    fechaSolicitud = "2025-11-04T10:00:00"
} | ConvertTo-Json

Write-Host -NoNewline "Creando solicitud... "
try {
    $solicitudResponse = Invoke-RestMethod -Uri "$GATEWAY_URL/api/seguimiento/solicitudes" `
        -Method POST `
        -ContentType "application/json" `
        -Body $solicitudBody
    Write-Host "✓ OK (Status: 201)" -ForegroundColor Green
    $SOLICITUD_ID = $solicitudResponse.id
    Write-Host "Solicitud ID: $SOLICITUD_ID" -ForegroundColor Cyan
    
    Write-Host ""
    Write-Host "=== PASO 9: Obtener Detalle Completo ===" -ForegroundColor Yellow
    Write-Host ""
    Write-Host -NoNewline "Obteniendo detalle de solicitud... "
    $detalle = Invoke-RestMethod -Uri "$GATEWAY_URL/api/seguimiento/solicitudes/$SOLICITUD_ID/detalle" -Method GET
    Write-Host "✓ OK (Status: 200)" -ForegroundColor Green
    Write-Host ""
    Write-Host "Detalle de la solicitud:" -ForegroundColor Cyan
    $detalle | ConvertTo-Json -Depth 10
    
    Write-Host ""
    Write-Host "=== PASO 10: Actualizar Estado ===" -ForegroundColor Yellow
    Write-Host ""
    Write-Host -NoNewline "Cambiando estado a PROGRAMADA... "
    $estadoResponse = Invoke-RestMethod -Uri "$GATEWAY_URL/api/seguimiento/solicitudes/$SOLICITUD_ID/estado?estado=PROGRAMADA" -Method PATCH
    Write-Host "✓ OK (Status: 200)" -ForegroundColor Green
} catch {
    Write-Host "✗ FAIL" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "✓ Pruebas completadas" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Resumen:" -ForegroundColor Yellow
Write-Host "- Cliente creado: ID $CLIENTE_ID"
Write-Host "- Contenedor creado: ID $CONTENEDOR_ID"
Write-Host "- Ubicación origen: ID $ORIGEN_ID"
Write-Host "- Ubicación destino: ID $DESTINO_ID"
Write-Host "- Solicitud creada: ID $SOLICITUD_ID"
Write-Host ""
Write-Host "🔍 Comandos útiles:" -ForegroundColor Yellow
Write-Host "  Ver clientes: Invoke-RestMethod '$GATEWAY_URL/api/seguimiento/clientes'"
Write-Host "  Ver contenedores: Invoke-RestMethod '$GATEWAY_URL/api/seguimiento/contenedores'"
Write-Host "  Ver solicitudes: Invoke-RestMethod '$GATEWAY_URL/api/seguimiento/solicitudes'"
Write-Host "  Ver camiones: Invoke-RestMethod '$GATEWAY_URL/api/logistica/camiones'"
Write-Host ""
