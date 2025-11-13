package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta del servicio GeoAPI.
 * Coincide exactamente con la estructura del documento oficial.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaDTO {
    private String origen;
    private String destino;
    private double kilometros;          // Nombre exacto del documento
    private String duracionTexto;       // String según documento (ej: "2 horas 15 mins")
}
