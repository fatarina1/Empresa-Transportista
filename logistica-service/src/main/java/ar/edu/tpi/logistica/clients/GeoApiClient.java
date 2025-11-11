package ar.edu.tpi.logistica.clients;

import ar.edu.tpi.logistica.dto.DistanciaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoApiClient {

    private final RestClient.Builder restClientBuilder;
    
    @Value("${geoapi.service.url:http://localhost:8080}")
    private String geoApiServiceUrl;

    /**
     * Calcula la distancia entre dos ubicaciones usando el servicio GeoAPI.
     * Utiliza RestClient (Spring 6+) para realizar la petición HTTP.
     * 
     * @param origen Dirección o coordenadas de origen (ej: "-31.4167,-64.1833" o "Córdoba, Argentina")
     * @param destino Dirección o coordenadas de destino
     * @return DistanciaDTO con kilómetros y duración en texto
     */
    public DistanciaDTO calcularDistancia(String origen, String destino) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(geoApiServiceUrl)
                    .build();
            
            return restClient.get()
                    .uri("/api/distancia?origen={origen}&destino={destino}", origen, destino)
                    .retrieve()
                    .body(DistanciaDTO.class);
            
        } catch (Exception e) {
            log.error("Error al calcular distancia desde geoapi: origen={}, destino={}", origen, destino, e);
            return null;
        }
    }
}
