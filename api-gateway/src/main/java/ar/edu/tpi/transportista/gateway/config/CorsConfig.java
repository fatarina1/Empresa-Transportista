package main.java.ar.edu.tpi.transportista.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para el API Gateway.
 * 
 * Permite que aplicaciones frontend desde diferentes orígenes (localhost:3000, etc.)
 * puedan realizar peticiones al backend a través del gateway.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Orígenes permitidos (aplicaciones frontend)
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React (default)
                "http://localhost:4200",  // Angular (default)
                "http://localhost:5173",  // Vite (default)
                "http://localhost:8080"   // Desarrollo local
        ));
        
        // Métodos HTTP permitidos
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers permitidos
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir envío de credenciales (cookies, headers de autorización)
        corsConfig.setAllowCredentials(true);
        
        // Tiempo de caché para preflight requests (en segundos)
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
