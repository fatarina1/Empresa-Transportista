package ar.edu.tpi.seguimiento.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para gestionar el Correlation ID en cada request HTTP.
 * Permite rastrear requests a través de múltiples servicios.
 */
@Slf4j
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Obtener o generar Correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Guardar en MDC para que esté disponible en todos los logs
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // Agregar a response headers
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Log del request
            long startTime = System.currentTimeMillis();
            log.debug("[CID={}] Request recibido: {} {}", correlationId, request.getMethod(), request.getRequestURI());

            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);

            // Log de finalización
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[CID={}] Request completado: {} {} - Status: {} - Duración: {}ms",
                    correlationId, request.getMethod(), request.getRequestURI(), 
                    response.getStatus(), duration);

        } finally {
            // Limpiar MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
