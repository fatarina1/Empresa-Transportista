package ar.edu.tpi.logistica.exceptions;

/**
 * Excepción lanzada cuando los datos proporcionados son inválidos o no cumplen
 * con las reglas de validación del negocio.
 * Corresponde a errores HTTP 400 BAD REQUEST.
 */
public class DatosInvalidosException extends RuntimeException {
    
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
    
    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
