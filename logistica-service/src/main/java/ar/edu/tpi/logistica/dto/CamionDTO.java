package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionDTO {
    private Long id;
    private String dominio;
    private Double capacidadPesoKg;
    private Double capacidadVolumenM3;
    private Double costoKmBase;
    private Double consumoPromedioLitrosKm;
    private Boolean disponible;
    private Long transportistaId;
    private LocalDateTime createdAt;
}
