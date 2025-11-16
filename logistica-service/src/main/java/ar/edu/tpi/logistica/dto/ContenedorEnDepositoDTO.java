package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorEnDepositoDTO {
    private Long contenedorId;
    private String identificacion;
    private Long solicitudId;
    private String solicitudNumero;
    private LocalDateTime fechaLlegada;
    private Long diasEnDeposito;
    private Double pesoKg;
    private Double volumenM3;
}
