package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorDTO {
    private Long id;
    private String identificacion;
    private BigDecimal pesoKg;
    private BigDecimal volumenM3;
    private BigDecimal largoM;
    private BigDecimal anchoM;
    private BigDecimal altoM;
    private String descripcion;
    private Long clienteId;
    private LocalDateTime createdAt;
}
