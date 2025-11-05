# 🧪 Pruebas Manuales - Sistema Funcionando

## ✅ Lo que SÍ funciona

### 1. Camiones (Logística) ✅

#### Crear Camión
```bash
curl -X POST http://localhost:8080/api/logistica/camiones \
  -H "Content-Type: application/json" \
  -d '{
    "dominio":"AA123BB",
    "capacidadPesoKg":25000.0,
    "capacidadVolumenM3":90.0,
    "costoKmBase":50.0,
    "consumoPromedioLitrosKm":0.35,
    "disponible":true
  }'
```

#### Listar Camiones
```bash
curl http://localhost:8080/api/logistica/camiones
```

#### Verificar Disponibilidad
```bash
curl "http://localhost:8080/api/logistica/camiones/verificar-disponibilidad?peso=2000&volumen=30"
```

### 2. Ubicaciones (Logística) ✅

#### Crear Ubicación
```bash
curl -X POST http://localhost:8080/api/logistica/ubicaciones \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":"Depósito Central Córdoba",
    "direccion":"Ruta 9 Km 5, Córdoba",
    "latitud":-31.4201,
    "longitud":-64.1888,
    "tipo":"DEPOSITO"
  }'
```

#### Listar Ubicaciones
```bash
curl http://localhost:8080/api/logistica/ubicaciones
```

### 3. Depósitos (Logística) ✅

**NOTA:** Primero debes crear una ubicación y obtener su ID

```bash
curl -X POST http://localhost:8080/api/logistica/depositos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":"Depósito Central",
    "ubicacionId":1,
    "capacidadMaxima":10000.0,
    "capacidadActual":5000.0
  }'
```

### 4. GeoAPI ✅

#### Calcular Distancia
```bash
curl "http://localhost:8080/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816"
```

## ❌ Lo que NO funciona (problema con Clientes y Contenedores)

### Clientes (Seguimiento) - Error 400

```bash
# Este comando da error 400 - Hay un problema en el servicio
curl -X POST http://localhost:8080/api/seguimiento/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":"Juan Pérez",
    "email":"juan@test.com",
    "documento":"12345678",
    "telefono":"123456",
    "direccion":"Test 123"
  }'
```

**Posibles causas:**
1. Validación de Bean Validation activa
2. Email o documento ya existe (pero no debería en la primera petición)
3. Problema en el binding del JSON al DTO
4. Falta un campo requerido que no está documentado

### Contenedores (Seguimiento) - Probablemente similar

## 🔍 Diagnóstico Necesario

Para arreglar el problema con Clientes y Contenedores:

1. **Ver los logs del seguimiento-service** en la terminal donde está corriendo
2. **Acceder a Swagger UI**: http://localhost:8081/swagger-ui/index.html
3. **Probar desde Swagger** para ver el error exacto
4. **Revisar la consola H2**: http://localhost:8081/h2-console (si está habilitada)

## 🎯 Flujo de Prueba Recomendado

### Orden para crear datos:

1. **Ubicaciones** (necesarias para depósitos y solicitudes)
   ```bash
   # Crear ubicación origen
   curl -X POST http://localhost:8080/api/logistica/ubicaciones \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Córdoba Depot","direccion":"Ruta 9","latitud":-31.4201,"longitud":-64.1888,"tipo":"DEPOSITO"}'
   
   # Crear ubicación destino
   curl -X POST http://localhost:8080/api/logistica/ubicaciones \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Buenos Aires Cliente","direccion":"Av Corrientes","latitud":-34.6037,"longitud":-58.3816,"tipo":"CLIENTE"}'
   ```

2. **Depósitos** (necesitan ID de ubicación)
   ```bash
   curl -X POST http://localhost:8080/api/logistica/depositos \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Depósito Central","ubicacionId":1,"capacidadMaxima":10000.0,"capacidadActual":5000.0}'
   ```

3. **Camiones**
   ```bash
   curl -X POST http://localhost:8080/api/logistica/camiones \
     -H "Content-Type: application/json" \
     -d '{"dominio":"AA123BB","capacidadPesoKg":25000.0,"capacidadVolumenM3":90.0,"costoKmBase":50.0,"consumoPromedioLitrosKm":0.35,"disponible":true}'
   ```

4. **Calcular Ruta**
   ```bash
   curl "http://localhost:8080/api/geoapi/distancia?origenLat=-31.4201&origenLng=-64.1888&destinoLat=-34.6037&destinoLng=-58.3816"
   ```

5. **Clientes** (⚠️ No funciona por ahora)

6. **Contenedores** (⚠️ No funciona por ahora)

7. **Solicitudes** (necesitan cliente, contenedor y ubicaciones)

## 📝 Próximos Pasos

1. Revisa los logs del terminal de `seguimiento-service` cuando haces el POST de cliente
2. Accede a Swagger UI: http://localhost:8081/swagger-ui/index.html
3. Intenta crear un cliente desde Swagger para ver el error específico
4. Si ves el error en los logs, puedes arreglarlo en el código

## 🎉 Resumen

✅ **Gateway rutea correctamente** - Los endpoints de logística y geoapi funcionan  
✅ **Logística service funciona** - Camiones, ubicaciones, depósitos  
✅ **GeoAPI funciona** - Cálculo de distancias  
❌ **Seguimiento service tiene problemas** - Clientes y contenedores dan error 400  

**El Gateway funciona perfecto**, el problema está en el servicio de seguimiento específicamente.
