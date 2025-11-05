# Prueba de Google Maps API Integration
# ========================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🗺️  Prueba de Google Maps API Integration" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$GEOAPI_URL = "http://localhost:8083"

Write-Host "📍 Testeando GeoAPI en $GEOAPI_URL" -ForegroundColor Yellow
Write-Host ""

# Test 1: Health check
Write-Host "Test 1: Health Check" -ForegroundColor White
Write-Host "--------------------" -ForegroundColor Gray
try {
    $health = Invoke-RestMethod -Uri "$GEOAPI_URL/api/geoapi/health" -Method Get
    if ($health -eq "GeoAPI is running") {
        Write-Host "✅ GeoAPI está corriendo" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ GeoAPI no responde. Asegúrate de que esté corriendo en el puerto 8083" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Calcular distancia simple
Write-Host "Test 2: Calcular Distancia" -ForegroundColor White
Write-Host "---------------------------" -ForegroundColor Gray
Write-Host "Calculando distancia entre Obelisco y Plaza Italia (Buenos Aires)..." -ForegroundColor Gray
try {
    $distancia = Invoke-RestMethod -Uri "$GEOAPI_URL/api/geoapi/distancia?origenLat=-34.603722&origenLng=-58.381592&destinoLat=-34.593841&destinoLng=-58.402344" -Method Get
    Write-Host "Distancia: $distancia km" -ForegroundColor Cyan
} catch {
    Write-Host "Error al calcular distancia: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Calcular ruta completa
Write-Host "Test 3: Calcular Ruta Completa" -ForegroundColor White
Write-Host "-------------------------------" -ForegroundColor Gray
Write-Host "Calculando ruta detallada..." -ForegroundColor Gray

$body = @{
    origen = @{
        nombre = "Obelisco"
        latitud = -34.603722
        longitud = -58.381592
    }
    destino = @{
        nombre = "Plaza Italia"
        latitud = -34.593841
        longitud = -58.402344
    }
} | ConvertTo-Json

try {
    $ruta = Invoke-RestMethod -Uri "$GEOAPI_URL/api/geoapi/calcular-ruta" -Method Post -Body $body -ContentType "application/json"
    
    Write-Host "Origen: $($ruta.origen)" -ForegroundColor Gray
    Write-Host "Destino: $($ruta.destino)" -ForegroundColor Gray
    Write-Host "Distancia: $($ruta.distanciaKm) km" -ForegroundColor Cyan
    Write-Host "Duración: $($ruta.duracionMinutos) minutos" -ForegroundColor Cyan
    
    # Verificar si tiene polilínea (indica que usó Google Maps API real)
    if ($ruta.polilinea -and $ruta.polilinea -ne "") {
        Write-Host "Polilínea: $($ruta.polilinea.Substring(0, [Math]::Min(50, $ruta.polilinea.Length)))..." -ForegroundColor Gray
        Write-Host ""
        Write-Host "✅ Usando Google Maps API real (tiene polilínea)" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "⚠️  Usando Haversine (fallback) - No hay polilínea" -ForegroundColor Yellow
        Write-Host "   Para usar Google Maps API real, configura la API Key:" -ForegroundColor Yellow
        Write-Host "   `$env:GOOGLE_MAPS_API_KEY='TU_API_KEY'" -ForegroundColor Yellow
        Write-Host "   Ver: geoapi\CONFIGURAR_GOOGLE_MAPS.md" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Error al calcular ruta: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Verificar logs del GeoAPI
Write-Host "Test 4: Verificar Método Usado" -ForegroundColor White
Write-Host "--------------------------------" -ForegroundColor Gray
Write-Host "Revisa los logs del GeoAPI (terminal donde corre mvn spring-boot:run)" -ForegroundColor Gray
Write-Host ""
Write-Host "Si ves: 'Llamando a Google Maps API...' → " -NoNewline -ForegroundColor Gray
Write-Host "Usando API real" -ForegroundColor Green
Write-Host "Si ves: 'Usando cálculo Haversine...' → " -NoNewline -ForegroundColor Gray
Write-Host "Usando fallback" -ForegroundColor Yellow
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ Pruebas completadas" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📚 Más info: geoapi\CONFIGURAR_GOOGLE_MAPS.md" -ForegroundColor Gray
