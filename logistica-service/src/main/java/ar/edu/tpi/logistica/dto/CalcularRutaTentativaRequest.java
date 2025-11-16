package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalcularRutaTentativaRequest {
    private Long ubicacionOrigenId;
    private Long ubicacionDestinoId;
    private List<Long> depositosIntermediosIds; // IDs de depósitos en orden
    private Double pesoKg;
    private Double volumenM3;
}
