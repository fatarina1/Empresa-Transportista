package ar.edu.tpi.logistica.config;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Interceptor para propagar el Correlation ID en llamadas REST a otros microservicios.
 * Asegura que el mismo ID de trazabilidad se mantenga a través de toda la cadena de requests.
 */
@Component
public class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                       ClientHttpRequestExecution execution) throws IOException {
        
        // Obtener el Correlation ID del contexto actual
        String correlationId = MDC.get(MDC_KEY);
        
        // Si existe, agregarlo al header del request saliente
        if (correlationId != null && !correlationId.isBlank()) {
            request.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        }
        
        // Continuar con la ejecución del request
        return execution.execute(request, body);
    }
}
