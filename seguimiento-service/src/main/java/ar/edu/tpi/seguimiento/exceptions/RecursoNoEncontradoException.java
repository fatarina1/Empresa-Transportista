package ar.edu.tpi.seguimiento.exceptions;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la base de datos.
 * Mapea a HTTP 404 NOT FOUND.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    
    private final String recurso;
    private final Object id;

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(String.format("%s con ID %s no fue encontrado", recurso, id));
        this.recurso = recurso;
        this.id = id;
    }

    public String getRecurso() {
        return recurso;
    }

    public Object getId() {
        return id;
    }
}
