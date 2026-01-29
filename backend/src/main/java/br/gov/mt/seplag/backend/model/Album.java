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
 * ENTIDADE: Album
 *
 * Representa um álbum musical no sistema.
 * Possui relacionamento N:N com Artista (permite colaborações).
 * Possui relacionamento 1:N com ImagemCapa.
 *
 * RECURSOS SÊNIOR IMPLEMENTADOS:
 * - Optimistic Locking (@Version) para controle de concorrência
 * - Auditoria completa (created_by, updated_by)
 * - EntityListener para auditoria automática via Spring Data
 */
@Entity
@Table(name = "albuns", indexes = {
        @Index(name = "idx_album_titulo", columnList = "titulo"),
        @Index(name = "idx_album_ano", columnList = "ano_lancamento")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(name = "ano_lancamento")
    private Integer anoLancamento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "artista_album",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artista_id")
    )
    @Builder.Default
    private Set<Artista> artistas = new HashSet<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ImagemCapa> imagensCapa = new HashSet<>();

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

    @Version
    @Column(name = "version")
    private Integer version;
}
