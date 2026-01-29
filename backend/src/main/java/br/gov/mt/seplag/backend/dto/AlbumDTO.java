package br.gov.mt.seplag.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferência de dados de Álbum
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
public class AlbumDTO {

    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String titulo;

    @Min(value = 1900, message = "Ano de lançamento deve ser posterior a 1900")
    @Max(value = 2100, message = "Ano de lançamento deve ser anterior a 2100")
    private Integer anoLancamento;

    private String descricao;

    @NotEmpty(message = "Álbum deve ter pelo menos um artista")
    private List<Long> artistasIds;

    private List<ArtistaDTO> artistas; // Resposta

    private List<String> imagensUrls; // URLs presigned do MinIO

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
     * IMPORTANTE: Ao atualizar um álbum, o frontend DEVE enviar
     * a mesma version que recebeu. Se outro usuário modificou o registro
     * no intervalo, a API retornará 409 Conflict.
     */
    private Integer version;

    /**
     * Retorna a primeira imagem do álbum (para exibição em grid)
     */
    public String getPrimeiraImagemUrl() {
        return (imagensUrls != null && !imagensUrls.isEmpty()) ? imagensUrls.get(0) : null;
    }
}
