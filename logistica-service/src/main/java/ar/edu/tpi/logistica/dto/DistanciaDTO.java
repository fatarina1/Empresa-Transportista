package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de Google Maps / geoapi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaDTO {
    private String origen;
    private String destino;
    private Double distanciaKm;
    private Integer duracionMinutos;
    private String polilinea; // Encoded polyline para mostrar en mapa
}
