package ar.edu.tpi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
public class KeycloakAuthController {

    /**
     * Simple endpoint that redirects the user to the Spring Security OAuth2 authorization endpoint.
     * Spring Security exposes /oauth2/authorization/{registrationId} by default, so we redirect there.
     */
    @GetMapping("/api/login")
    public ResponseEntity<Void> loginRedirect() {
        // Return a 302 redirect to Spring Security's authorization endpoint
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/oauth2/authorization/keycloak"))
                .build();
    }

    /**
     * After successful OAuth2 login, Spring Security stores the authorized client. This endpoint
     * returns basic token information for the currently authenticated user and authorized client.
     * Requires the user to be authenticated via OAuth2 Login (Keycloak).
     */
    @GetMapping("/api/me/token")
    public ResponseEntity<Map<String, Object>> getToken(
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient,
            @AuthenticationPrincipal OidcUser oidcUser) {

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("access_token", authorizedClient.getAccessToken().getTokenValue());
        resp.put("issued_at", authorizedClient.getAccessToken().getIssuedAt());
        resp.put("expires_at", authorizedClient.getAccessToken().getExpiresAt());
        resp.put("refresh_token", authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null);
        resp.put("username", oidcUser != null ? oidcUser.getPreferredUsername() : null);

        return ResponseEntity.ok(resp);
    }
}
