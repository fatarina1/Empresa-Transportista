package ar.edu.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * Configuración de seguridad centralizada en el API Gateway.
 * 
 * Esta clase maneja:
 * - Autenticación mediante JWT (OAuth2 Resource Server)
 * - Autorización basada en roles (RBAC) extraídos de Keycloak
 * - Control de acceso a todos los endpoints de los microservicios
 * 
 * Los microservicios NO tienen configuración de seguridad, 
 * toda la validación se realiza aquí en el Gateway.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // ============================================
                // ENDPOINTS PÚBLICOS (sin autenticación)
                // ============================================
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // ============================================
                // LOGÍSTICA SERVICE - RBAC
                // ============================================
                
                // CAMIONES
                .pathMatchers(HttpMethod.GET, "/api/logistica/camiones/**").hasAnyRole("admin", "transportista")
                .pathMatchers(HttpMethod.POST, "/api/logistica/camiones/**").hasRole("admin")
                .pathMatchers(HttpMethod.PUT, "/api/logistica/camiones/**").hasRole("admin")
                .pathMatchers(HttpMethod.DELETE, "/api/logistica/camiones/**").hasRole("admin")
                
                // DEPÓSITOS
                .pathMatchers("/api/logistica/depositos/**").hasRole("admin")
                
                // UBICACIONES
                .pathMatchers("/api/logistica/ubicaciones/**").hasRole("admin")
                
                // RUTAS
                .pathMatchers(HttpMethod.GET, "/api/logistica/rutas/**").hasAnyRole("admin", "transportista")
                .pathMatchers(HttpMethod.POST, "/api/logistica/rutas/**").hasRole("admin")
                .pathMatchers(HttpMethod.PUT, "/api/logistica/rutas/**").hasRole("admin")
                .pathMatchers(HttpMethod.DELETE, "/api/logistica/rutas/**").hasRole("admin")
                
                // TRAMOS
                .pathMatchers(HttpMethod.GET, "/api/logistica/tramos/**").hasAnyRole("admin", "transportista")
                .pathMatchers(HttpMethod.POST, "/api/logistica/tramos/**").hasRole("admin")
                .pathMatchers(HttpMethod.PUT, "/api/logistica/tramos/**").hasRole("admin")
                .pathMatchers(HttpMethod.DELETE, "/api/logistica/tramos/**").hasRole("admin")
                
                // TARIFAS
                .pathMatchers("/api/logistica/tarifas/**").hasRole("admin")
                
                // ============================================
                // SEGUIMIENTO SERVICE - RBAC
                // ============================================
                
                // SOLICITUDES
                .pathMatchers(HttpMethod.GET, "/api/seguimiento/solicitudes/**").hasAnyRole("cliente", "transportista", "admin")
                .pathMatchers(HttpMethod.POST, "/api/seguimiento/solicitudes/**").hasAnyRole("cliente", "admin")
                .pathMatchers(HttpMethod.PUT, "/api/seguimiento/solicitudes/**").hasAnyRole("transportista", "admin")
                .pathMatchers(HttpMethod.DELETE, "/api/seguimiento/solicitudes/**").hasRole("admin")
                
                // CONTENEDORES
                .pathMatchers(HttpMethod.GET, "/api/seguimiento/contenedores/**").authenticated()
                .pathMatchers(HttpMethod.POST, "/api/seguimiento/contenedores/**").hasAnyRole("cliente", "admin")
                .pathMatchers(HttpMethod.PUT, "/api/seguimiento/contenedores/**").hasAnyRole("cliente", "admin")
                .pathMatchers(HttpMethod.DELETE, "/api/seguimiento/contenedores/**").hasRole("admin")
                
                // CLIENTES
                .pathMatchers("/api/seguimiento/clientes/**").hasRole("admin")
                
                // ============================================
                // GEOAPI SERVICE
                // ============================================
                .pathMatchers("/geoapi/**").authenticated()
                
                // Cualquier otra ruta requiere autenticación
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .build();
    }
    
    /**
     * Convierte el JWT de Keycloak extrayendo los roles desde 
     * resource_access.tpi-backend-client.roles
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
