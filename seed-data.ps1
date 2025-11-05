# Script para Seedear Datos Iniciales - Sistema de Transporte
# ===========================================================

Write-Host "🌱 Iniciando Seedeo de Datos..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$GATEWAY_URL = "http://localhost:8080"

# Función para hacer POST y obtener el ID
function Create-Entity {
    param($Url, $Body, $EntityName)
    
    Write-Host -NoNewline "Creando $EntityName... "
    try {
        $response = Invoke-RestMethod -Uri $Url -Method POST -ContentType "application/json" -Body ($Body | ConvertTo-Json)
        Write-Host "✓ OK (ID: $($response.id))" -ForegroundColor Green
        return $response.id
    } catch {
        Write-Host "✗ FAIL" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $null
    }
}

# ============================================
# PASO 1: CREAR CLIENTES
# ============================================
Write-Host "=== PASO 1: Creando Clientes ===" -ForegroundColor Yellow
Write-Host ""

$cliente1 = @{
    nombre = "Juan Pérez"
    email = "juan.perez@example.com"
    documento = "12345678"
    telefono = "+543511234567"
    direccion = "Av. Colón 1234, Córdoba"
}
$CLIENTE_1_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/clientes" $cliente1 "Cliente 1 (Juan Pérez)"

$cliente2 = @{
    nombre = "María González"
    email = "maria.gonzalez@example.com"
    documento = "87654321"
    telefono = "+543519876543"
    direccion = "San Martín 500, Córdoba"
}
$CLIENTE_2_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/clientes" $cliente2 "Cliente 2 (María González)"

$cliente3 = @{
    nombre = "Carlos Rodríguez"
    email = "carlos.rodriguez@example.com"
    documento = "11223344"
    telefono = "+543515551234"
    direccion = "Av. Vélez Sarsfield 800, Córdoba"
}
$CLIENTE_3_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/clientes" $cliente3 "Cliente 3 (Carlos Rodríguez)"

Write-Host ""

# ============================================
# PASO 2: CREAR UBICACIONES
# ============================================
Write-Host "=== PASO 2: Creando Ubicaciones ===" -ForegroundColor Yellow
Write-Host ""

# Depósito Córdoba
$ubicacion1 = @{
    nombre = "Depósito Central Córdoba"
    latitud = -31.4201
    longitud = -64.1888
    tipo = "DEPOSITO"
}
$UBICACION_1_ID = Create-Entity "$GATEWAY_URL/api/logistica/ubicaciones" $ubicacion1 "Ubicación 1 (Depósito Córdoba)"

# Cliente Buenos Aires
$ubicacion2 = @{
    nombre = "Cliente - Buenos Aires"
    latitud = -34.6037
    longitud = -58.3816
    tipo = "CLIENTE"
}
$UBICACION_2_ID = Create-Entity "$GATEWAY_URL/api/logistica/ubicaciones" $ubicacion2 "Ubicación 2 (Buenos Aires)"

# Cliente Rosario
$ubicacion3 = @{
    nombre = "Cliente - Rosario"
    latitud = -32.9442
    longitud = -60.6505
    tipo = "CLIENTE"
}
$UBICACION_3_ID = Create-Entity "$GATEWAY_URL/api/logistica/ubicaciones" $ubicacion3 "Ubicación 3 (Rosario)"

# Depósito Mendoza
$ubicacion4 = @{
    nombre = "Depósito Mendoza"
    latitud = -32.8895
    longitud = -68.8458
    tipo = "DEPOSITO"
}
$UBICACION_4_ID = Create-Entity "$GATEWAY_URL/api/logistica/ubicaciones" $ubicacion4 "Ubicación 4 (Depósito Mendoza)"

Write-Host ""

# ============================================
# PASO 3: CREAR DEPÓSITOS
# ============================================
Write-Host "=== PASO 3: Creando Depósitos ===" -ForegroundColor Yellow
Write-Host ""

$deposito1 = @{
    nombre = "Depósito Central Córdoba"
    capacidadMaxima = 50000.0
    capacidadActual = 12500.0
    costoEstadia = 150.0
    ubicacionId = $UBICACION_1_ID
}
$DEPOSITO_1_ID = Create-Entity "$GATEWAY_URL/api/logistica/depositos" $deposito1 "Depósito 1 (Córdoba)"

$deposito2 = @{
    nombre = "Depósito Mendoza"
    capacidadMaxima = 30000.0
    capacidadActual = 5000.0
    costoEstadia = 120.0
    ubicacionId = $UBICACION_4_ID
}
$DEPOSITO_2_ID = Create-Entity "$GATEWAY_URL/api/logistica/depositos" $deposito2 "Depósito 2 (Mendoza)"

Write-Host ""

# ============================================
# PASO 4: CREAR CAMIONES
# ============================================
Write-Host "=== PASO 4: Creando Camiones ===" -ForegroundColor Yellow
Write-Host ""

$camion1 = @{
    patente = "ABC123"
    marca = "Scania"
    modelo = "R450"
    capacidadCarga = 30000.0
    capacidadVolumen = 100.0
    disponible = $true
    costoPorKm = 55.0
}
$CAMION_1_ID = Create-Entity "$GATEWAY_URL/api/logistica/camiones" $camion1 "Camión 1 (Scania ABC123)"

$camion2 = @{
    patente = "DEF456"
    marca = "Mercedes-Benz"
    modelo = "Actros"
    capacidadCarga = 25000.0
    capacidadVolumen = 85.0
    disponible = $true
    costoPorKm = 50.0
}
$CAMION_2_ID = Create-Entity "$GATEWAY_URL/api/logistica/camiones" $camion2 "Camión 2 (Mercedes DEF456)"

