package ar.edu.tpi.seguimiento.exceptions;

/**
 * Excepción lanzada cuando se viola una regla de negocio.
 * Mapea a HTTP 422 UNPROCESSABLE ENTITY.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
