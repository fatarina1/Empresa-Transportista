#!/bin/bash
# Script de prueba rápida - Keycloak + Microservicios
# Uso: chmod +x test-keycloak.sh && ./test-keycloak.sh

set -e  # Detener en caso de error

echo "🧪 Script de prueba - Keycloak + Microservicios"
echo "=============================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variables
GATEWAY_URL="http://localhost:8080"
KEYCLOAK_URL="http://localhost:8180"
REALM="tpi-backend"
CLIENT_ID="tpi-backend-client"

echo "📋 Verificando servicios..."
echo ""

# 1. Verificar que los contenedores estén corriendo
echo "1️⃣  Verificando contenedores Docker..."
if ! docker compose ps | grep -q "Up"; then
    echo -e "${RED}❌ Los contenedores no están corriendo${NC}"
    echo "   Ejecuta: docker compose up -d"
    exit 1
fi
echo -e "${GREEN}✅ Contenedores corriendo${NC}"
echo ""

# 2. Verificar salud del gateway
echo "2️⃣  Verificando API Gateway..."
if curl -s "${GATEWAY_URL}/actuator/health" | grep -q "UP"; then
    echo -e "${GREEN}✅ Gateway está UP${NC}"
else
    echo -e "${RED}❌ Gateway no responde${NC}"
    exit 1
fi
echo ""

# 3. Verificar Keycloak
echo "3️⃣  Verificando Keycloak..."
if curl -s "${KEYCLOAK_URL}" | grep -q "Keycloak"; then
    echo -e "${GREEN}✅ Keycloak accesible${NC}"
else
    echo -e "${RED}❌ Keycloak no responde${NC}"
    echo "   Espera unos segundos a que inicie completamente"
    exit 1
fi
echo ""

# 4. Verificar configuración de Keycloak (requiere usuario/contraseña)
echo "4️⃣  Para continuar, necesitas:"
echo "   - Haber configurado el realm: ${REALM}"
echo "   - Haber creado el cliente: ${CLIENT_ID}"
echo "   - Tener al menos un usuario creado"
echo ""
read -p "¿Ya configuraste Keycloak? (s/n): " configured

if [[ "$configured" != "s" ]]; then
    echo -e "${YELLOW}⚠️  Configura Keycloak primero:${NC}"
    echo "   1. Abre: ${KEYCLOAK_URL}"
    echo "   2. Login: admin / admin123"
    echo "   3. Sigue la guía en GUIA_KEYCLOAK_DOCKER.md"
    echo ""
    exit 0
fi

# 5. Probar obtención de token
echo ""
echo "5️⃣  Probando obtención de token..."
echo "   Ingresa las credenciales de un usuario de prueba:"
read -p "   Username: " username
read -sp "   Password: " password
echo ""

# Obtener token via password grant (solo para desarrollo)
TOKEN_RESPONSE=$(curl -s -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${username}" \
  -d "password=${password}")

# Verificar si se obtuvo el token
if echo "$TOKEN_RESPONSE" | grep -q "access_token"; then
    echo -e "${GREEN}✅ Token obtenido exitosamente${NC}"
    
    # Extraer el access_token
    ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
    
    echo ""
    echo "📝 Token (primeros 50 caracteres):"
    echo "   ${ACCESS_TOKEN:0:50}..."
    echo ""
    
    # 6. Probar endpoint protegido
    echo "6️⃣  Probando endpoint protegido..."
    echo "   GET ${GATEWAY_URL}/api/seguimiento/solicitudes"
    
    RESPONSE=$(curl -s -w "\n%{http_code}" \
      -H "Authorization: Bearer ${ACCESS_TOKEN}" \
      "${GATEWAY_URL}/api/seguimiento/solicitudes")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${GREEN}✅ Endpoint respondió correctamente (200)${NC}"
        echo ""
        echo "📄 Respuesta:"
        echo "$BODY" | head -n 10
        if [ $(echo "$BODY" | wc -l) -gt 10 ]; then
            echo "   ... (respuesta truncada)"
        fi
    else
        echo -e "${RED}❌ Error: HTTP ${HTTP_CODE}${NC}"
        echo "   Respuesta: $BODY"
    fi
    
else
    echo -e "${RED}❌ No se pudo obtener el token${NC}"
    echo "   Error: $TOKEN_RESPONSE"
    echo ""
    echo "   Posibles causas:"
    echo "   - Usuario/contraseña incorrectos"
    echo "   - El realm o cliente no existe"
    echo "   - Keycloak no está completamente configurado"
    exit 1
fi

echo ""
echo "=============================================="
echo -e "${GREEN}✅ Pruebas completadas exitosamente${NC}"
echo ""
echo "🎯 Próximos pasos:"
echo "   - Prueba el login vía navegador: ${GATEWAY_URL}/api/login"
echo "   - Obtén el token: ${GATEWAY_URL}/api/me/token"
echo "   - Inspecciona el token en: https://jwt.io"
echo ""