$camion3 = @{
    patente = "GHI789"
    marca = "Volvo"
    modelo = "FH16"
    capacidadCarga = 28000.0
    capacidadVolumen = 90.0
    disponible = $false
    costoPorKm = 52.0
}
$CAMION_3_ID = Create-Entity "$GATEWAY_URL/api/logistica/camiones" $camion3 "Camión 3 (Volvo GHI789 - No disponible)"

Write-Host ""

# ============================================
# PASO 5: CREAR CONTENEDORES
# ============================================
Write-Host "=== PASO 5: Creando Contenedores ===" -ForegroundColor Yellow
Write-Host ""

$contenedor1 = @{
    identificacion = "CONT-001-2025"
    tipo = "REFRIGERADO"
    peso = 2500.0
    volumen = 35.0
    clienteId = $CLIENTE_1_ID
}
$CONTENEDOR_1_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/contenedores" $contenedor1 "Contenedor 1 (Refrigerado)"

$contenedor2 = @{
    identificacion = "CONT-002-2025"
    tipo = "SECO"
    peso = 1800.0
    volumen = 28.0
    clienteId = $CLIENTE_2_ID
}
$CONTENEDOR_2_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/contenedores" $contenedor2 "Contenedor 2 (Seco)"

$contenedor3 = @{
    identificacion = "CONT-003-2025"
    tipo = "SECO"
    peso = 3200.0
    volumen = 42.0
    clienteId = $CLIENTE_3_ID
}
$CONTENEDOR_3_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/contenedores" $contenedor3 "Contenedor 3 (Seco)"

Write-Host ""

# ============================================
# PASO 6: CREAR SOLICITUDES
# ============================================
Write-Host "=== PASO 6: Creando Solicitudes ===" -ForegroundColor Yellow
Write-Host ""

$solicitud1 = @{
    clienteId = $CLIENTE_1_ID
    contenedorId = $CONTENEDOR_1_ID
    ubicacionOrigenId = $UBICACION_1_ID
    ubicacionDestinoId = $UBICACION_2_ID
    fechaSolicitud = "2025-11-04T08:00:00"
}
$SOLICITUD_1_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/solicitudes" $solicitud1 "Solicitud 1 (Córdoba → Buenos Aires)"

$solicitud2 = @{
    clienteId = $CLIENTE_2_ID
    contenedorId = $CONTENEDOR_2_ID
    ubicacionOrigenId = $UBICACION_1_ID
    ubicacionDestinoId = $UBICACION_3_ID
    fechaSolicitud = "2025-11-04T09:30:00"
}
$SOLICITUD_2_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/solicitudes" $solicitud2 "Solicitud 2 (Córdoba → Rosario)"

$solicitud3 = @{
    clienteId = $CLIENTE_3_ID
    contenedorId = $CONTENEDOR_3_ID
    ubicacionOrigenId = $UBICACION_4_ID
    ubicacionDestinoId = $UBICACION_2_ID
    fechaSolicitud = "2025-11-04T10:00:00"
}
$SOLICITUD_3_ID = Create-Entity "$GATEWAY_URL/api/seguimiento/solicitudes" $solicitud3 "Solicitud 3 (Mendoza → Buenos Aires)"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✓ Seedeo Completado" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# RESUMEN
# ============================================
Write-Host "📊 RESUMEN DE DATOS CREADOS:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Clientes: 3" -ForegroundColor Cyan
Write-Host "  - Juan Pérez (ID: $CLIENTE_1_ID)"
Write-Host "  - María González (ID: $CLIENTE_2_ID)"
Write-Host "  - Carlos Rodríguez (ID: $CLIENTE_3_ID)"
Write-Host ""
Write-Host "Ubicaciones: 4" -ForegroundColor Cyan
Write-Host "  - Depósito Córdoba (ID: $UBICACION_1_ID)"
Write-Host "  - Cliente Buenos Aires (ID: $UBICACION_2_ID)"
Write-Host "  - Cliente Rosario (ID: $UBICACION_3_ID)"
Write-Host "  - Depósito Mendoza (ID: $UBICACION_4_ID)"
Write-Host ""
Write-Host "Depósitos: 2" -ForegroundColor Cyan
Write-Host "  - Depósito Central Córdoba (ID: $DEPOSITO_1_ID)"
Write-Host "  - Depósito Mendoza (ID: $DEPOSITO_2_ID)"
Write-Host ""
Write-Host "Camiones: 3" -ForegroundColor Cyan
Write-Host "  - Scania ABC123 (ID: $CAMION_1_ID) - Disponible"
Write-Host "  - Mercedes DEF456 (ID: $CAMION_2_ID) - Disponible"
Write-Host "  - Volvo GHI789 (ID: $CAMION_3_ID) - No Disponible"
Write-Host ""
Write-Host "Contenedores: 3" -ForegroundColor Cyan
Write-Host "  - CONT-001-2025 Refrigerado (ID: $CONTENEDOR_1_ID)"
Write-Host "  - CONT-002-2025 Seco (ID: $CONTENEDOR_2_ID)"
Write-Host "  - CONT-003-2025 Seco (ID: $CONTENEDOR_3_ID)"
Write-Host ""
Write-Host "Solicitudes: 3" -ForegroundColor Cyan
Write-Host "  - Solicitud $SOLICITUD_1_ID: Córdoba → Buenos Aires"
Write-Host "  - Solicitud $SOLICITUD_2_ID: Córdoba → Rosario"
Write-Host "  - Solicitud $SOLICITUD_3_ID: Mendoza → Buenos Aires"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Ahora puedes probar el sistema con datos reales!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
