package utnfc.isi.back.spring.geoapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadaDTO {
    private Double latitud;
    private Double longitud;
    private String nombre;
}
