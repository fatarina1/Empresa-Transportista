package ar.edu.tpi.seguimiento.exceptions;

/**
 * Excepción lanzada cuando los datos proporcionados no son válidos.
 * Mapea a HTTP 400 BAD REQUEST.
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
