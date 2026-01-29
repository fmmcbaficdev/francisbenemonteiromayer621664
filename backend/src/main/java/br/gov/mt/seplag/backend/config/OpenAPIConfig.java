package br.gov.mt.seplag.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração Swagger/OpenAPI
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SEPLAG Artists & Albums API",
                version = "1.0.0",
                description = "API para gerenciamento de Artistas e Álbuns",
                contact = @Contact(
                        name = "SEPLAG/MT",
                        email = "seplag@mt.gov.br"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor Local"),
                @Server(url = "https://api.seplag.mt.gov.br", description = "Produção")
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenAPIConfig {
}
