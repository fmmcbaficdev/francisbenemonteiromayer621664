package br.gov.mt.seplag.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * CONFIGURAÇÃO: Spring Data JPA Auditing
 *
 * Habilita auditoria automática para entidades JPA.
 * Preenche automaticamente os campos:
 * - @CreatedBy → created_by (username de quem criou)
 * - @LastModifiedBy → updated_by (username de quem modificou)
 * - @CreatedDate → created_at (timestamp de criação)
 * - @LastModifiedDate → updated_at (timestamp de modificação)
 *
 * COMO FUNCIONA:
 * 1. Entidade usa @EntityListeners(AuditingEntityListener.class)
 * 2. Campos anotados com @CreatedBy/@LastModifiedBy
 * 3. AuditorAware obtém username do SecurityContext
 * 4. JPA preenche automaticamente antes de persistir/atualizar
 *
 * EXEMPLO:
 * <pre>
 * @CreatedBy
 * @Column(name = "created_by")
 * private String createdBy;  // → Preenchido com "admin" automaticamente
 * </pre>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /**
     * Bean que fornece o username do usuário atual para auditoria.
     *
     * FLUXO:
     * 1. JPA vai persistir/atualizar entidade
     * 2. AuditingEntityListener intercepta
     * 3. Chama AuditorAware.getCurrentAuditor()
     * 4. Obtém username do SecurityContext
     * 5. Preenche campos @CreatedBy/@LastModifiedBy
     *
     * @return AuditorAware<String> que retorna o username atual
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            // Obter autenticação do SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Se não autenticado ou anônimo, usar "system"
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            // Se for "anonymousUser", usar "system"
            String username = authentication.getName();
            if ("anonymousUser".equals(username)) {
                return Optional.of("system");
            }

            return Optional.of(username);
        };
    }
}
