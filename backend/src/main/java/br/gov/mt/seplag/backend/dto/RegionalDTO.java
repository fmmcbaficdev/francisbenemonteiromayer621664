package br.gov.mt.seplag.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de Regional
 *
 * SINCRONIZAÇÃO COM API EXTERNA:
 * - codigoExterno: ID da regional na API externa
 * - externalHash: MD5 do nome para detectar mudanças (algoritmo O(n))
 * - ativa: false quando regional foi removida da API externa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionalDTO {

    private Long id;
    private Integer codigoExterno;
    private String nome;
    private Boolean ativa;

    /**
     * Hash MD5 do nome para detectar alterações.
     * Usado no algoritmo de sincronização O(n).
     */
    private String externalHash;

    private LocalDateTime ultimaSincronizacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
