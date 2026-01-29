package br.gov.mt.seplag.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Validação de configurações de segurança no startup
 * Previne uso de secrets padrão em ambiente de produção
 */
@Configuration
@Slf4j
public class SecurityValidationConfig {

    private final Environment environment;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public SecurityValidationConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateSecurityConfig() {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (isProduction || Arrays.asList(environment.getActiveProfiles()).isEmpty()) {
            validateJwtSecret();
        }
    }

    private void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new SecurityException(
                "JWT_SECRET não configurado! Defina a variável de ambiente JWT_SECRET."
            );
        }

        if (jwtSecret.equals("CHANGE_ME_IN_PRODUCTION_USE_ENV_VAR")) {
            log.warn("╔══════════════════════════════════════════════════════════════╗");
            log.warn("║  ⚠️  AVISO DE SEGURANÇA                                       ║");
            log.warn("║  JWT_SECRET está usando valor padrão!                         ║");
            log.warn("║  Em produção, defina a variável de ambiente JWT_SECRET        ║");
            log.warn("║  Gerar secret: openssl rand -base64 64                        ║");
            log.warn("╚══════════════════════════════════════════════════════════════╝");
        }

        // Validar tamanho mínimo do secret (256 bits = 32 bytes para HS256)
        if (jwtSecret.length() < 32) {
            log.warn("JWT_SECRET muito curto! Recomendado mínimo de 32 caracteres para segurança.");
        }
    }
}
