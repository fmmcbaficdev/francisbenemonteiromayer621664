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
 * Sincronizada com API externa: https://integrador-argus-api.geia.vip/v1/regionais
 *
 * ALGORITMO DE SINCRONIZAÇÃO O(n):
 * - Usa HashMap para lookup em O(1)
 * - external_hash (MD5) para detectar mudanças sem comparar campo a campo
 * - Complexidade total: O(n) onde n = número de regionais
 *
 * @see br.gov.mt.seplag.backend.service.RegionalSyncService
 */
@Entity
@Table(name = "regionais", indexes = {
        @Index(name = "idx_regional_codigo_externo", columnList = "codigo_externo", unique = true),
        @Index(name = "idx_regional_ativa", columnList = "ativa"),
        @Index(name = "idx_regional_external_hash", columnList = "external_hash")
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
     * Indica se regional está ativa (soft delete)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativa = true;

    /**
     * Hash MD5 do nome para detectar alterações.
     *
     * ALGORITMO O(n):
     * 1. Calcular hash dos dados externos: hashExterno = MD5(nome)
     * 2. Comparar com hash local: O(1)
     * 3. Se diferente → houve mudança → atualizar
     *
     * VANTAGEM: Não precisa comparar todos os campos individualmente
     */
    @Column(name = "external_hash", length = 64)
    private String externalHash;

    /**
     * Data da última sincronização com API externa
     */
    @Column(name = "ultima_sincronizacao")
    private LocalDateTime ultimaSincronizacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
