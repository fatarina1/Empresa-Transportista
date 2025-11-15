package ar.edu.tpi.seguimiento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad del servicio de Seguimiento.
 * 
 * Reglas de autorización según enunciado TPI:
 * 
 * Cliente (rol: cliente):
 * - Registra pedidos de traslado (POST /solicitudes)
 * - Consulta el estado de su contenedor (GET /solicitudes)
 * - Ve costo y tiempo estimado (GET /solicitudes/{id})
 * 
 * Operador/Administrador (rol: admin):
 * - Consulta rutas tentativas (GET /solicitudes)
 * - Asigna ruta a solicitud (PUT /solicitudes)
 * - Consulta contenedores pendientes (GET /contenedores)
 * - Asigna camión a tramo (en logistica-service)
 * - Gestiona clientes
 * 
 * Transportista (rol: transportista):
 * - Ve tramos asignados (GET /solicitudes - filtrado por su usuario)
 * - Registra inicio y fin de tramo (PUT /solicitudes - solo sus tramos)
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
                
                // Solicitudes: Todos los roles con diferentes permisos
                .requestMatchers(HttpMethod.GET, "/solicitudes/**").hasAnyRole("cliente", "transportista", "admin")
                .requestMatchers(HttpMethod.POST, "/solicitudes/**").hasAnyRole("cliente", "admin") // Cliente registra pedidos
                .requestMatchers(HttpMethod.PUT, "/solicitudes/**").hasAnyRole("transportista", "admin") // Transportista registra inicio/fin, Admin asigna rutas
                .requestMatchers(HttpMethod.DELETE, "/solicitudes/**").hasRole("admin")
                
                // Contenedores: Todos pueden ver, solo ADMIN gestiona
                .requestMatchers(HttpMethod.GET, "/contenedores/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/contenedores/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/contenedores/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/contenedores/**").hasRole("admin")
                
                // Clientes: Solo ADMIN gestiona clientes
                .requestMatchers("/clientes/**").hasRole("admin")
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return jwtAuthenticationConverter;
    }
}
