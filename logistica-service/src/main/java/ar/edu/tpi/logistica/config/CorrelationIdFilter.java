package ar.edu.tpi.logistica.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que intercepta cada request HTTP para asignar o propagar un Correlation ID.
 * Este ID permite trazar una solicitud a través de múltiples microservicios.
 * 
 * El Correlation ID se almacena en MDC (Mapped Diagnostic Context) para que 
 * esté disponible automáticamente en todos los logs generados durante el request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Obtener Correlation ID del header o generar uno nuevo
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generado nuevo Correlation ID: {}", correlationId);
        } else {
            log.debug("Correlation ID recibido: {}", correlationId);
        }
        
        // Almacenar en MDC para que esté disponible en todos los logs
        MDC.put(MDC_KEY, correlationId);
        
        // Agregar a la respuesta HTTP para que el cliente pueda rastrearlo
        response.addHeader(CORRELATION_ID_HEADER, correlationId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
        } finally {
            // Calcular tiempo de procesamiento
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("[CID={}] {} {} - Status: {} - Duración: {}ms", 
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration
            );
            
            // Limpiar MDC al finalizar el request
            MDC.remove(MDC_KEY);
        }
    }
}
