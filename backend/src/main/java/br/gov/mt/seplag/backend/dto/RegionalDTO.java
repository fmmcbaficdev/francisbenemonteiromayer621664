package br.gov.mt.seplag.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de Regional
 */
public record RegionalDTO(
        Long id,
        Integer codigoExterno,
        String nome,
        Boolean ativa,
        LocalDateTime ultimaSincronizacao
) {
    /**
     * Factory para regionais ativas
     */
    public static RegionalDTO ativa(Integer codigoExterno, String nome) {
        return new RegionalDTO(null, codigoExterno, nome, true, LocalDateTime.now());
    }

    /**
     * Verifica se a regional está ativa
     */
    public boolean isAtiva() {
        return Boolean.TRUE.equals(ativa);
    }
}