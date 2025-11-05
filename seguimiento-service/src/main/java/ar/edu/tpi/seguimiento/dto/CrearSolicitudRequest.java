package ar.edu.tpi.seguimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear una nueva solicitud de transporte
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearSolicitudRequest {
    private Long clienteId;
    private Long contenedorId;
    private Long origenId;
    private Long destinoId;
    
    // Datos del cliente (si no existe)
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteDocumento;
    private String clienteDireccion;
    
    // Datos del contenedor (si no existe)
    private String contenedorIdentificacion;
    private BigDecimal contenedorPesoKg;
    private BigDecimal contenedorVolumenM3;
    private BigDecimal contenedorLargoM;
    private BigDecimal contenedorAnchoM;
    private BigDecimal contenedorAltoM;
    private String contenedorDescripcion;
}
