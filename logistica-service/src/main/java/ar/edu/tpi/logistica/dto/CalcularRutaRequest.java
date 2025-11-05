package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para solicitar cálculo de ruta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalcularRutaRequest {
    private Long solicitudId;
    private Long origenId;
    private Long destinoId;
    private BigDecimal pesoKg;
    private BigDecimal volumenM3;
    private List<Long> depositosIntermedios; // IDs de depósitos por los que debe pasar
}
