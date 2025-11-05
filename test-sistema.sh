#!/bin/bash

# Script de Prueba Rápida del Sistema de Transporte
# ===================================================

echo "🚀 Iniciando pruebas del Sistema de Transporte"
echo "================================================"
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variables
GATEWAY_URL="http://localhost:8080"
SEGUIMIENTO_URL="http://localhost:8081"
LOGISTICA_URL="http://localhost:8082"
GEOAPI_URL="http://localhost:8083"

# Función para verificar respuesta
check_response() {
    if [ $1 -eq 200 ] || [ $1 -eq 201 ]; then
        echo -e "${GREEN}✓ OK (Status: $1)${NC}"
        return 0
    else
        echo -e "${RED}✗ FAIL (Status: $1)${NC}"
        return 1
    fi
}

echo "=== PASO 1: Health Checks ==="
echo ""

echo -n "Verificando Gateway... "
STATUS=$(curl -s -o /dev/null -w "%{http_code}" $GATEWAY_URL/actuator/health)
check_response $STATUS

echo -n "Verificando Seguimiento Service... "
STATUS=$(curl -s -o /dev/null -w "%{http_code}" $SEGUIMIENTO_URL/actuator/health)
check_response $STATUS

echo -n "Verificando Logistica Service... "
STATUS=$(curl -s -o /dev/null -w "%{http_code}" $LOGISTICA_URL/actuator/health)
check_response $STATUS

echo -n "Verificando GeoAPI Service... "
STATUS=$(curl -s -o /dev/null -w "%{http_code}" $GEOAPI_URL/actuator/health)
check_response $STATUS

echo ""
echo "=== PASO 2: Verificando Rutas del Gateway ==="
echo ""
echo -n "Obteniendo rutas configuradas... "
ROUTES=$(curl -s $GATEWAY_URL/actuator/gateway/routes)
if [ ! -z "$ROUTES" ]; then
    echo -e "${GREEN}✓ OK${NC}"
    echo "Rutas encontradas:"
    echo "$ROUTES" | grep -o '"route_id":"[^"]*"' | head -5
else
    echo -e "${RED}✗ FAIL${NC}"
fi

echo ""
echo "=== PASO 3: Crear Cliente (a través del Gateway) ==="
echo ""
CLIENTE_RESPONSE=$(curl -s -X POST $GATEWAY_URL/api/seguimiento/clientes \
  -H "Content-Type: application/json" \
  -w "\n%{http_code}" \
  -d '{
    "nombre": "Test Cliente",
    "email": "test@example.com",
    "documento": "99999999",
    "telefono": "+543511111111",
    "direccion": "Test 123"
  }')

STATUS=$(echo "$CLIENTE_RESPONSE" | tail -1)
BODY=$(echo "$CLIENTE_RESPONSE" | head -n -1)
echo -n "Creando cliente... "
check_response $STATUS

if [ $STATUS -eq 201 ]; then
    CLIENTE_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo -e "${YELLOW}Cliente ID: $CLIENTE_ID${NC}"
fi

echo ""
echo "=== PASO 4: Crear Contenedor ==="
echo ""
CONTENEDOR_RESPONSE=$(curl -s -X POST $GATEWAY_URL/api/seguimiento/contenedores \
  -H "Content-Type: application/json" \
  -w "\n%{http_code}" \
  -d "{
    \"identificacion\": \"TEST-CONT-001\",
    \"tipo\": \"SECO\",
    \"peso\": 1000.0,
    \"volumen\": 20.0,
    \"clienteId\": $CLIENTE_ID
  }")

STATUS=$(echo "$CONTENEDOR_RESPONSE" | tail -1)
BODY=$(echo "$CONTENEDOR_RESPONSE" | head -n -1)
echo -n "Creando contenedor... "
check_response $STATUS

if [ $STATUS -eq 201 ]; then
    CONTENEDOR_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo -e "${YELLOW}Contenedor ID: $CONTENEDOR_ID${NC}"
fi

echo ""
echo "=== PASO 5: Crear Ubicaciones ==="
echo ""
UBICACION_ORIGEN=$(curl -s -X POST $GATEWAY_URL/api/logistica/ubicaciones \
  -H "Content-Type: application/json" \
  -w "\n%{http_code}" \
  -d '{
    "nombre": "Test Origen",
    "latitud": -31.4201,
    "longitud": -64.1888,
    "tipo": "DEPOSITO"
  }')

STATUS=$(echo "$UBICACION_ORIGEN" | tail -1)
BODY=$(echo "$UBICACION_ORIGEN" | head -n -1)
echo -n "Creando ubicación origen... "
check_response $STATUS

if [ $STATUS -eq 201 ]; then
    ORIGEN_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo -e "${YELLOW}Origen ID: $ORIGEN_ID${NC}"
