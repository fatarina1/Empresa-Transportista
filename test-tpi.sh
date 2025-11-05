#!/bin/bash

# Script de pruebas automatizadas para TPI Backend 2025
# Este script ejecuta los casos de prueba principales del enunciado

GATEWAY_URL="http://localhost:8080"
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  PRUEBAS TPI - Backend 2025${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# Contadores
TESTS_PASSED=0
TESTS_FAILED=0

# Función para ejecutar prueba
run_test() {
    local test_name=$1
    local command=$2
    local expected_code=${3:-200}
    
    echo -e "${CYAN}🧪 Test: $test_name${NC}"
    
    response=$(eval "$command" -w "\n%{http_code}" 2>&1)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS - HTTP $http_code${NC}"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        echo "$body" | head -3
    else
        echo -e "${RED}✗ FAIL - Expected HTTP $expected_code, got $http_code${NC}"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        echo "$body"
    fi
    echo ""
}

# ==================== PRUEBAS DE HEALTH CHECK ====================
echo -e "${YELLOW}=== Health Checks ===${NC}"

run_test "Gateway Health" "curl -s $GATEWAY_URL/actuator/health"
run_test "Seguimiento Health" "curl -s http://localhost:8081/actuator/health" 200
run_test "Logística Health" "curl -s http://localhost:8082/actuator/health" 200
run_test "GeoAPI Health" "curl -s http://localhost:8083/actuator/health" 200

# ==================== RF1: REGISTRAR SOLICITUD ====================
echo -e "${YELLOW}=== RF1: Registrar Solicitud de Transporte ===${NC}"

# Crear cliente
echo -e "${CYAN}Creando cliente...${NC}"
CLIENTE_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/seguimiento/clientes" \
    -H "Content-Type: application/json" \
    -d '{
        "nombre": "Test Cliente",
        "email": "test'$(date +%s)'@example.com",
        "documento": "'$(date +%s)'",
        "telefono": "+543511111111",
        "direccion": "Test 123"
    }')
CLIENTE_ID=$(echo "$CLIENTE_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -n "$CLIENTE_ID" ]; then
    echo -e "${GREEN}✓ Cliente creado con ID: $CLIENTE_ID${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Error al crear cliente${NC}"
    echo "$CLIENTE_RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi
echo ""

# Crear contenedor
echo -e "${CYAN}Creando contenedor...${NC}"
CONTENEDOR_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/seguimiento/contenedores" \
    -H "Content-Type: application/json" \
    -d '{
        "identificacion": "TEST-'$(date +%s)'",
        "pesoKg": 2500.0,
        "volumenM3": 40.0,
        "largoM": 6.0,
        "anchoM": 2.4,
        "altoM": 2.6,
        "descripcion": "Contenedor de prueba",
        "clienteId": '$CLIENTE_ID'
    }')
CONTENEDOR_ID=$(echo "$CONTENEDOR_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -n "$CONTENEDOR_ID" ]; then
    echo -e "${GREEN}✓ Contenedor creado con ID: $CONTENEDOR_ID${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Error al crear contenedor${NC}"
    echo "$CONTENEDOR_RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi
echo ""

# Crear ubicaciones
echo -e "${CYAN}Creando ubicación origen...${NC}"
ORIGEN_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/logistica/ubicaciones" \
    -H "Content-Type: application/json" \
    -d '{
        "nombre": "Origen Test",
        "direccion": "Córdoba",
        "latitud": -31.4201,
        "longitud": -64.1888,
        "tipo": "DEPOSITO"
    }')
ORIGEN_ID=$(echo "$ORIGEN_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -n "$ORIGEN_ID" ]; then
    echo -e "${GREEN}✓ Origen creado con ID: $ORIGEN_ID${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Error al crear origen${NC}"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi
echo ""

echo -e "${CYAN}Creando ubicación destino...${NC}"
DESTINO_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/logistica/ubicaciones" \
    -H "Content-Type: application/json" \
    -d '{
        "nombre": "Destino Test",
        "direccion": "Buenos Aires",
        "latitud": -34.6037,
        "longitud": -58.3816,
        "tipo": "CLIENTE"
    }')
