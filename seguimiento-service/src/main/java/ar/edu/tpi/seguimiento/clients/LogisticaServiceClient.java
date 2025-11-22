package ar.edu.tpi.seguimiento.clients;

import ar.edu.tpi.seguimiento.dto.TrackingInfoDTO;
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

    /**
     * Obtiene información de tracking en tiempo real de una ruta.
     * 
     * @param rutaId ID de la ruta a consultar
     * @return TrackingInfoDTO con información completa de tracking
     */
    public TrackingInfoDTO obtenerTrackingInfo(Long rutaId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(logisticaServiceUrl)
                    .build();
            
            return restClient.get()
                    .uri("/rutas/{id}/tracking", rutaId)
                    .retrieve()
                    .body(TrackingInfoDTO.class);
                    
        } catch (Exception e) {
            log.error("Error al obtener tracking info para ruta {} desde logistica-service", rutaId, e);
            return null;
        }
    }

    /**
     * Asigna una ruta a una solicitud existente
     */
    public void asignarRutaASolicitud(Long solicitudId, Long rutaId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(logisticaServiceUrl)
                    .build();

            restClient.patch()
                    .uri("/solicitudes/{id}/ruta", solicitudId)
                    .body(new RutaIdRequest(rutaId))
                    .retrieve()
                    .toBodilessEntity();

            log.info("Ruta {} asignada a solicitud {} correctamente", rutaId, solicitudId);

        } catch (Exception e) {
            log.error("Error al asignar ruta {} a solicitud {}", rutaId, solicitudId, e);
            throw new RuntimeException("No se pudo asignar la ruta a la solicitud", e);
        }
    }

    /**
     * Obtiene una solicitud por su rutaId
     */
    public SolicitudDTO obtenerSolicitudPorRutaId(Long rutaId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(logisticaServiceUrl)
                    .build();

            return restClient.get()
                    .uri("/solicitudes/ruta/{rutaId}", rutaId)
                    .retrieve()
                    .body(SolicitudDTO.class);

        } catch (Exception e) {
            log.error("Error al obtener solicitud por rutaId {} desde logistica-service", rutaId, e);
            return null;
        }
    }

    // DTO interno para enviar rutaId
    private record RutaIdRequest(Long rutaId) {}

    // DTO interno para recibir solicitud
    private record SolicitudDTO(Long id, String numero, Long rutaId) {}
}