fi

UBICACION_DESTINO=$(curl -s -X POST $GATEWAY_URL/api/logistica/ubicaciones \
  -H "Content-Type: application/json" \
  -w "\n%{http_code}" \
  -d '{
    "nombre": "Test Destino",
    "latitud": -34.6037,
    "longitud": -58.3816,
    "tipo": "CLIENTE"
  }')

STATUS=$(echo "$UBICACION_DESTINO" | tail -1)
BODY=$(echo "$UBICACION_DESTINO" | head -n -1)
echo -n "Creando ubicación destino... "
check_response $STATUS

if [ $STATUS -eq 201 ]; then
    DESTINO_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo -e "${YELLOW}Destino ID: $DESTINO_ID${NC}"
fi

echo ""
echo "=== PASO 6: Crear Camión ==="
echo ""
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST $GATEWAY_URL/api/logistica/camiones \
  -H "Content-Type: application/json" \
  -d '{
    "patente": "TEST123",
    "marca": "Test",
    "modelo": "Test",
    "capacidadCarga": 30000.0,
    "capacidadVolumen": 100.0,
    "disponible": true,
    "costoPorKm": 50.0
  }')
echo -n "Creando camión... "
check_response $STATUS

echo ""
echo "=== PASO 7: Verificar Disponibilidad de Camiones ==="
echo ""
DISPONIBILIDAD=$(curl -s $GATEWAY_URL/api/logistica/camiones/verificar-disponibilidad?peso=1000\&volumen=20)
echo "Disponibilidad para peso=1000, volumen=20: $DISPONIBILIDAD"

echo ""
echo "=== PASO 8: Calcular Distancia (GeoAPI) ==="
echo ""
DISTANCIA=$(curl -s "$GATEWAY_URL/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816")
echo "Distancia calculada: $DISTANCIA"

echo ""
echo "=== PASO 9: Crear Solicitud ==="
echo ""
SOLICITUD_RESPONSE=$(curl -s -X POST $GATEWAY_URL/api/seguimiento/solicitudes \
  -H "Content-Type: application/json" \
  -w "\n%{http_code}" \
  -d "{
    \"clienteId\": $CLIENTE_ID,
    \"contenedorId\": $CONTENEDOR_ID,
    \"ubicacionOrigenId\": $ORIGEN_ID,
    \"ubicacionDestinoId\": $DESTINO_ID,
    \"fechaSolicitud\": \"2025-11-04T10:00:00\"
  }")

STATUS=$(echo "$SOLICITUD_RESPONSE" | tail -1)
BODY=$(echo "$SOLICITUD_RESPONSE" | head -n -1)
echo -n "Creando solicitud... "
check_response $STATUS

if [ $STATUS -eq 201 ]; then
    SOLICITUD_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo -e "${YELLOW}Solicitud ID: $SOLICITUD_ID${NC}"
    
    echo ""
    echo "=== PASO 10: Obtener Detalle Completo ==="
    echo ""
    echo -n "Obteniendo detalle de solicitud... "
    DETALLE=$(curl -s -w "\n%{http_code}" $GATEWAY_URL/api/seguimiento/solicitudes/$SOLICITUD_ID/detalle)
    STATUS=$(echo "$DETALLE" | tail -1)
    check_response $STATUS
    
    if [ $STATUS -eq 200 ]; then
        echo ""
        echo "Detalle de la solicitud:"
        echo "$DETALLE" | head -n -1 | python3 -m json.tool 2>/dev/null || echo "$DETALLE" | head -n -1
    fi
    
    echo ""
    echo "=== PASO 11: Actualizar Estado ==="
    echo ""
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X PATCH "$GATEWAY_URL/api/seguimiento/solicitudes/$SOLICITUD_ID/estado?estado=PROGRAMADA")
    echo -n "Cambiando estado a PROGRAMADA... "
    check_response $STATUS
fi

echo ""
echo "================================================"
echo -e "${GREEN}✓ Pruebas completadas${NC}"
echo "================================================"
echo ""
echo "📊 Resumen:"
echo "- Cliente creado: ID $CLIENTE_ID"
echo "- Contenedor creado: ID $CONTENEDOR_ID"
echo "- Ubicación origen: ID $ORIGEN_ID"
echo "- Ubicación destino: ID $DESTINO_ID"
echo "- Solicitud creada: ID $SOLICITUD_ID"
echo ""
echo "🔍 Para ver todos los datos:"
echo "  Clientes: curl $GATEWAY_URL/api/seguimiento/clientes"
echo "  Contenedores: curl $GATEWAY_URL/api/seguimiento/contenedores"
echo "  Solicitudes: curl $GATEWAY_URL/api/seguimiento/solicitudes"
echo "  Camiones: curl $GATEWAY_URL/api/logistica/camiones"
echo ""
