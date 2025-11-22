package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para representar un cambio de estado en el historial de una solicitud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoDTO {
    private Long id;
    private Long solicitudId;
    private String estado;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private String detalle;
}
