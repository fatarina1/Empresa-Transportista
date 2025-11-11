# 🌐 GeoAPI - Google Maps Distance Matrix

Microservicio Spring Boot que calcula distancias entre coordenadas geográficas utilizando Google Maps Distance Matrix API.

## 📋 Descripción

Este proyecto implementa un microservicio RESTful que actúa como intermediario para la API de Google Maps, permitiendo calcular distancias y tiempos de viaje entre dos puntos geográficos.

---

## 🛠️ Tecnologías

- **Spring Boot**: 3.5.6
- **Java**: 21
- **Maven**: 3.8+
- **Lombok**: Reducción de código boilerplate
- **RestClient**: Cliente HTTP para consumir Google Maps API
- **Jackson**: Parsing de respuestas JSON
- **Swagger UI**: Documentación interactiva de la API

---

## ⚙️ Configuración

### 1. Obtener API Key de Google Maps

1. Crear cuenta en [Google Cloud Console](https://console.cloud.google.com/)
2. Crear un nuevo proyecto
3. Ir a "APIs y Servicios > Biblioteca"
4. Buscar **"Distance Matrix API"** y habilitarla
5. Ir a "APIs y Servicios > Credenciales"
6. Crear nueva API Key
7. Copiar la clave generada

### 2. Configurar la API Key

Editar `src/main/resources/application.properties`:

```properties
google.maps.apikey=TU_API_KEY_AQUI
```

**⚠️ Importante**: No subir la API Key al repositorio. Usar variables de entorno en producción.

---

## 🚀 Ejecución

### Compilar y ejecutar:

```bash
cd geoapi
mvn spring-boot:run
```

El servicio estará disponible en: **http://localhost:8080**

---

## 🔌 Endpoints

### 1. Calcular Distancia

**GET** `/api/distancia`

**Parámetros:**
- `origen` (String): Coordenadas de origen (formato: "latitud,longitud")
- `destino` (String): Coordenadas de destino (formato: "latitud,longitud")

**Ejemplo de uso:**

```bash
curl "http://localhost:8080/api/distancia?origen=-31.4167,-64.1833&destino=-32.8908,-68.8272"
```

**Respuesta:**

```json
{
  "origen": "-31.4167,-64.1833",
  "destino": "-32.8908,-68.8272",
  "kilometros": 378.5,
  "duracionTexto": "4 hours 30 mins"
}
```

---

## 📊 Estructura del Proyecto

```
geoapi/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── utnfc/isi/back/spring/geoapi/
│   │   │       ├── GeoapiApplication.java          # Clase principal
│   │   │       ├── controller/
│   │   │       │   └── GeoController.java          # REST Controller
│   │   │       ├── service/
│   │   │       │   └── GeoService.java             # Lógica de negocio
│   │   │       └── model/
│   │   │           └── DistanciaDTO.java           # DTO de respuesta
│   │   └── resources/
│   │       └── application.properties              # Configuración
│   └── test/
└── pom.xml                                         # Dependencias Maven
```

---

## 🧪 Pruebas

### Desde Swagger UI

Acceder a: http://localhost:8080/swagger-ui.html

### Ejemplos de prueba:

**1. Córdoba a Mendoza:**
```
origen=-31.4167,-64.1833&destino=-32.8908,-68.8272
```

**2. Buenos Aires a Rosario:**
```
origen=-34.6037,-58.3816&destino=-32.9442,-60.6505
```

**3. CABA - Obelisco a Plaza Italia:**
```
origen=-34.603722,-58.381592&destino=-34.593841,-58.402344
```

---

## 📝 Modelo de Datos

### DistanciaDTO

```java
public class DistanciaDTO {
    private String origen;          // Coordenadas de origen
    private String destino;         // Coordenadas de destino
    private double kilometros;      // Distancia en km
    private String duracionTexto;   // Duración legible (ej: "4 hours 30 mins")
}
```

---

## 🔍 API de Google Maps Utilizada

**Distance Matrix API**

- **URL Base**: `https://maps.googleapis.com/maps/api`
- **Endpoint**: `/distancematrix/json`
- **Parámetros**:
  - `origins`: Coordenadas de origen
  - `destinations`: Coordenadas de destino
  - `units`: metric (kilómetros)
  - `key`: API Key de Google Maps

**Ejemplo de URL completa:**
```
https://maps.googleapis.com/maps/api/distancematrix/json?origins=-31.4167,-64.1833&destinations=-32.8908,-68.8272&units=metric&key=YOUR_API_KEY
```

---

## 📚 Dependencias Principales

```xml
<dependencies>
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    
    <!-- Swagger UI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
    </dependency>
</dependencies>
```

---

## ⚠️ Notas Importantes

1. **Límites de Google Maps API**:
   - Gratis: 40,000 requests/mes
   - Después: $5 por cada 1,000 requests adicionales

2. **Formatos de coordenadas**:
   - Latitud: -90 a 90
   - Longitud: -180 a 180
   - Formato: "latitud,longitud" (sin espacios)

3. **Puerto**:
   - Por defecto: 8080
   - Si está ocupado, cambiar en `application.properties`

---

## 🐛 Troubleshooting

### Error: "REQUEST_DENIED"
- **Causa**: API Key inválida o Distance Matrix API no habilitada
- **Solución**: Verificar que Distance Matrix API esté habilitada en Google Cloud Console

### Error: "OVER_QUERY_LIMIT"
- **Causa**: Se excedió el límite de requests
- **Solución**: Configurar facturación en Google Cloud o esperar al siguiente mes

### Error: Puerto 8080 en uso
- **Solución**: Cambiar puerto en `application.properties`:
  ```properties
  server.port=8081
  ```

---

## 📖 Referencias

- [Google Maps Distance Matrix API Documentation](https://developers.google.com/maps/documentation/distance-matrix/overview)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [RestClient Documentation](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)

---

## ✅ Checklist de Configuración

- [ ] Cuenta de Google Cloud creada
- [ ] Distance Matrix API habilitada
- [ ] API Key generada y copiada
- [ ] API Key configurada en `application.properties`
- [ ] Proyecto compilado sin errores (`mvn clean install`)
- [ ] Servicio iniciado correctamente (`mvn spring-boot:run`)
- [ ] Prueba exitosa en Swagger UI

---

**Estado**: ✅ Implementación completa según documento de referencia
