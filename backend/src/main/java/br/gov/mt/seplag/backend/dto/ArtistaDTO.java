package br.gov.mt.seplag.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de Artista
 * Usado nas requisições e respostas da API
 *
 * CAMPOS DE AUDITORIA:
 * - createdAt/createdBy: Quando/quem criou o registro
 * - updatedAt/updatedBy: Quando/quem modificou por último
 * - version: Controle de concorrência (Optimistic Locking)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistaDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;

    private String biografia;

    private Integer totalAlbuns; // Calculado

    // ═══════════════════════════════════════════════════════════
    // AUDITORIA - Retornados na resposta (não enviados na request)
    // ═══════════════════════════════════════════════════════════

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    /**
     * Version para Optimistic Locking.
     *
     * IMPORTANTE: Ao atualizar um registro, o frontend DEVE enviar
     * a mesma version que recebeu. Se outro usuário modificou o registro
     * no intervalo, a API retornará 409 Conflict.
     *
     * FLUXO:
     * 1. GET /artistas/1 → { version: 5 }
     * 2. PUT /artistas/1 { version: 5, nome: "Novo" } → Sucesso se version ainda é 5
     * 3. Se version mudou para 6 → 409 Conflict
     */
    private Integer version;
}
