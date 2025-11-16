package utnfc.isi.back.spring.geoapi.exceptions;

public class ServicioExternoException extends RuntimeException {
    private final String servicio;

    public ServicioExternoException(String servicio, String mensaje) {
        super(String.format("Error en servicio externo '%s': %s", servicio, mensaje));
        this.servicio = servicio;
    }

    public String getServicio() {
        return servicio;
    }
}
