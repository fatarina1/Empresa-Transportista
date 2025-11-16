package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaTentativaDTO {
    private List<TramoTentativoDTO> tramos;
    private BigDecimal costoEstimadoTotal;
    private Integer tiempoEstimadoTotalMinutos;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private BigDecimal distanciaTotalKm;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TramoTentativoDTO {
        private Integer orden;
        private String origenNombre;
        private String destinoNombre;
        private BigDecimal distanciaKm;
        private Integer tiempoEstimadoMinutos;
        private BigDecimal costoEstimado;
        private String tipo; // ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO
    }
}
