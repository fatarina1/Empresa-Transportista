package ar.edu.tpi.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para crear una ruta completa con sus tramos
 * a partir de una ruta tentativa seleccionada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearRutaConTramosRequest {
    private Long solicitudId; // ID de la solicitud en seguimiento-service
    private RutaTentativaDTO rutaTentativa;
    private String observaciones;
}
