package utnfc.isi.back.spring.geoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalcularRutaRequest {
    private CoordenadaDTO origen;
    private CoordenadaDTO destino;
}
