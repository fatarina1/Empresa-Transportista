package ar.edu.tpi.logistica.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeguimientoServiceClient {

    private final RestClient.Builder restClientBuilder;
    
    @Value("${seguimiento.service.url:http://seguimiento-service:8081}")
    private String seguimientoServiceUrl;

    /**
     * Obtiene los datos del contenedor asociado a una solicitud
     */
    public ContenedorDetalleDTO obtenerContenedorPorSolicitudId(Long solicitudId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(seguimientoServiceUrl)
                    .build();
            
            // Primero obtenemos la solicitud
            SolicitudResponseDTO solicitud = restClient.get()
                    .uri("/solicitudes/{id}", solicitudId)
                    .retrieve()
                    .body(SolicitudResponseDTO.class);
            
            if (solicitud == null || solicitud.getContenedorId() == null) {
                log.warn("No se encontró contenedor para solicitud {}", solicitudId);
                return null;
            }
            
            // Luego obtenemos el contenedor
            return restClient.get()
                    .uri("/contenedores/{id}", solicitud.getContenedorId())
                    .retrieve()
                    .body(ContenedorDetalleDTO.class);
            
        } catch (Exception e) {
            log.error("Error al obtener contenedor para solicitud {}", solicitudId, e);
            throw new RuntimeException("Error comunicándose con seguimiento-service: " + e.getMessage());
        }
    }

    /**
     * Actualiza el costo final y tiempo real de una solicitud
     */
    public void actualizarCostosYTiempo(Long solicitudId, BigDecimal costoFinal, Integer tiempoRealMinutos) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(seguimientoServiceUrl)
                    .build();
            
            Map<String, Object> request = Map.of(
                "costoFinal", costoFinal,
                "tiempoRealMinutos", tiempoRealMinutos
            );
            
            restClient.patch()
                    .uri("/solicitudes/{id}/costos", solicitudId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Actualizado costo final {} y tiempo {} min para solicitud {}", 
                    costoFinal, tiempoRealMinutos, solicitudId);
            
        } catch (Exception e) {
            log.error("Error al actualizar costos para solicitud {}", solicitudId, e);
            // No lanzamos excepción para no romper el flujo
        }
    }

    /**
     * Actualiza el costo estimado y tiempo estimado de una solicitud (cuando se asigna una ruta)
     */
    public void actualizarCostosYTiempoEstimados(Long solicitudId, BigDecimal costoEstimado, Integer tiempoEstimadoMinutos) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(seguimientoServiceUrl)
                    .build();
            
            Map<String, Object> request = Map.of(
                "costoEstimado", costoEstimado,
                "tiempoEstimadoMin", tiempoEstimadoMinutos
            );
            
            restClient.patch()
                    .uri("/solicitudes/{id}/costos-estimados", solicitudId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Actualizado costo estimado {} y tiempo estimado {} min para solicitud {}", 
                    costoEstimado, tiempoEstimadoMinutos, solicitudId);
            
        } catch (Exception e) {
            log.error("Error al actualizar costos estimados para solicitud {}", solicitudId, e);
            // No lanzamos excepción para no romper el flujo
        }
    }

    /**
     * Obtiene una solicitud por su rutaId
     */
    public SolicitudResponseDTO obtenerSolicitudPorRutaId(Long rutaId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(seguimientoServiceUrl)
                    .build();
            
            return restClient.get()
                    .uri("/solicitudes/ruta/{rutaId}", rutaId)
                    .retrieve()
                    .body(SolicitudResponseDTO.class);
            
        } catch (Exception e) {
            log.error("Error al obtener solicitud por rutaId {}", rutaId, e);
            return null;
        }
    }

    /**
     * Asigna una ruta a una solicitud (actualiza solicitud.rutaId)
     */
    public void asignarRutaASolicitud(Long solicitudId, Long rutaId) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(seguimientoServiceUrl)
                    .build();
            
            Map<String, Object> request = Map.of("rutaId", rutaId);
            
            restClient.patch()
                    .uri("/solicitudes/{id}/ruta", solicitudId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Solicitud {} vinculada con ruta {}", solicitudId, rutaId);
            
        } catch (Exception e) {
            log.error("Error al asignar ruta {} a solicitud {}", rutaId, solicitudId, e);
            throw new RuntimeException("Error al vincular ruta con solicitud: " + e.getMessage());
        }
    }

    /**
     * Actualiza el estado de una solicitud
     */
    public void actualizarEstadoSolicitud(Long solicitudId, String nuevoEstado) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(seguimientoServiceUrl)
                    .build();
            
            Map<String, String> request = Map.of("estado", nuevoEstado);
            
            restClient.patch()
                    .uri("/solicitudes/{id}/estado", solicitudId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Actualizado estado de solicitud {} a {}", solicitudId, nuevoEstado);
            
        } catch (Exception e) {
            log.error("Error al actualizar estado de solicitud {}", solicitudId, e);
            throw new RuntimeException("Error al actualizar estado de solicitud: " + e.getMessage());
        }
    }

    // DTOs internos para respuestas
    @lombok.Data
    public static class SolicitudResponseDTO {
        private Long id;
        private String numero;
        private Long contenedorId;
        private Long clienteId;
        private String estado;
    }

    @lombok.Data
    public static class ContenedorDetalleDTO {
        private Long id;
        private String identificacion;
        private BigDecimal pesoKg;
        private BigDecimal volumenM3;
        private BigDecimal largoM;
        private BigDecimal anchoM;
        private BigDecimal altoM;
    }
}
