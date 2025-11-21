package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutasAlternativasDTO {
    private List<RutaTentativaDTO> alternativas;
    private Integer cantidadAlternativas;
    private String observaciones;
}
