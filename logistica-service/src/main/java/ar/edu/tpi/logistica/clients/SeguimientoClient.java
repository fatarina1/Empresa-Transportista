package ar.edu.tpi.logistica.clients;

import ar.edu.tpi.logistica.dto.ContenedorRemotoDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeguimientoClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${seguimiento.service.url:http://localhost:8082}")
    private String seguimientoServiceUrl;

    
    // Obtiene el contenedor asociado a una solicitud mediante el endpoint
    // GET /solicitudes/{id}/detalle del servicio de seguimiento.
    public ContenedorRemotoDTO obtenerContenedorDeSolicitud(Long solicitudId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(seguimientoServiceUrl)
                    .build();

            SolicitudDetalleRemota detalle = restClient.get()
                    .uri("/solicitudes/{id}/detalle", solicitudId)
                    .retrieve()
                    .body(SolicitudDetalleRemota.class);

            if (detalle != null) {
                return detalle.getContenedor();
            }
        } catch (Exception e) {
            log.error("Error al obtener detalle de solicitud {} desde seguimiento: {}", solicitudId, e.getMessage());
        }
        return null;
    }

    @Data
    private static class SolicitudDetalleRemota {
        private ContenedorRemotoDTO contenedor;
    }
}
