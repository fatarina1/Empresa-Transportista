package ar.edu.tpi.seguimiento.clients;

import ar.edu.tpi.seguimiento.dto.UbicacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticaServiceClient {

    private final RestClient.Builder restClientBuilder;
    
    @Value("${logistica.service.url:http://localhost:8082}")
    private String logisticaServiceUrl;

    /**
     * Obtiene información de una ubicación desde el servicio de logística.     * 
     * @param ubicacionId ID de la ubicación a consultar
     * @return UbicacionDTO con los datos de la ubicación
     */
    public UbicacionDTO obtenerUbicacion(Long ubicacionId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(logisticaServiceUrl)
                    .build();
            
            return restClient.get()
                    .uri("/ubicaciones/{id}", ubicacionId)
                    .retrieve()
                    .body(UbicacionDTO.class);
                    
        } catch (Exception e) {
            log.error("Error al obtener ubicación {} desde logistica-service", ubicacionId, e);
            return null;
        }
    }

    /**
     * Crea una nueva ubicación en el servicio de logística.
     * 
     * @param ubicacionDTO Datos de la ubicación a crear
     * @return UbicacionDTO con la ubicación creada (incluyendo ID)
     */
    public UbicacionDTO crearUbicacion(UbicacionDTO ubicacionDTO) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(logisticaServiceUrl)
                    .build();
            
            return restClient.post()
                    .uri("/ubicaciones")
                    .body(ubicacionDTO)
                    .retrieve()
                    .body(UbicacionDTO.class);
                    
        } catch (Exception e) {
            log.error("Error al crear ubicación en logistica-service", e);
            return null;
        }
    }

    /**
     * Verifica disponibilidad de camiones para un peso y volumen específico.
     * 
     * @param pesoKg Peso en kilogramos
     * @param volumenM3 Volumen en metros cúbicos
     * @return true si hay camiones disponibles, false en caso contrario
     */
    public boolean verificarDisponibilidadCamiones(Double pesoKg, Double volumenM3) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(logisticaServiceUrl)
                    .build();
            
            Boolean disponible = restClient.get()
                    .uri("/camiones/verificar-disponibilidad?peso={peso}&volumen={volumen}", pesoKg, volumenM3)
                    .retrieve()
                    .body(Boolean.class);
                    
            return Boolean.TRUE.equals(disponible);
            
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad de camiones", e);
            return false;
        }
    }
}
