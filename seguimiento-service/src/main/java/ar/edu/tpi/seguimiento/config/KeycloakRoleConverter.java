package ar.edu.tpi.seguimiento.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Convertidor personalizado para extraer roles de Keycloak del token JWT.
 * 
 * Los roles en Keycloak se encuentran en:
 * resource_access.tpi-backend-client.roles
 * 
 * Este convertidor los convierte a authorities de Spring Security con prefijo ROLE_
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        
        if (resourceAccess != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get("tpi-backend-client");
            
            if (clientResource != null) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) clientResource.get("roles");
                
                if (roles != null) {
                    for (String role : roles) {
                        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            }
        }
        
        return grantedAuthorities;
    }
}
