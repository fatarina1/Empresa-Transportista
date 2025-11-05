package ar.edu.tpi.logistica.clients;

import ar.edu.tpi.logistica.dto.DistanciaDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GeoApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${geoapi.service.url:http://localhost:8083}")
    private String geoApiServiceUrl;

    public GeoApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calcula la distancia entre dos puntos geográficos
     */
    public DistanciaDTO calcularDistancia(Double origenLat, Double origenLng, 
                                           Double destinoLat, Double destinoLng) {
        try {
            String url = geoApiServiceUrl + "/api/geoapi/distancia" +
                    "?origenLat=" + origenLat +
                    "&origenLng=" + origenLng +
                    "&destinoLat=" + destinoLat +
                    "&destinoLng=" + destinoLng;
            
            Double distanciaKm = restTemplate.getForObject(url, Double.class);
            
            DistanciaDTO resultado = new DistanciaDTO();
            resultado.setDistanciaKm(distanciaKm);
            resultado.setOrigen(origenLat + "," + origenLng);
            resultado.setDestino(destinoLat + "," + destinoLng);
            // Estimar duración: 60 km/h promedio
            resultado.setDuracionMinutos((int) (distanciaKm / 60.0 * 60));
            
            return resultado;
            
        } catch (Exception e) {
            log.error("Error al calcular distancia desde geoapi", e);
            return null;
        }
    }

    /**
     * Calcula una ruta completa con todos los detalles
     */
    public DistanciaDTO calcularRuta(Map<String, Object> origen, Map<String, Object> destino) {
        try {
            String url = geoApiServiceUrl + "/api/geoapi/calcular-ruta";
            
            Map<String, Object> request = new HashMap<>();
            request.put("origen", origen);
            request.put("destino", destino);
            
            return restTemplate.postForObject(url, request, DistanciaDTO.class);
            
        } catch (Exception e) {
            log.error("Error al calcular ruta desde geoapi", e);
            return null;
        }
    }
}
