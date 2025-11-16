package ar.edu.tpi.logistica.exceptions;

/**
 * Excepción lanzada cuando se viola una regla de negocio específica del dominio.
 * Ejemplo: intentar asignar un camión sin capacidad suficiente.
 * Corresponde a errores HTTP 422 UNPROCESSABLE ENTITY o 409 CONFLICT.
 */
public class ReglaNegocioException extends RuntimeException {
    
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
    
    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
