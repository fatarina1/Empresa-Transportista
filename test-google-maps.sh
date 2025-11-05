#!/bin/bash

echo "=========================================="
echo "🗺️  Prueba de Google Maps API Integration"
echo "=========================================="
echo ""

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# URL del GeoAPI
GEOAPI_URL="http://localhost:8083"

echo "📍 Testeando GeoAPI en $GEOAPI_URL"
echo ""

# Test 1: Health check
echo "Test 1: Health Check"
echo "--------------------"
HEALTH=$(curl -s "$GEOAPI_URL/api/geoapi/health")
if [ "$HEALTH" == "GeoAPI is running" ]; then
    echo -e "${GREEN}✅ GeoAPI está corriendo${NC}"
else
    echo -e "${RED}❌ GeoAPI no responde. Asegúrate de que esté corriendo en el puerto 8083${NC}"
    exit 1
fi
echo ""

# Test 2: Calcular distancia simple
echo "Test 2: Calcular Distancia"
echo "---------------------------"
echo "Calculando distancia entre Obelisco y Plaza Italia (Buenos Aires)..."
RESPONSE=$(curl -s "$GEOAPI_URL/api/geoapi/distancia?origenLat=-34.603722&origenLng=-58.381592&destinoLat=-34.593841&destinoLng=-58.402344")
echo "Distancia: $RESPONSE km"
echo ""

# Test 3: Calcular ruta completa
echo "Test 3: Calcular Ruta Completa"
echo "-------------------------------"
echo "Calculando ruta detallada..."
RUTA=$(curl -s -X POST "$GEOAPI_URL/api/geoapi/calcular-ruta" \
  -H "Content-Type: application/json" \
  -d '{
    "origen": {
      "nombre": "Obelisco",
      "latitud": -34.603722,
      "longitud": -58.381592
    },
    "destino": {
      "nombre": "Plaza Italia",
      "latitud": -34.593841,
      "longitud": -58.402344
    }
  }')

echo "$RUTA" | jq '.'
echo ""

# Verificar si tiene polilínea (indica que usó Google Maps API real)
HAS_POLYLINE=$(echo "$RUTA" | jq -r '.polilinea')
if [ -n "$HAS_POLYLINE" ] && [ "$HAS_POLYLINE" != "null" ] && [ "$HAS_POLYLINE" != "" ]; then
    echo -e "${GREEN}✅ Usando Google Maps API real (tiene polilínea)${NC}"
else
    echo -e "${YELLOW}⚠️  Usando Haversine (fallback) - No hay polilínea${NC}"
    echo -e "${YELLOW}   Para usar Google Maps API real, configura la API Key:${NC}"
    echo -e "${YELLOW}   export GOOGLE_MAPS_API_KEY='TU_API_KEY'${NC}"
    echo -e "${YELLOW}   Ver: geoapi/CONFIGURAR_GOOGLE_MAPS.md${NC}"
fi
echo ""

# Test 4: Verificar logs del GeoAPI
echo "Test 4: Verificar Método Usado"
echo "--------------------------------"
echo "Revisa los logs del GeoAPI (terminal donde corre mvn spring-boot:run)"
echo ""
echo "Si ves: 'Llamando a Google Maps API...' → ${GREEN}Usando API real${NC}"
echo "Si ves: 'Usando cálculo Haversine...' → ${YELLOW}Usando fallback${NC}"
echo ""

echo "=========================================="
echo "✅ Pruebas completadas"
echo "=========================================="
echo ""
echo "📚 Más info: geoapi/CONFIGURAR_GOOGLE_MAPS.md"
