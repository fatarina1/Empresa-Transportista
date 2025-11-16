package ar.edu.tpi.seguimiento.exceptions;

/**
 * Excepción lanzada cuando falla la comunicación con un servicio externo.
 * Mapea a HTTP 502 BAD GATEWAY o 503 SERVICE UNAVAILABLE.
 */
public class ServicioExternoException extends RuntimeException {

    private final String servicio;

    public ServicioExternoException(String servicio, String mensaje) {
        super(String.format("Error en servicio externo '%s': %s", servicio, mensaje));
        this.servicio = servicio;
    }

    public ServicioExternoException(String servicio, String mensaje, Throwable causa) {
        super(String.format("Error en servicio externo '%s': %s", servicio, mensaje), causa);
        this.servicio = servicio;
    }

    public String getServicio() {
        return servicio;
    }
}
