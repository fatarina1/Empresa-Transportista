package ar.edu.tpi.logistica.exceptions;

/**
 * Excepción lanzada cuando falla la comunicación con un servicio externo
 * (seguimiento-service, geoapi, etc.).
 * Corresponde a errores HTTP 502 BAD GATEWAY o 503 SERVICE UNAVAILABLE.
 */
public class ServicioExternoException extends RuntimeException {
    
    private final String servicioNombre;
    
    public ServicioExternoException(String servicioNombre, String mensaje) {
        super(String.format("Error al comunicarse con %s: %s", servicioNombre, mensaje));
        this.servicioNombre = servicioNombre;
    }
    
    public ServicioExternoException(String servicioNombre, String mensaje, Throwable causa) {
        super(String.format("Error al comunicarse con %s: %s", servicioNombre, mensaje), causa);
        this.servicioNombre = servicioNombre;
    }
    
    public String getServicioNombre() {
        return servicioNombre;
    }
}
