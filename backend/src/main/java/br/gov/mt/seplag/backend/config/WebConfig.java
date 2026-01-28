package br.gov.mt.seplag.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração Web MVC
 * Registra interceptors (Rate Limiting, CORS, etc.)
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/v1/**") // ✨ Aplicar apenas nas APIs versionadas
                .excludePathPatterns(
                        "/v1/auth/**",       // Autenticação
                        "/actuator/**",      // Health checks
                        "/swagger-ui/**",    // Swagger UI
                        "/v3/api-docs/**",   // OpenAPI docs
                        "/ws/**"             // WebSocket (se tiver)
                )
                .order(1); // ✨ Ordem de execução (menor = primeiro)
    }
}