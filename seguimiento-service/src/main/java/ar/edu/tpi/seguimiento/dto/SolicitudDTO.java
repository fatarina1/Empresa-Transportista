package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDTO {
    private Long id;
    private String numero;
    private Long clienteId;
    private Long contenedorId;
    
    // IDs de ubicaciones (mantener para compatibilidad)
    private Long origenId;
    private Long destinoId;
    
    // Objetos completos de ubicación (para evitar llamadas adicionales)
    private UbicacionDTO origen;
    private UbicacionDTO destino;
    
    private Long rutaId; // ID de la ruta asignada en logística-service
    private String estado; // BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA
    private BigDecimal costoEstimado;
    private Integer tiempoEstimadoMin;
    private BigDecimal costoFinal;
    private Integer tiempoRealMin;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEntrega;
}
