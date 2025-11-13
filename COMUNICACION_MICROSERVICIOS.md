# Comunicación entre Microservicios

## 📋 Resumen de Cambios

Se ha reacomodado la arquitectura de microservicios para seguir las mejores prácticas definidas en los documentos oficiales del TPI:
- **Apunte 18**: Consumo de API Externa con RestClient
- **Apunte 19**: API Gateway

## 🔄 Cambios Realizados

### 1. Migración de RestTemplate a RestClient

**Antes** (deprecado):
```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

**Ahora** (Spring 6+ / Boot 3.2+):
```java
@Bean
public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
}
```

### 2. Actualización de Clientes HTTP

#### GeoApiClient (logistica-service)

**Antes**: Usaba RestTemplate con múltiples parámetros de coordenadas
```java
DistanciaDTO calcularDistancia(Double origenLat, Double origenLng, 
                               Double destinoLat, Double destinoLng)
```

**Ahora**: Usa RestClient con strings como define el documento oficial
```java
public DistanciaDTO calcularDistancia(String origen, String destino) {
    RestClient restClient = restClientBuilder
            .baseUrl(geoApiServiceUrl)
            .build();
    
    return restClient.get()
            .uri("/api/distancia?origen={origen}&destino={destino}", origen, destino)
            .retrieve()
            .body(DistanciaDTO.class);
}
```

**Formato de parámetros**:
- Coordenadas: `"-31.4167,-64.1833"`
- Direcciones: `"Córdoba, Argentina"`

#### LogisticaServiceClient (seguimiento-service)

**Antes**: Usaba RestTemplate con concatenación manual de URLs
```java
String url = logisticaServiceUrl + "/api/ubicaciones/" + ubicacionId;
return restTemplate.getForObject(url, UbicacionDTO.class);
```

**Ahora**: Usa RestClient con URI templates
```java
public UbicacionDTO obtenerUbicacion(Long ubicacionId) {
    RestClient restClient = restClientBuilder
            .baseUrl(logisticaServiceUrl)
            .build();
    
    return restClient.get()
            .uri("/api/ubicaciones/{id}", ubicacionId)
            .retrieve()
            .body(UbicacionDTO.class);
}
```

### 3. Actualización de DTOs

#### DistanciaDTO

**Antes**:
```java
private Double distanciaKm;
private Integer duracionMinutos;
private String polilinea;
```

**Ahora** (coincide exactamente con documento oficial de GeoAPI):
```java
private String origen;
private String destino;
private double kilometros;          // Nombre exacto del documento
private String duracionTexto;       // String según documento (ej: "2 horas 15 mins")
```

### 4. Configuración de Puertos

| Servicio | Puerto |
|----------|--------|
| **geoapi** | 8080 |
| **seguimiento-service** | 8081 |
| **logistica-service** | 8082 |
| **api-gateway** | 8090 |

**Cambio importante**: El Gateway se movió del puerto 8080 al 8090 para liberar el 8080 para GeoAPI (según documento oficial).

### 5. API Gateway - Ruteo

El Gateway mantiene su configuración programática en `GatewayConfig.java`:

```java
@Bean
public RouteLocator configurarRutas(RouteLocatorBuilder builder) {
    return builder.routes()
        // Logística
        .route("logistica-camiones", r -> r
            .path("/api/logistica/camiones/**")
            .filters(f -> f.stripPrefix(2))
            .uri(urlLogistica))
        
        // Seguimiento  
        .route("seguimiento-solicitudes", r -> r
            .path("/api/seguimiento/solicitudes/**")
            .filters(f -> f.stripPrefix(2))
            .uri(urlSeguimiento))
        
        // GeoAPI
        .route("geoapi-calcular", r -> r
            .path("/api/geoapi/**")
            .filters(f -> f.stripPrefix(2))
            .uri(urlGeoapi))
        
        .build();
}
```

## 🌐 Flujo de Comunicación

### Ejemplo 1: Cliente → Gateway → GeoAPI
```
1. Cliente llama: http://localhost:8090/api/geoapi/distancia?origen=...&destino=...
2. Gateway recibe en puerto 8090
3. Gateway aplica stripPrefix(2) → elimina /api/geoapi
4. Gateway redirige a: http://localhost:8080/distancia?origen=...&destino=...
5. GeoAPI responde con DistanciaDTO
6. Gateway devuelve respuesta al cliente
```

### Ejemplo 2: Seguimiento → Logística (interno)
```
1. SolicitudService necesita validar ubicación
2. LogisticaServiceClient.obtenerUbicacion(123)
3. RestClient construye: http://localhost:8082/api/ubicaciones/123
4. Logística responde con UbicacionDTO
5. Seguimiento procesa la respuesta
```

### Ejemplo 3: Logística → GeoAPI (interno)
```
1. TramoService necesita calcular distancia
2. GeoApiClient.calcularDistancia("-31.42,-64.18", "-32.89,-68.83")
3. RestClient construye: http://localhost:8080/api/distancia?origen=-31.42,-64.18&destino=-32.89,-68.83
4. GeoAPI responde con DistanciaDTO {kilometros: 410.5, duracionTexto: "5 horas 30 mins"}
5. TramoService guarda distancia en BD
```

## ✅ Ventajas de RestClient

Según **Apunte 18**:

1. **Bloqueante y moderno**: Reemplazo natural de RestTemplate
2. **API fluida**: Código más legible y expresivo
3. **Mejor manejo de errores**: Control más granular
4. **URI Templates**: Parámetros tipados y seguros
5. **Integración Spring**: Compatible con interceptores, métricas, etc.

## 🔧 Configuración de Propiedades

### logistica-service/application.properties
```properties
server.port=8082
spring.application.name=logistica-service
geoapi.service.url=http://localhost:8080
```

### seguimiento-service/application.properties
```properties
server.port=8081
spring.application.name=seguimiento-service
logistica.service.url=http://localhost:8082
```

### api-gateway/application.yml
```yaml
server:
  port: 8090

api-gateway:
  url-logistica-service: http://localhost:8082
  url-seguimiento-service: http://localhost:8081
  url-geoapi-service: http://localhost:8080
```

## 🚀 Para Ejecutar

1. **Levantar servicios en orden**:
   ```bash
   # Terminal 1 - GeoAPI
   cd geoapi
   mvn spring-boot:run
   
   # Terminal 2 - Logística
   cd logistica-service
   mvn spring-boot:run
   
   # Terminal 3 - Seguimiento
   cd seguimiento-service
   mvn spring-boot:run
   
   # Terminal 4 - Gateway
   cd api-gateway
   mvn spring-boot:run
   ```

2. **Verificar servicios**:
   - GeoAPI: http://localhost:8080/swagger-ui.html
   - Logística: http://localhost:8082/swagger-ui.html
   - Seguimiento: http://localhost:8081/swagger-ui.html
   - Gateway: http://localhost:8090/actuator/health

## 📚 Referencias

- **Apunte 18**: Cliente HTTP con RestClient (Spring 6+)
- **Apunte 19**: API Gateway con Spring Cloud Gateway
- **Documento GeoAPI**: Google Maps Distance Matrix API

## ⚠️ Importante

- **NO usar RestTemplate**: Está marcado como deprecado desde Spring 6
- **Respetar puertos**: GeoAPI debe estar en 8080 según documento oficial
- **Usar RestClient.Builder**: Inyectar el builder, no crear instancias directamente
- **URI Templates**: Usar placeholders `{variable}` en lugar de concatenación de strings
