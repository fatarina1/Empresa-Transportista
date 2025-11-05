#!/bin/bash

# Script para poblar la base de datos con datos de prueba
# Asegúrate de que los 4 servicios estén corriendo antes de ejecutar este script

GATEWAY_URL="http://localhost:8080"
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  SEED DATA - Sistema de Transporte${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# Función para crear una entidad
create_entity() {
    local url=$1
    local data=$2
    local nombre=$3
    
    response=$(curl -s -X POST "$url" \
        -H "Content-Type: application/json" \
        -d "$data" \
        -w "\n%{http_code}")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        echo -e "${GREEN}✓ $nombre creado${NC}"
        echo "$body"
    else
        echo -e "${RED}✗ Error al crear $nombre (HTTP $http_code)${NC}"
        echo "$body"
        return 1
    fi
}

# Verificar que el gateway esté disponible
echo -e "${CYAN}Verificando conexión con el Gateway...${NC}"
if curl -s "$GATEWAY_URL/actuator/health" > /dev/null; then
    echo -e "${GREEN}✓ Gateway disponible${NC}"
else
    echo -e "${RED}✗ Gateway no disponible en $GATEWAY_URL${NC}"
    echo -e "${YELLOW}Asegúrate de que los 4 servicios estén corriendo${NC}"
    exit 1
fi
echo ""

# ==================== CLIENTES ====================
echo -e "${YELLOW}=== Creando Clientes ===${NC}"

CLIENTE_1=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/clientes" \
    '{
        "nombre": "Juan Pérez",
        "email": "juan.perez@example.com",
        "documento": "12345678",
        "telefono": "+543511234567",
        "direccion": "Av. Colón 1234, Córdoba"
    }' \
    "Cliente 1: Juan Pérez")

CLIENTE_1_ID=$(echo "$CLIENTE_1" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

CLIENTE_2=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/clientes" \
    '{
        "nombre": "María González",
        "email": "maria.gonzalez@example.com",
        "documento": "87654321",
        "telefono": "+541145678901",
        "direccion": "Av. Corrientes 5678, Buenos Aires"
    }' \
    "Cliente 2: María González")

CLIENTE_2_ID=$(echo "$CLIENTE_2" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

CLIENTE_3=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/clientes" \
    '{
        "nombre": "Carlos Rodríguez",
        "email": "carlos.rodriguez@example.com",
        "documento": "11223344",
        "telefono": "+543414567890",
        "direccion": "Bv. Oroño 9876, Rosario"
    }' \
    "Cliente 3: Carlos Rodríguez")

