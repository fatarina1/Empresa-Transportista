package ar.edu.tpi.logistica.config;

import ar.edu.tpi.logistica.dto.ErrorResponse;
import ar.edu.tpi.logistica.exceptions.DatosInvalidosException;
import ar.edu.tpi.logistica.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.logistica.exceptions.ReglaNegocioException;
import ar.edu.tpi.logistica.exceptions.ServicioExternoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Centraliza el manejo de errores y garantiza respuestas HTTP consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones cuando un recurso no es encontrado (404).
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex, 
            HttpServletRequest request) {
        
        String correlationId = MDC.get("correlationId");
        log.warn("[CID={}] Recurso no encontrado: {}", correlationId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "Recurso no encontrado",
            ex.getMessage(),
            request.getRequestURI(),
            correlationId
        );
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de datos inválidos o validación (400).
     */
    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> handleDatosInvalidos(
            DatosInvalidosException ex, 
            HttpServletRequest request) {
        
        String correlationId = MDC.get("correlationId");
        log.warn("[CID={}] Datos inválidos: {}", correlationId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Datos inválidos",
            ex.getMessage(),
            request.getRequestURI(),
            correlationId
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de validación de Bean Validation (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        String correlationId = MDC.get("correlationId");
        String mensajes = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce("", (a, b) -> a + "; " + b);
        
        log.warn("[CID={}] Errores de validación: {}", correlationId, mensajes);
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "Errores de validación",
            mensajes,
            request.getRequestURI(),
            correlationId
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de reglas de negocio (422).
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(
            ReglaNegocioException ex, 
            HttpServletRequest request) {
        
        String correlationId = MDC.get("correlationId");
        log.warn("[CID={}] Violación de regla de negocio: {}", correlationId, ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Regla de negocio violada",
            ex.getMessage(),
            request.getRequestURI(),
            correlationId
        );
        
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Maneja excepciones de servicios externos (502).
     */
    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> handleServicioExterno(
            ServicioExternoException ex, 
            HttpServletRequest request) {
        
        String correlationId = MDC.get("correlationId");
        log.error("[CID={}] Error en servicio externo {}: {}", 
            correlationId, ex.getServicioNombre(), ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_GATEWAY.value(),
            "Error de servicio externo",
            ex.getMessage(),
            request.getRequestURI(),
            correlationId
        );
        
        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Maneja excepciones no controladas (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        String correlationId = MDC.get("correlationId");
        log.error("[CID={}] Error inesperado en {}: {}", 
            correlationId, request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno del servidor",
            "Ha ocurrido un error inesperado. Por favor contacte al administrador.",
            request.getRequestURI(),
            correlationId
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
