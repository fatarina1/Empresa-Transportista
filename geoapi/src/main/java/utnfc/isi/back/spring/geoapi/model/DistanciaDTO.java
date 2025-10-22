package utnfc.isi.back.spring.geoapi.model;

import lombok.Data;

@Data
public class DistanciaDTO {
    private String origen; 
    private String destino; 
    private double kilometros; 
    private String duracionTexto; 
}