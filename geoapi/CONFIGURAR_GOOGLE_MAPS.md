
# 🗺️ Configuración de Google Maps API

## 📋 Requisitos

Para usar la API de Google Maps en el GeoAPI, necesitas:

1. Una cuenta de Google Cloud Platform
2. Una API Key de Google Maps con Directions API habilitada
3. Créditos o facturación configurada en GCP

---

## 🔑 Paso 1: Obtener API Key de Google Maps

### 1.1 Crear proyecto en Google Cloud

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la facturación (Google ofrece $200 de crédito gratis)

### 1.2 Habilitar Directions API

1. Ve a **APIs & Services** → **Library**
2. Busca **"Directions API"**
3. Haz clic en **Enable**

### 1.3 Crear API Key

1. Ve a **APIs & Services** → **Credentials**
2. Haz clic en **Create Credentials** → **API Key**
3. Copia la API Key generada
4. (Recomendado) Restringe la API Key:
   - **Application restrictions**: HTTP referrers o IP addresses
   - **API restrictions**: Solo Directions API

---

## ⚙️ Paso 2: Configurar la API Key en el Proyecto

### Opción A: Variable de Entorno (Recomendado para producción)

#### Windows (PowerShell):
```powershell
$env:GOOGLE_MAPS_API_KEY="TU_API_KEY_AQUI"
```

#### Windows (CMD):
```cmd
set GOOGLE_MAPS_API_KEY=TU_API_KEY_AQUI
```

#### Linux/Mac:
```bash
export GOOGLE_MAPS_API_KEY="TU_API_KEY_AQUI"
```

Luego inicia el servicio:
```bash
cd geoapi
mvn spring-boot:run
```

### Opción B: Archivo application.properties (Solo para desarrollo local)

Edita `geoapi/src/main/resources/application.properties`:

```properties
google.maps.api.key=TU_API_KEY_AQUI
```

**⚠️ IMPORTANTE**: No subas este archivo a Git con la API Key real. Usa `.gitignore` para excluirlo.

### Opción C: Archivo .env (Con Docker)

Crea un archivo `.env` en la raíz del proyecto:

```env
GOOGLE_MAPS_API_KEY=TU_API_KEY_AQUI
```

Referéncialo en `docker-compose.yml`:

```yaml
services:
  geoapi:
    environment:
      - GOOGLE_MAPS_API_KEY=${GOOGLE_MAPS_API_KEY}
```

---

## 🧪 Paso 3: Probar la Integración

### 3.1 Verificar que el servicio está corriendo

```bash
curl http://localhost:8083/api/geoapi/health
```

**Respuesta esperada**: `"GeoAPI is running"`

### 3.2 Probar cálculo de distancia

```bash
curl "http://localhost:8083/api/geoapi/distancia?origenLat=-34.603722&origenLng=-58.381592&destinoLat=-34.593841&destinoLng=-58.402344"
```

**Con API Key configurada**: Usa Google Maps Directions API real
**Sin API Key**: Usa cálculo Haversine como fallback

### 3.3 Probar cálculo de ruta completa

```bash
curl -X POST http://localhost:8083/api/geoapi/calcular-ruta \
  -H "Content-Type: application/json" \
  -d '{
    "origen": {
      "nombre": "Obelisco, Buenos Aires",
      "latitud": -34.603722,
      "longitud": -58.381592
    },
    "destino": {
      "nombre": "Plaza Italia, Buenos Aires",
      "latitud": -34.593841,
      "longitud": -58.402344
    }
  }'
```

**Respuesta con Google Maps API**:
```json
{
  "origen": "-34.603722,-58.381592",
  "destino": "-34.593841,-58.402344",
  "distanciaKm": 3.45,
  "duracionMinutos": 12,
  "polilinea": "encoded_polyline_string_here"
}
```

**Respuesta con Haversine (fallback)**:
```json
{
  "origen": "-34.603722,-58.381592",
  "destino": "-34.593841,-58.402344",
  "distanciaKm": 2.87,
  "duracionMinutos": 2,
  "polilinea": ""
}
```

---

## 🔍 Verificar qué método se está usando

