package ar.edu.tpi.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Filtro personalizado que extrae información del JWT autenticado
 * y la pasa como headers a los microservicios downstream.
 * 
 * Headers agregados:
 * - X-User-Id: username del usuario autenticado (ej: "cliente01", "admin01")
 * - X-User-Roles: roles del usuario separados por coma (ej: "CLIENTE,USER")
 */
@Component
public class UserContextFilter extends AbstractGatewayFilterFactory<UserContextFilter.Config> {

    public UserContextFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication())
            .flatMap(authentication -> {
                if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    
                    // Extraer preferred_username del JWT
                    String username = jwt.getClaimAsString("preferred_username");
                    
                    // Extraer roles (opcional, para debugging)
                    String roles = String.join(",", authentication.getAuthorities().stream()
                        .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                        .toList());
                    
                    // IMPORTANTE: Crear un nuevo request con los headers agregados
                    var mutatedExchange = exchange.mutate()
                        .request(builder -> {
                            if (username != null) {
                                builder.header("X-User-Id", username);
                            }
                            if (!roles.isEmpty()) {
                                builder.header("X-User-Roles", roles);
                            }
                        })
                        .build();
                    
                    return chain.filter(mutatedExchange);
                }
                
                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange)); // Si no hay contexto de seguridad, continuar
    }

    public static class Config {
        // Configuración vacía, puede extenderse en el futuro
    }
}
