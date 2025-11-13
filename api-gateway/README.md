# API Gateway - Sistema de Transporte de Contenedores

## 📋 Descripción

API Gateway que actúa como **punto único de entrada** para los microservicios del sistema de transporte. Implementado con **Spring Cloud Gateway** siguiendo el **Apunte 19** de la cátedra.

## 🏗️ Arquitectura

```
Cliente (Browser/Postman)
        ↓
  API Gateway :8080
        ↓
    ┌───────┴────────┬─────────────┐
    ↓                ↓             ↓
Seguimiento:8081  Logística:8082  GeoAPI:8083
```

## 🚀 Características Implementadas (según Apunte 19)

### ✅ Ruteo de Peticiones
- **Configuración programática** con `RouteLocator` (GatewayConfig.java)
- URLs externalizadas en `application.yml`
- Predicados basados en `path()`
- Filtro `stripPrefix()` para normalización de rutas

### ✅ Filtros Predefinidos (por ruta)
- **stripPrefix(2)** - Remueve `/api/logistica` o `/api/seguimiento` del path
- **addRequestHeader()** - Agrega `X-Gateway-Service` para identificar el servicio
- **addResponseHeader()** - Agrega `X-Processed-By: API-Gateway` en respuestas

### ✅ CORS (Cross-Origin Resource Sharing)
- Configuración en `application.yml`
- Soporta múltiples frontends (React, Angular, Vite)
- Headers y credenciales habilitados

### ✅ Monitoreo
- Spring Boot Actuator habilitado
- Endpoints: health, metrics, gateway/routes
- Health checks con detalles completos

## 📡 Rutas Configuradas

### Seguimiento Service (`:8081`)
| Endpoint Gateway | Microservicio Destino |
|-----------------|------------------------|
| `GET /api/seguimiento/solicitudes/**` | `GET /solicitudes/**` |
| `POST /api/seguimiento/solicitudes` | `POST /solicitudes` |
| `GET /api/seguimiento/clientes/**` | `GET /clientes/**` |
| `GET /api/seguimiento/contenedores/**` | `GET /contenedores/**` |

### Logística Service (`:8082`)
| Endpoint Gateway | Microservicio Destino |
|-----------------|------------------------|
| `GET /api/logistica/camiones/**` | `GET /camiones/**` |
| `GET /api/logistica/depositos/**` | `GET /depositos/**` |
| `GET /api/logistica/rutas/**` | `GET /rutas/**` |
| `GET /api/logistica/tramos/**` | `GET /tramos/**` |
| `GET /api/logistica/ubicaciones/**` | `GET /ubicaciones/**` |
| `GET /api/logistica/tarifas/**` | `GET /tarifas/**` |

### GeoAPI Service (`:8083`)
| Endpoint Gateway | Microservicio Destino |
|-----------------|------------------------|
| `POST /api/geoapi/calcular-ruta` | `POST /calcular-ruta` |
| `GET /api/geoapi/distancia` | `GET /distancia` |

## ⚙️ Configuración

### application.yml
```yaml
api-gateway:
  url-logistica-service: http://localhost:8082
  url-seguimiento-service: http://localhost:8081
  url-geoapi-service: http://localhost:8083
```

### GatewayConfig.java (Configuración Programática)
```java
@Bean
public RouteLocator configurarRutas(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("logistica-camiones", r -> r
            .path("/api/logistica/camiones/**")
            .filters(f -> f.stripPrefix(2))
            .uri(urlLogistica))
        .build();
}
```

## 🎯 Ventajas (según Apunte 19)

✅ **Único punto de entrada**: Clientes solo conocen `localhost:8080`
✅ **Desacoplamiento**: Servicios internos no se exponen directamente
✅ **Configuración programática**: Mayor flexibilidad que YAML declarativo
✅ **Filtros predefinidos**: Uso de filtros como `stripPrefix`, `addRequestHeader`
✅ **CORS centralizado**: No se replica en cada microservicio

## Ejecución

### Requisitos previos
- Java 21
- Maven 3.8+
- Los microservicios `logistica-service` y `seguimiento-service` deben estar corriendo

### Comandos

**Compilar:**
```bash
./mvnw clean package
```

**Ejecutar:**
```bash
./mvnw spring-boot:run
```

O con el JAR:
```bash
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

## Ejemplo de uso

```bash
# En lugar de llamar directamente a los servicios:
curl http://localhost:8082/camiones

# Llamar a través del gateway:
curl http://localhost:8080/api/logistica/camiones
```

## Configuración

La configuración principal se encuentra en `src/main/resources/application.yml`

Para cambiar puertos de los servicios backend, editar las URIs en las rutas del gateway.

## Tecnologías

- Spring Boot 3.5.6
- Spring Cloud Gateway 2025.0.0
- Java 21
- Lombok
- Spring Boot Actuator