Revisa los logs del GeoAPI:

```bash
# Si está usando Google Maps API real:
INFO  Llamando a Google Maps API para calcular ruta desde (-34.603722,-58.381592) hasta (-34.593841,-58.402344)
INFO  Ruta calculada exitosamente con Google Maps: 3.45 km, 12 minutos

# Si está usando Haversine (fallback):
WARN  Google Maps API Key no configurada. Usando cálculo Haversine como fallback.
INFO  Usando cálculo Haversine para distancia aproximada
```

---

## 📊 Diferencias: Google Maps API vs Haversine

| Característica | Google Maps API | Haversine (Fallback) |
|----------------|-----------------|----------------------|
| **Distancia** | Real (considera calles) | Línea recta (geodésica) |
| **Precisión** | ✅ Alta (rutas reales) | ⚠️ Aproximada (+/- 20%) |
| **Duración** | ✅ Basada en tráfico histórico | ⚠️ Estimación fija (60 km/h) |
| **Polilínea** | ✅ Sí (para mapas) | ❌ No disponible |
| **Costo** | 💰 $5 por 1000 requests | 🆓 Gratis |
| **Requiere Internet** | ✅ Sí | ❌ No |

---

## 🚨 Troubleshooting

### Error: "REQUEST_DENIED"

**Causa**: API Key inválida o Directions API no habilitada.

**Solución**:
1. Verifica que copiaste la API Key correctamente
2. Ve a GCP Console → APIs & Services → Library
3. Habilita **Directions API**

### Error: "OVER_QUERY_LIMIT"

**Causa**: Excediste el límite de requests (gratuito: 40,000/mes).

**Solución**:
- Espera hasta el próximo mes
- O configura facturación en GCP

### Logs muestran: "Usando cálculo Haversine como fallback"

**Causa**: La API Key no está configurada o es inválida.

**Solución**:
1. Verifica la variable de entorno:
   ```bash
   echo $GOOGLE_MAPS_API_KEY  # Linux/Mac
   echo %GOOGLE_MAPS_API_KEY%  # Windows CMD
   $env:GOOGLE_MAPS_API_KEY    # Windows PowerShell
   ```
2. Si está vacía, configúrala según el Paso 2
3. Reinicia el servicio GeoAPI

---

## 💡 Recomendaciones

### Para Desarrollo Local
- ✅ Usa Haversine (fallback) para pruebas rápidas
- ✅ Configura Google Maps API solo cuando necesites precisión real

### Para Producción
- ✅ Siempre usa Google Maps API real
- ✅ Configura restricciones de API Key (IP whitelisting)
- ✅ Monitorea el uso de cuotas en GCP Console
- ✅ Implementa caché de rutas frecuentes (Redis)

### Seguridad
- ❌ **NUNCA** hagas commit de la API Key en Git
- ✅ Usa variables de entorno o servicios de secretos (AWS Secrets Manager, Azure Key Vault)
- ✅ Agrega `application.properties` con API Key real a `.gitignore`

---

## 📚 Recursos Adicionales

- [Google Maps Directions API Documentation](https://developers.google.com/maps/documentation/directions/overview)
- [Google Cloud Platform Console](https://console.cloud.google.com/)
- [Pricing Calculator](https://developers.google.com/maps/documentation/directions/usage-and-billing)
- [API Key Best Practices](https://developers.google.com/maps/api-key-best-practices)

---

## ✅ Checklist de Configuración

- [ ] Cuenta de Google Cloud creada
- [ ] Proyecto de GCP configurado
- [ ] Directions API habilitada
- [ ] API Key generada y copiada
- [ ] Restricciones de API Key configuradas
- [ ] Variable de entorno `GOOGLE_MAPS_API_KEY` configurada
- [ ] GeoAPI reiniciado
- [ ] Prueba de distancia ejecutada exitosamente
- [ ] Logs verificados (debe mostrar "Google Maps API" no "Haversine")

---

**Estado Actual**: El sistema funciona con o sin API Key. Si no está configurada, usa Haversine automáticamente como fallback. Para producción, **debes configurar la API Key real**.
