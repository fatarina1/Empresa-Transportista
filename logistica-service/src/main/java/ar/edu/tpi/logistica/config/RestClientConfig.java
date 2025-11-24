package ar.edu.tpi.logistica.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private final CorrelationIdInterceptor correlationIdInterceptor;

    public RestClientConfig(CorrelationIdInterceptor correlationIdInterceptor) {
        this.correlationIdInterceptor = correlationIdInterceptor;
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .requestInterceptor(correlationIdInterceptor);
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        // Usar Apache HttpClient para soportar el método PATCH
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(5000); // 5 segundos
        factory.setConnectionRequestTimeout(5000); // 5 segundos
        
        return new BufferingClientHttpRequestFactory(factory);
    }
}
