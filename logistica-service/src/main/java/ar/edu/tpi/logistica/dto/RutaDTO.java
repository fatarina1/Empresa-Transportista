package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaDTO {
    private Long id;
    private Long solicitudExternaId; // ID de la solicitud en seguimiento-service
    private String estado; // PROPUESTA, ASIGNADA, EN_EJECUCION, FINALIZADA, CANCELADA
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private String observaciones;
    private LocalDateTime createdAt;
    private List<TramoDTO> tramos; // Lista de tramos (opcional, para detalle completo)
}