CLIENTE_3_ID=$(echo "$CLIENTE_3" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

echo ""

# ==================== UBICACIONES ====================
echo -e "${YELLOW}=== Creando Ubicaciones ===${NC}"

UBICACION_1=$(create_entity \
    "$GATEWAY_URL/api/logistica/ubicaciones" \
    '{
        "nombre": "Depósito Central Córdoba",
        "direccion": "Ruta 9 Km 5, Córdoba",
        "latitud": -31.4201,
        "longitud": -64.1888,
        "tipo": "DEPOSITO"
    }' \
    "Ubicación 1: Depósito Central Córdoba")

UBICACION_1_ID=$(echo "$UBICACION_1" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

UBICACION_2=$(create_entity \
    "$GATEWAY_URL/api/logistica/ubicaciones" \
    '{
        "nombre": "Cliente Buenos Aires",
        "direccion": "Av. Corrientes 5678, Buenos Aires",
        "latitud": -34.6037,
        "longitud": -58.3816,
        "tipo": "CLIENTE"
    }' \
    "Ubicación 2: Cliente Buenos Aires")

UBICACION_2_ID=$(echo "$UBICACION_2" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

UBICACION_3=$(create_entity \
    "$GATEWAY_URL/api/logistica/ubicaciones" \
    '{
        "nombre": "Cliente Rosario",
        "direccion": "Bv. Oroño 9876, Rosario",
        "latitud": -32.9442,
        "longitud": -60.6505,
        "tipo": "CLIENTE"
    }' \
    "Ubicación 3: Cliente Rosario")

UBICACION_3_ID=$(echo "$UBICACION_3" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

UBICACION_4=$(create_entity \
    "$GATEWAY_URL/api/logistica/ubicaciones" \
    '{
        "nombre": "Depósito Mendoza",
        "direccion": "Ruta 40 Km 10, Mendoza",
        "latitud": -32.8895,
        "longitud": -68.8458,
        "tipo": "DEPOSITO"
    }' \
    "Ubicación 4: Depósito Mendoza")

UBICACION_4_ID=$(echo "$UBICACION_4" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

echo ""

# ==================== DEPÓSITOS ====================
echo -e "${YELLOW}=== Creando Depósitos ===${NC}"

DEPOSITO_1=$(create_entity \
    "$GATEWAY_URL/api/logistica/depositos" \
    '{
        "nombre": "Depósito Central Córdoba",
        "ubicacionId": '"$UBICACION_1_ID"',
        "capacidadMaxima": 10000.0,
        "capacidadActual": 5000.0
    }' \
    "Depósito 1: Central Córdoba")

DEPOSITO_1_ID=$(echo "$DEPOSITO_1" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

DEPOSITO_2=$(create_entity \
    "$GATEWAY_URL/api/logistica/depositos" \
    '{
        "nombre": "Depósito Mendoza",
        "ubicacionId": '"$UBICACION_4_ID"',
        "capacidadMaxima": 8000.0,
        "capacidadActual": 3000.0
    }' \
    "Depósito 2: Mendoza")

DEPOSITO_2_ID=$(echo "$DEPOSITO_2" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

echo ""

# ==================== CAMIONES ====================
echo -e "${YELLOW}=== Creando Camiones ===${NC}"

CAMION_1=$(create_entity \
    "$GATEWAY_URL/api/logistica/camiones" \
    '{
        "patente": "AA123BB",
        "marca": "Mercedes-Benz",
        "modelo": "Actros 2646",
        "capacidadCarga": 25000.0,
        "capacidadVolumen": 90.0,
        "disponible": true
    }' \
    "Camión 1: Mercedes-Benz Actros")

CAMION_1_ID=$(echo "$CAMION_1" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

CAMION_2=$(create_entity \
    "$GATEWAY_URL/api/logistica/camiones" \
    '{
        "patente": "CC456DD",
        "marca": "Scania",
        "modelo": "R 450",
        "capacidadCarga": 30000.0,
        "capacidadVolumen": 100.0,
        "disponible": true
    }' \
    "Camión 2: Scania R 450")

CAMION_2_ID=$(echo "$CAMION_2" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

CAMION_3=$(create_entity \
    "$GATEWAY_URL/api/logistica/camiones" \
    '{
        "patente": "EE789FF",
        "marca": "Volvo",
        "modelo": "FH 500",
        "capacidadCarga": 28000.0,
        "capacidadVolumen": 95.0,
        "disponible": false
    }' \
    "Camión 3: Volvo FH 500 (No disponible)")

CAMION_3_ID=$(echo "$CAMION_3" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

echo ""

# ==================== CONTENEDORES ====================
echo -e "${YELLOW}=== Creando Contenedores ===${NC}"

CONTENEDOR_1=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/contenedores" \
    '{
        "identificacion": "CONT-001",
        "tipo": "REFRIGERADO",
        "peso": 2500.0,
        "volumen": 40.0,
        "temperaturaActual": -18.0
    }' \
    "Contenedor 1: CONT-001 (Refrigerado)")

CONTENEDOR_1_ID=$(echo "$CONTENEDOR_1" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

CONTENEDOR_2=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/contenedores" \
    '{
        "identificacion": "CONT-002",
        "tipo": "SECO",
        "peso": 3000.0,
        "volumen": 50.0
    }' \
    "Contenedor 2: CONT-002 (Seco)")

CONTENEDOR_2_ID=$(echo "$CONTENEDOR_2" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

CONTENEDOR_3=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/contenedores" \
    '{
        "identificacion": "CONT-003",
        "tipo": "SECO",
        "peso": 4000.0,
        "volumen": 60.0
    }' \
    "Contenedor 3: CONT-003 (Seco)")

CONTENEDOR_3_ID=$(echo "$CONTENEDOR_3" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')

echo ""

# ==================== SOLICITUDES ====================
echo -e "${YELLOW}=== Creando Solicitudes de Transporte ===${NC}"

SOLICITUD_1=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/solicitudes" \
    '{
        "clienteId": '"$CLIENTE_1_ID"',
        "contenedorId": '"$CONTENEDOR_1_ID"',
        "ubicacionOrigenId": '"$UBICACION_1_ID"',
        "ubicacionDestinoId": '"$UBICACION_2_ID"',
        "fechaSolicitud": "2025-11-04T10:00:00"
    }' \
    "Solicitud 1: Córdoba → Buenos Aires")

SOLICITUD_2=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/solicitudes" \
    '{
        "clienteId": '"$CLIENTE_2_ID"',
        "contenedorId": '"$CONTENEDOR_2_ID"',
        "ubicacionOrigenId": '"$UBICACION_1_ID"',
        "ubicacionDestinoId": '"$UBICACION_3_ID"',
        "fechaSolicitud": "2025-11-04T11:00:00"
    }' \
    "Solicitud 2: Córdoba → Rosario")

SOLICITUD_3=$(create_entity \
    "$GATEWAY_URL/api/seguimiento/solicitudes" \
    '{
        "clienteId": '"$CLIENTE_3_ID"',
        "contenedorId": '"$CONTENEDOR_3_ID"',
        "ubicacionOrigenId": '"$UBICACION_4_ID"',
        "ubicacionDestinoId": '"$UBICACION_2_ID"',
        "fechaSolicitud": "2025-11-04T12:00:00"
    }' \
    "Solicitud 3: Mendoza → Buenos Aires")

echo ""

# ==================== RESUMEN ====================
echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  RESUMEN DE DATOS CREADOS${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo -e "${CYAN}Clientes:${NC}"
echo "  - Juan Pérez (ID: $CLIENTE_1_ID)"
echo "  - María González (ID: $CLIENTE_2_ID)"
echo "  - Carlos Rodríguez (ID: $CLIENTE_3_ID)"
echo ""
echo -e "${CYAN}Ubicaciones:${NC}"
echo "  - Depósito Central Córdoba (ID: $UBICACION_1_ID)"
echo "  - Cliente Buenos Aires (ID: $UBICACION_2_ID)"
echo "  - Cliente Rosario (ID: $UBICACION_3_ID)"
echo "  - Depósito Mendoza (ID: $UBICACION_4_ID)"
echo ""
echo -e "${CYAN}Depósitos:${NC}"
echo "  - Central Córdoba (ID: $DEPOSITO_1_ID)"
echo "  - Mendoza (ID: $DEPOSITO_2_ID)"
echo ""
echo -e "${CYAN}Camiones:${NC}"
echo "  - Mercedes-Benz Actros (ID: $CAMION_1_ID) - Disponible"
echo "  - Scania R 450 (ID: $CAMION_2_ID) - Disponible"
echo "  - Volvo FH 500 (ID: $CAMION_3_ID) - No disponible"
echo ""
echo -e "${CYAN}Contenedores:${NC}"
echo "  - CONT-001 Refrigerado (ID: $CONTENEDOR_1_ID)"
echo "  - CONT-002 Seco (ID: $CONTENEDOR_2_ID)"
echo "  - CONT-003 Seco (ID: $CONTENEDOR_3_ID)"
echo ""
echo -e "${CYAN}Solicitudes:${NC}"
echo "  - Solicitud 1: Córdoba → Buenos Aires"
echo "  - Solicitud 2: Córdoba → Rosario"
echo "  - Solicitud 3: Mendoza → Buenos Aires"
echo ""
echo -e "${GREEN}¡Datos de prueba creados exitosamente!${NC}"
echo -e "${YELLOW}Ahora puedes probar el sistema con estos datos${NC}"
echo ""
echo -e "${CYAN}Próximos pasos:${NC}"
echo "  1. Verifica los datos: curl http://localhost:8080/api/seguimiento/clientes"
echo "  2. Importa la colección de Postman: Sistema-Transporte.postman_collection.json"
echo "  3. Revisa la guía: GUIA_PRUEBAS_COMPLETAS.md"
echo ""
