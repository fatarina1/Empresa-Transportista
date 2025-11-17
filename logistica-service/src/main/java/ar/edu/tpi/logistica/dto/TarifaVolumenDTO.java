package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaVolumenDTO {
    private Long id;
    private BigDecimal volumenMinM3;
    private BigDecimal volumenMaxM3;
    private BigDecimal costoKmBase;
}
