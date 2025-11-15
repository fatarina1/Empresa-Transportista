package ar.edu.tpi.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Conversor de roles de Keycloak para Spring Security.
 * 
 * Extrae los roles desde el claim 'resource_access.tpi-backend-client.roles'
 * del JWT emitido por Keycloak y los convierte en authorities de Spring Security
 * con el prefijo 'ROLE_' requerido para usar hasRole().
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Extraer resource_access del JWT
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        
        if (resourceAccess == null) {
            return Collections.emptyList();
        }
        
        // Buscar el cliente 'tpi-backend-client'
        @SuppressWarnings("unchecked")
        Map<String, Object> tpiBackendClient = (Map<String, Object>) resourceAccess.get("tpi-backend-client");
        
        if (tpiBackendClient == null) {
            return Collections.emptyList();
        }
        
        // Extraer los roles
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) tpiBackendClient.get("roles");
        
        if (roles == null) {
            return Collections.emptyList();
        }
        
        // Convertir a GrantedAuthority con prefijo ROLE_
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
