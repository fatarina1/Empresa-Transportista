# 🧪 Pruebas del Sistema con Postman

## 🎯 Opciones de Prueba

Tienes **3 formas** de probar el sistema completo:

### 1️⃣ Postman (Recomendado) ⭐
- Importa `Sistema-Transporte.postman_collection.json` en Postman
- Ejecuta las carpetas en orden
- Interface visual y fácil de usar

### 2️⃣ Script PowerShell (Windows)
```powershell
.\test-sistema.ps1
```
- Prueba automática completa
- Crea datos de ejemplo
- Muestra resultados en colores

### 3️⃣ Script Bash (Linux/Mac/Git Bash)
```bash
chmod +x test-sistema.sh
./test-sistema.sh
```
- Prueba automática completa
- Crea datos de ejemplo
- Output con colores

---

## 📖 Guía Completa

Lee `GUIA_PRUEBAS_POSTMAN.md` para:
- Instrucciones paso a paso
- Orden de inicio de servicios
- Troubleshooting
- Flujo End-to-End completo

---

## ⚡ Inicio Rápido con Postman

### Paso 1: Iniciar Servicios (en orden)

```bash
# Terminal 1
cd geoapi
./mvnw spring-boot:run

# Terminal 2
cd logistica-service
./mvnw spring-boot:run

# Terminal 3
cd seguimiento-service
./mvnw spring-boot:run

# Terminal 4
cd api-gateway
./mvnw spring-boot:run
```

### Paso 2: Importar Colección en Postman
1. Abre Postman
2. File → Import
3. Selecciona `Sistema-Transporte.postman_collection.json`
4. ✅ Listo!

### Paso 3: Ejecutar Pruebas

#### Opción A: Ejecución Manual
1. Abre la carpeta **"0. Health Checks"**
2. Ejecuta **"Gateway Health"** → Debe retornar `200 OK`
3. Continúa con las carpetas en orden numérico

#### Opción B: Ejecución Automática
1. Click derecho en la colección
2. **"Run collection"**
3. **"Run Sistema Transporte"**
4. Ve los resultados en tiempo real

---

## 🎪 Flujo de Prueba Recomendado

### 1. Verificación de Salud ✅
- Carpeta: **"0. Health Checks"**
- Ejecutar todas las peticiones
- ✅ Todos deben retornar `200 OK`

### 2. Crear Datos Base 📝
Ejecutar en orden:
1. **"1. Clientes"** → Crear Cliente
2. **"2. Contenedores"** → Crear Contenedor
3. **"3. Ubicaciones"** → Crear Origen y Destino
4. **"4. Depósitos"** → Crear Depósito
5. **"5. Camiones"** → Crear Camión

### 3. Verificaciones 🔍
- **"5. Camiones"** → Verificar Disponibilidad
- **"6. GeoAPI"** → Calcular Distancia

### 4. Flujo Completo 🚀
- Carpeta: **"7. Solicitudes"**
- Crear solicitud
- Ver detalle completo
- Actualizar estado

### 5. Flujo E2E Automatizado 🎯
- Carpeta: **"8. Flujo Completo E2E"**
- Ejecuta todos los pasos en secuencia
- Crea un caso completo de principio a fin

---

## 🔍 Verificar que el Gateway Funciona

Para confirmar que las peticiones pasan por el Gateway:

1. **Haz una petición a través del Gateway** (puerto 8080):
   ```
   GET http://localhost:8080/api/seguimiento/clientes
   ```

2. **Verifica las rutas configuradas**:
   ```
   GET http://localhost:8080/actuator/gateway/routes
   ```

3. **Compara con acceso directo** (puerto 8081):
   ```
   GET http://localhost:8081/clientes
   ```

Si ambos retornan la misma respuesta, el Gateway está ruteando correctamente.

---

## 🐛 Troubleshooting

### Error: "Could not get any response"
**Causa:** El servicio no está corriendo
**Solución:** Verifica que los 4 servicios estén iniciados

### Error: 404 Not Found
**Causa:** Ruta incorrecta o servicio no registrado
**Solución:** 
```
GET http://localhost:8080/actuator/gateway/routes
```

### Error: 503 Service Unavailable
**Causa:** El microservicio destino está caído
**Solución:** Reinicia el microservicio específico

---

## 📊 Endpoints Disponibles

### A través del Gateway (puerto 8080)

#### Seguimiento
- `GET/POST /api/seguimiento/clientes`
- `GET/POST /api/seguimiento/contenedores`
- `GET/POST/PATCH /api/seguimiento/solicitudes`

#### Logística
- `GET/POST /api/logistica/camiones`
- `GET/POST /api/logistica/depositos`
- `GET/POST /api/logistica/ubicaciones`
- `GET /api/logistica/camiones/verificar-disponibilidad`

#### GeoAPI
- `GET /api/geoapi/distancia`
- `POST /api/geoapi/calcular-ruta`

---

## 💡 Tips

1. **Guarda los IDs**: Después de crear un cliente, guarda su ID para usarlo en contenedores
2. **Usa Variables**: En Postman, puedes guardar `{{clienteId}}` como variable
3. **Tests Automáticos**: Agrega scripts en la pestaña "Tests" para validar respuestas
4. **Entorno**: Crea un Environment con `gateway_url = http://localhost:8080`
5. **Logs**: Mantén abiertos los 4 terminales para ver logs en tiempo real

---

## ✅ Checklist de Pruebas Completas

- [ ] Todos los health checks pasan
- [ ] Se puede crear cliente
- [ ] Se puede crear contenedor
- [ ] Se pueden crear ubicaciones
- [ ] Se puede crear camión
- [ ] Verificación de disponibilidad funciona
- [ ] Cálculo de distancia funciona
- [ ] Se puede crear solicitud
- [ ] Obtener detalle completo funciona
- [ ] Actualizar estado funciona
- [ ] Headers del gateway están presentes

---

## 📁 Archivos en este Proyecto

```
├── Sistema-Transporte.postman_collection.json  # Colección de Postman
├── GUIA_PRUEBAS_POSTMAN.md                     # Guía completa detallada
├── test-sistema.ps1                            # Script PowerShell
├── test-sistema.sh                             # Script Bash
└── PRUEBAS_README.md                           # Este archivo
```

---

¡Sistema listo para probar! 🚀

Para soporte adicional, consulta `GUIA_PRUEBAS_POSTMAN.md`
