package ar.edu.tpi.logistica.dto;

import java.time.LocalDateTime;

/**
 * Estructura estándar para respuestas de error en la API.
 * Proporciona información clara y consistente sobre fallos.
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    String correlationId
) {
    public static ErrorResponse of(int status, String error, String message, String path, String correlationId) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, correlationId);
    }
}
