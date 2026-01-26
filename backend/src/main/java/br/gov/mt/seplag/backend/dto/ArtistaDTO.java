package br.gov.mt.seplag.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferência de dados de Artista
 */
public record ArtistaDTO(
        Long id,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
        String nome,

        String biografia,

        Integer totalAlbuns
) {
    /**
     * Construtor para criação sem ID (novo artista)
     */
    public ArtistaDTO(String nome, String biografia) {
        this(null, nome, biografia, null);
    }
}