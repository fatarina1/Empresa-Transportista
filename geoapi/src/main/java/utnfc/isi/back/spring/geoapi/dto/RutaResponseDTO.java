package utnfc.isi.back.spring.geoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaResponseDTO {
    private String origen;
    private String destino;
    private Double distanciaKm;
    private Integer duracionMinutos;
    private String polilinea; // Encoded polyline para el mapa
}
