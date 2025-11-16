package ar.edu.tpi.seguimiento.config;

import ar.edu.tpi.seguimiento.dto.ErrorResponse;
import ar.edu.tpi.seguimiento.exceptions.DatosInvalidosException;
import ar.edu.tpi.seguimiento.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.seguimiento.exceptions.ReglaNegocioException;
import ar.edu.tpi.seguimiento.exceptions.ServicioExternoException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el servicio de seguimiento.
 * Centraliza el manejo de errores y proporciona respuestas HTTP consistentes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("[CID={}] Recurso no encontrado: {}", correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> handleDatosInvalidos(
            DatosInvalidosException ex, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("[CID={}] Datos inválidos: {}", correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Datos inválidos",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("[CID={}] Errores de validación: {}", correlationId, errors);
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación",
                errors.toString(),
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(
            ReglaNegocioException ex, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.warn("[CID={}] Violación de regla de negocio: {}", correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Regla de negocio violada",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> handleServicioExterno(
            ServicioExternoException ex, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[CID={}] Error en servicio externo: {}", correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_GATEWAY.value(),
                "Error en servicio externo",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[CID={}] Error inesperado: {}", correlationId, ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ha ocurrido un error inesperado. Por favor, contacte al administrador.",
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