DESTINO_ID=$(echo "$DESTINO_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -n "$DESTINO_ID" ]; then
    echo -e "${GREEN}✓ Destino creado con ID: $DESTINO_ID${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Error al crear destino${NC}"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi
echo ""

# Crear solicitud
echo -e "${CYAN}Creando solicitud de transporte...${NC}"
SOLICITUD_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/seguimiento/solicitudes" \
    -H "Content-Type: application/json" \
    -d '{
        "clienteId": '$CLIENTE_ID',
        "contenedorId": '$CONTENEDOR_ID',
        "origenId": '$ORIGEN_ID',
        "destinoId": '$DESTINO_ID'
    }')
SOLICITUD_ID=$(echo "$SOLICITUD_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -n "$SOLICITUD_ID" ]; then
    echo -e "${GREEN}✓ Solicitud creada con ID: $SOLICITUD_ID${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Error al crear solicitud${NC}"
    echo "$SOLICITUD_RESPONSE"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi
echo ""

# ==================== RF2: CONSULTAR ESTADO ====================
echo -e "${YELLOW}=== RF2: Consultar Estado de Transporte ===${NC}"

run_test "Consultar solicitud por ID" \
    "curl -s $GATEWAY_URL/api/seguimiento/solicitudes/$SOLICITUD_ID"

run_test "Consultar detalle completo de solicitud" \
    "curl -s $GATEWAY_URL/api/seguimiento/solicitudes/$SOLICITUD_ID/detalle"

run_test "Consultar solicitudes del cliente" \
    "curl -s $GATEWAY_URL/api/seguimiento/solicitudes/cliente/$CLIENTE_ID"

# ==================== RF3: CALCULAR RUTA ====================
echo -e "${YELLOW}=== RF3: Consultar Rutas Tentativas ===${NC}"

run_test "Calcular distancia Córdoba - Buenos Aires" \
    "curl -s '$GATEWAY_URL/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816'"

run_test "Listar camiones disponibles" \
    "curl -s $GATEWAY_URL/api/logistica/camiones/disponibles"

# ==================== RF6: CREAR Y ASIGNAR CAMIÓN ====================
echo -e "${YELLOW}=== RF6 + RF10: Crear Camión y Validar Capacidad ===${NC}"

echo -e "${CYAN}Creando camión...${NC}"
CAMION_RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/logistica/camiones" \
    -H "Content-Type: application/json" \
    -d '{
        "dominio": "TEST'$(date +%s)'",
        "capacidadPesoKg": 30000.0,
        "capacidadVolumenM3": 90.0,
        "costoKmBase": 50.0,
        "consumoPromedioLitrosKm": 0.35,
        "disponible": true
    }')
CAMION_ID=$(echo "$CAMION_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)

if [ -n "$CAMION_ID" ]; then
    echo -e "${GREEN}✓ Camión creado con ID: $CAMION_ID${NC}"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Error al crear camión${NC}"
    TESTS_FAILED=$((TESTS_FAILED + 1))
fi
echo ""

run_test "Verificar disponibilidad de camión para contenedor (RF10)" \
    "curl -s '$GATEWAY_URL/api/logistica/camiones/verificar-disponibilidad?peso=2500&volumen=40'"

# ==================== RF9: CRUD DE DATOS ====================
echo -e "${YELLOW}=== RF9: Gestión de Depósitos, Camiones y Tarifas ===${NC}"

run_test "Listar todos los camiones" \
    "curl -s $GATEWAY_URL/api/logistica/camiones"

run_test "Listar todas las ubicaciones" \
    "curl -s $GATEWAY_URL/api/logistica/ubicaciones"

run_test "Listar todos los clientes" \
    "curl -s $GATEWAY_URL/api/seguimiento/clientes"

run_test "Listar todos los contenedores" \
    "curl -s $GATEWAY_URL/api/seguimiento/contenedores"

run_test "Listar todas las solicitudes" \
    "curl -s $GATEWAY_URL/api/seguimiento/solicitudes"

# ==================== RESUMEN ====================
echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  RESUMEN DE PRUEBAS${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
echo ""

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ TODOS LOS TESTS PASARON! 🎉${NC}"
    exit 0
else
    PERCENTAGE=$((TESTS_PASSED * 100 / TOTAL_TESTS))
    echo -e "${YELLOW}⚠ $PERCENTAGE% de tests pasaron${NC}"
    exit 1
fi
