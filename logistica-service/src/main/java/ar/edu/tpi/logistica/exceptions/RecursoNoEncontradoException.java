package ar.edu.tpi.logistica.exceptions;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en el sistema.
 * Corresponde a errores HTTP 404 NOT FOUND.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    
    public RecursoNoEncontradoException(String recurso, Long id) {
        super(String.format("%s con ID %d no fue encontrado", recurso, id));
    }
    
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
    
    public RecursoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
