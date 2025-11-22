package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO con información de tracking en tiempo real de una solicitud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingInfoDTO {
    private Long rutaId;
    private String estadoRuta;
    private UbicacionDTO ubicacionActual;
    private List<TramoDTO> tramos;
}
