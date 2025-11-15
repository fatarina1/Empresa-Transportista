package ar.edu.tpi.logistica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad del servicio de Logística.
 * 
 * Reglas de autorización según enunciado TPI:
 * 
 * Operador/Administrador (rol: admin):
 * - Carga y actualiza ciudades, depósitos, tarifas, camiones
 * - Asigna camiones a tramos
 * - Modifica parámetros de tarifación
 * - Acceso total a este servicio
 * 
 * Transportista (rol: transportista):
 * - Solo lectura de camiones (para ver asignaciones)
 * - Solo lectura de rutas/tramos (para ver sus asignaciones)
 * 
 * Cliente (rol: cliente):
 * - SIN acceso a este servicio (solo opera en seguimiento)
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers("/actuator/health", "/actuator/info", "/openapi.yml", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Camiones: ADMIN gestiona, TRANSPORTISTA solo lee
                .requestMatchers(HttpMethod.GET, "/camiones/**").hasAnyRole("admin", "transportista")
                .requestMatchers(HttpMethod.POST, "/camiones/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/camiones/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/camiones/**").hasRole("admin")
                
                // Depósitos: Solo ADMIN (gestiona depósitos)
                .requestMatchers("/depositos/**").hasRole("admin")
                
                // Ubicaciones: Solo ADMIN (carga y actualiza ciudades)
                .requestMatchers("/ubicaciones/**").hasRole("admin")
                
                // Tarifas: Solo ADMIN (modifica parámetros de tarifación)
                .requestMatchers("/tarifas/**").hasRole("admin")
                
                // Rutas: ADMIN gestiona, TRANSPORTISTA lee
                .requestMatchers(HttpMethod.GET, "/rutas/**").hasAnyRole("admin", "transportista")
                .requestMatchers(HttpMethod.POST, "/rutas/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/rutas/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/rutas/**").hasRole("admin")
                
                // Tramos: ADMIN asigna, TRANSPORTISTA lee
                .requestMatchers(HttpMethod.GET, "/tramos/**").hasAnyRole("admin", "transportista")
                .requestMatchers(HttpMethod.POST, "/tramos/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/tramos/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/tramos/**").hasRole("admin")
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }
    
    /**
     * Convierte los roles de Keycloak (resource_access.tpi-backend-client.roles)
     * a authorities de Spring Security con prefijo ROLE_
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // Extraer roles de resource_access.tpi-backend-client.roles
        grantedAuthoritiesConverter.setAuthoritiesClaimName("resource_access.tpi-backend-client.roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        
        return jwtAuthenticationConverter;
    }
}
