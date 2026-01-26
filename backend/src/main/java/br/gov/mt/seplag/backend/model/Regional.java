package br.gov.mt.seplag.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ENTIDADE: Regional
 *
 * Representa uma regional administrativa do MT.
 * Sincronizada com API externa.
 */
@Entity
@Table(name = "regionais", indexes = {
        @Index(name = "idx_regional_codigo_externo", columnList = "codigo_externo", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Regional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código externo da regional (ID da API externa)
     */
    @Column(name = "codigo_externo", nullable = false, unique = true)
    private Integer codigoExterno;

    @Column(nullable = false, length = 200)
    private String nome;

    /**
     * Indica se regional está ativa
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativa = true;

    /**
     * Data da última sincronização com API externa
     */
    @Column(name = "ultima_sincronizacao")
    private LocalDateTime ultimaSincronizacao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
