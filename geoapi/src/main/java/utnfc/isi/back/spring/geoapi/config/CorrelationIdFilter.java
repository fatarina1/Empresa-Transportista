package utnfc.isi.back.spring.geoapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            long startTime = System.currentTimeMillis();
            log.debug("[CID={}] Request recibido: {} {}", correlationId, request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[CID={}] Request completado: {} {} - Status: {} - Duración: {}ms",
                    correlationId, request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
