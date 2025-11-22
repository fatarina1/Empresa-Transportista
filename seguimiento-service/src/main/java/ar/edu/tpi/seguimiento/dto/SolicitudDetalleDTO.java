package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO con información completa de la solicitud incluyendo datos de otros servicios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDetalleDTO {
    private Long id;
    private String numero;
    private String estado;
    
    // Datos del cliente
    private ClienteDTO cliente;
    
    // Datos del contenedor
    private ContenedorDTO contenedor;
    
    // Ubicaciones (vienen del servicio de logística)
    private UbicacionDTO origen;
    private UbicacionDTO destino;
    
    // Costos y tiempos
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoMin;
    private BigDecimal costoFinal;
    private Integer tiempoRealMin;
    
    // Tracking en tiempo real (si tiene ruta asignada)
    private TrackingInfoDTO tracking;
}
