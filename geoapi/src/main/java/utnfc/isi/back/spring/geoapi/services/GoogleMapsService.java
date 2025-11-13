package utnfc.isi.back.spring.geoapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import utnfc.isi.back.spring.geoapi.dto.RutaResponseDTO;

/**
 * Servicio para integración con Google Maps Directions API
 */
@Slf4j
@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key:}")
    private String apiKey;

    @Value("${google.maps.api.url:https://maps.googleapis.com/maps/api}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calcula la ruta entre dos puntos usando Google Maps Directions API
     * Si la API Key no está configurada o falla, usa Haversine como fallback
     */
    public RutaResponseDTO calcularRuta(Double origenLat, Double origenLng, 
                                        Double destinoLat, Double destinoLng) {
        String origin = origenLat + "," + origenLng;
        String destination = destinoLat + "," + destinoLng;
        
        // Si la API Key no está configurada o es la de ejemplo, usar Haversine
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            log.warn("Google Maps API Key no configurada. Usando cálculo Haversine como fallback.");
            return calcularConHaversine(origin, destination, origenLat, origenLng, destinoLat, destinoLng);
        }

        try {
            // Llamada real a Google Maps Directions API
            String url = String.format(
                "%s/directions/json?origin=%s&destination=%s&key=%s&mode=driving&language=es",
                apiUrl, origin, destination, apiKey
            );

            log.info("Llamando a Google Maps API para calcular ruta desde ({},{}) hasta ({},{})", 
                    origenLat, origenLng, destinoLat, destinoLng);

            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Verificar el status de la respuesta
            String status = rootNode.path("status").asText();
            
            if (!"OK".equals(status)) {
                log.warn("Google Maps API retornó status: {}. Usando Haversine como fallback.", status);
                return calcularConHaversine(origin, destination, origenLat, origenLng, destinoLat, destinoLng);
            }

            // Extraer datos de la ruta
            JsonNode routes = rootNode.path("routes");
            if (routes.isEmpty()) {
                log.warn("No se encontraron rutas. Usando Haversine como fallback.");
                return calcularConHaversine(origin, destination, origenLat, origenLng, destinoLat, destinoLng);
            }

            JsonNode route = routes.get(0);
            JsonNode leg = route.path("legs").get(0);

            // Distancia en metros, convertir a km
            double distanciaMetros = leg.path("distance").path("value").asDouble();
            double distanciaKm = distanciaMetros / 1000.0;

            // Duración en segundos, convertir a minutos
            double duracionSegundos = leg.path("duration").path("value").asDouble();
            int duracionMinutos = (int) (duracionSegundos / 60.0);

            // Polilínea encodificada
            String polilinea = route.path("overview_polyline").path("points").asText();

            // Construir respuesta
            RutaResponseDTO response = new RutaResponseDTO();
            response.setOrigen(origin);
            response.setDestino(destination);
            response.setDistanciaKm(Math.round(distanciaKm * 100.0) / 100.0); // Redondear a 2 decimales
            response.setDuracionMinutos(duracionMinutos);
            response.setPolilinea(polilinea);

            log.info("Ruta calculada exitosamente con Google Maps: {} km, {} minutos", 
                    response.getDistanciaKm(), response.getDuracionMinutos());

            return response;
            
        } catch (Exception e) {
            log.error("Error al llamar a Google Maps API: {}. Usando Haversine como fallback.", e.getMessage());
            return calcularConHaversine(origin, destination, origenLat, origenLng, destinoLat, destinoLng);
        }
    }

    /**
     * Método fallback usando fórmula de Haversine cuando Google Maps API no está disponible
     */
    private RutaResponseDTO calcularConHaversine(String origin, String destination,
                                                double lat1, double lon1, double lat2, double lon2) {
        log.info("Usando cálculo Haversine para distancia aproximada");
        
        double distancia = calcularDistanciaHaversine(lat1, lon1, lat2, lon2);
        
        RutaResponseDTO response = new RutaResponseDTO();
        response.setOrigen(origin);
        response.setDestino(destination);
        response.setDistanciaKm(Math.round(distancia * 100.0) / 100.0);
        
        // Estimar duración: 60 km/h promedio
        int duracion = (int) (distancia / 60.0 * 60); // minutos
        response.setDuracionMinutos(duracion);
        
        response.setPolilinea(""); // No disponible sin API real
        
        return response;
    }

    /**
     * Fórmula de Haversine para calcular distancia entre dos coordenadas
     */
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
