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
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // OPCIÓN 1: Intentar extraer desde resource_access.tpi-backend-client.roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tpiBackendClient = (Map<String, Object>) resourceAccess.get("tpi-backend-client");
            
            if (tpiBackendClient != null) {
                @SuppressWarnings("unchecked")
                List<String> clientRoles = (List<String>) tpiBackendClient.get("roles");
                
                if (clientRoles != null) {
                    clientRoles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .forEach(authorities::add);
                }
            }
        }
        
        // OPCIÓN 2 (FALLBACK): Si no hay roles de cliente, usar realm_access.roles
        if (authorities.isEmpty()) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                @SuppressWarnings("unchecked")
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                
                if (realmRoles != null) {
                    // Filtrar solo los roles que nos interesan (admin, cliente, transportista)
                    realmRoles.stream()
                            .filter(role -> role.equals("admin") || 
                                          role.equals("cliente") || 
                                          role.equals("transportista"))
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .forEach(authorities::add);
                }
            }
        }
        
        return authorities;
    }
}
