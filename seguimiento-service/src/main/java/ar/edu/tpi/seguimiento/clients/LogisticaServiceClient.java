package ar.edu.tpi.seguimiento.clients;

import ar.edu.tpi.seguimiento.dto.UbicacionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class LogisticaServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${logistica.service.url:http://localhost:8082}")
    private String logisticaServiceUrl;

    public LogisticaServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene información de una ubicación desde el servicio de logística
     */
    public UbicacionDTO obtenerUbicacion(Long ubicacionId) {
        try {
            String url = logisticaServiceUrl + "/api/ubicaciones/" + ubicacionId;
            return restTemplate.getForObject(url, UbicacionDTO.class);
        } catch (Exception e) {
            log.error("Error al obtener ubicación {} desde logistica-service", ubicacionId, e);
            return null;
        }
    }

    /**
     * Verifica disponibilidad de camiones para un peso y volumen específico
     */
    public boolean verificarDisponibilidadCamiones(Double pesoKg, Double volumenM3) {
        try {
            String url = logisticaServiceUrl + "/api/camiones/verificar-disponibilidad?peso=" + pesoKg + "&volumen=" + volumenM3;
            Boolean disponible = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(disponible);
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad de camiones", e);
            return false;
        }
    }
}
