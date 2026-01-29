package br.gov.mt.seplag.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * ENTIDADE: Usuario
 *
 * Representa um usuário do sistema com autenticação JWT.
 * Implementa UserDetails do Spring Security.
 *
 * RECURSOS:
 * - Optimistic Locking (@Version)
 * - Soft delete (campo ativo)
 * - Auditoria (created_at, updated_at)
 */
@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuario_username", columnList = "username")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 200, unique = true)
    private String email;

    /**
     * Flag para soft delete / desativação de usuário
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    // ═══════════════════════════════════════════════════════════
    // AUDITORIA
    // ═══════════════════════════════════════════════════════════

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // OPTIMISTIC LOCKING
    // ═══════════════════════════════════════════════════════════

    @Version
    @Column(name = "version")
    private Integer version;

    // ═══════════════════════════════════════════════════════════
    // SPRING SECURITY - UserDetails
    // ═══════════════════════════════════════════════════════════

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo != null && ativo;
    }
}
