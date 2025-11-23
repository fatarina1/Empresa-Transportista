package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para calcular rutas alternativas.
 * Los depósitos intermedios se seleccionan automáticamente según optimización geográfica.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalcularRutaTentativaRequest {
    private Long ubicacionOrigenId;
    private Long ubicacionDestinoId;
    private Double pesoKg;
    private Double volumenM3;
}
