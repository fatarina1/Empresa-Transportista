package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoDTO {
    private Long id;
    private Long rutaId;
    private Integer orden;
    
    // IDs de depósitos (pueden ser null si es origen/destino de cliente)
    private Long origenDepositoId;
    private Long destinoDepositoId;
    
    // Coordenadas GPS
    private Double origenLatitud;
    private Double origenLongitud;
    private Double destinoLatitud;
    private Double destinoLongitud;
    
    // Tipo: ORIGEN_DESTINO, ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO
    private String tipo;
    
    // Estado: ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
    private String estado;
    
    private Double distanciaKm;
    private BigDecimal costoAproximado;
    private BigDecimal costoReal;
    
    // Fechas estimadas
    private LocalDateTime fechaInicioEstimada;
    private LocalDateTime fechaFinEstimada;
    
    // Fechas reales
    private LocalDateTime fechaInicioReal;
    private LocalDateTime fechaFinReal;
    
    private Long camionId;
    private LocalDateTime createdAt;
}
