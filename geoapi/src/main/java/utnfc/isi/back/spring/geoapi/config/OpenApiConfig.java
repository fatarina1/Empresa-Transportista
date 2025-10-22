package main.java.utnfc.isi.back.spring.geoapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Geo API",
                version = "v1",
                description = "API de geolocalización y utilidades",
                contact = @Contact(name = "Equipo", email = "soporte@example.com"),
                license = @License(name = "MIT")
        )
)
public class OpenApiConfig {
    // Clase vacía: la metadata se define mediante anotaciones
}
