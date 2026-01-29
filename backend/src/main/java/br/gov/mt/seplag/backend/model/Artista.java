package br.gov.mt.seplag.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ENTIDADE: Artista
 *
 * Representa um artista musical cadastrado no sistema.
 * Possui relacionamento N:N com Album (permite colaborações).
 *
 * RECURSOS SÊNIOR IMPLEMENTADOS:
 * - Optimistic Locking (@Version) para controle de concorrência
 * - Auditoria completa (created_by, updated_by)
 * - EntityListener para auditoria automática via Spring Data
 */
@Entity
@Table(name = "artistas", indexes = {
        @Index(name = "idx_artista_nome", columnList = "nome")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Artista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String biografia;

    @ManyToMany(mappedBy = "artistas", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Album> albuns = new HashSet<>();

    // ═══════════════════════════════════════════════════════════
    // AUDITORIA - Spring Data JPA Auditing
    // ═══════════════════════════════════════════════════════════

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ═══════════════════════════════════════════════════════════
    // OPTIMISTIC LOCKING - Controle de Concorrência
    // ═══════════════════════════════════════════════════════════
    // Se dois usuários editarem simultaneamente:
    // - Usuário A: GET → version: 5
    // - Usuário B: GET → version: 5
    // - Usuário A: PUT (version: 5) → Sucesso → version: 6
    // - Usuário B: PUT (version: 5) → OptimisticLockException (409 Conflict)

    @Version
    @Column(name = "version")
    private Integer version;
}
