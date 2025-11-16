package ar.edu.tpi.gateway.config;

import ar.edu.tpi.gateway.filter.UserContextFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración programática del API Gateway.
 * Esta configuración define las rutas hacia los microservicios del sistema.
 * 
 * Beneficios de configuración programática vs YAML:
 * - Mayor flexibilidad para lógica condicional
 * - Mejor control de filtros personalizados
 * - Más fácil de testear y mantener
 * - Autocomplete y validación en tiempo de compilación
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator configurarRutas(
            RouteLocatorBuilder builder,
            @Value("${api-gateway.url-logistica-service}") String urlLogistica,
            @Value("${api-gateway.url-seguimiento-service}") String urlSeguimiento,
            @Value("${api-gateway.url-geoapi-service}") String urlGeoapi,
            UserContextFilter userContextFilter) {
        
        return builder.routes()
                // ============================================
                // RUTAS PARA LOGISTICA-SERVICE
                // ============================================
                
                // Camiones
                .route("logistica-camiones", r -> r
                        .path("/api/logistica/camiones/**")
                        .filters(f -> f
                                .stripPrefix(2)  // Remueve /api/logistica
                                .addRequestHeader("X-Gateway-Service", "logistica")
                                .addResponseHeader("X-Processed-By", "API-Gateway"))
                        .uri(urlLogistica))
                
                // Depósitos
                .route("logistica-depositos", r -> r
                        .path("/api/logistica/depositos/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway-Service", "logistica"))
                        .uri(urlLogistica))
                
                // Ubicaciones
                .route("logistica-ubicaciones", r -> r
                        .path("/api/logistica/ubicaciones/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway-Service", "logistica"))
                        .uri(urlLogistica))
                
                // Rutas
                .route("logistica-rutas", r -> r
                        .path("/api/logistica/rutas/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway-Service", "logistica"))
                        .uri(urlLogistica))
                
                // Tramos
                .route("logistica-tramos", r -> r
                        .path("/api/logistica/tramos/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway-Service", "logistica"))
                        .uri(urlLogistica))
                
                // Tarifas
                .route("logistica-tarifas", r -> r
                        .path("/api/logistica/tarifas/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway-Service", "logistica"))
                        .uri(urlLogistica))
                
                // ============================================
                // RUTAS PARA SEGUIMIENTO-SERVICE
                // ============================================
                
                // Solicitudes
                .route("seguimiento-solicitudes", r -> r
                        .path("/api/seguimiento/solicitudes/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(userContextFilter.apply(new UserContextFilter.Config()))
                                .addRequestHeader("X-Gateway-Service", "seguimiento")
                                .addResponseHeader("X-Processed-By", "API-Gateway"))
                        .uri(urlSeguimiento))
                
                // Contenedores
                .route("seguimiento-contenedores", r -> r
                        .path("/api/seguimiento/contenedores/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway-Service", "seguimiento"))
                        .uri(urlSeguimiento))
                
                // Clientes
                .route("seguimiento-clientes", r -> r
                        .path("/api/seguimiento/clientes/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway-Service", "seguimiento"))
                        .uri(urlSeguimiento))
                
                // ============================================
                // RUTAS PARA GEOAPI-SERVICE
                // ============================================
                
                // GeoAPI - Calcular rutas y distancias
                // Path: /geoapi/api/distancia → stripPrefix(1) → /api/distancia
                .route("geoapi-calcular", r -> r
                        .path("/geoapi/**")
                        .filters(f -> f
                                .stripPrefix(1)  // Solo elimina /geoapi, mantiene /api/distancia
                                .addRequestHeader("X-Gateway-Service", "geoapi"))
                        .uri(urlGeoapi))
                
                .build();
    }
}
