package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostoRealDTO {
    private BigDecimal costoTotal;
    private BigDecimal costoPorKm;
    private BigDecimal costoCombustible;
    private BigDecimal costoEstadias;
    private BigDecimal cargosGestion;
    private Integer tiempoRealMinutos;
    private List<DetalleTramoDTO> detallesTramos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleTramoDTO {
        private Long tramoId;
        private String origen;
        private String destino;
        private BigDecimal distanciaKm;
        private BigDecimal costoKm;
        private BigDecimal costoCombustible;
        private BigDecimal costoEstadia;
        private Integer tiempoMinutos;
    }
}
