package br.gov.mt.seplag.backend.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO para transferência de dados de Álbum
 */
public record AlbumDTO(
        Long id,

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        String titulo,

        @Min(value = 1900, message = "Ano de lançamento deve ser posterior a 1900")
        @Max(value = 2100, message = "Ano de lançamento deve ser anterior a 2100")
        Integer anoLancamento,

        String descricao,

        @NotEmpty(message = "Álbum deve ter pelo menos um artista")
        List<Long> artistasIds,

        List<ArtistaDTO> artistas,

        List<String> imagensUrls
) {
    /**
     * Construtor para criação de álbum (request)
     */
    public AlbumDTO(String titulo, Integer anoLancamento, String descricao, List<Long> artistasIds) {
        this(null, titulo, anoLancamento, descricao, artistasIds, null, null);
    }
}