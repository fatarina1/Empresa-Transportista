package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una ubicación (viene del servicio de logística)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private String tipo; // DEPOSITO, CLIENTE
}
