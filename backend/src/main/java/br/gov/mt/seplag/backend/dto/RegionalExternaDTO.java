package br.gov.mt.seplag.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados da API externa de regionais
 * API: https://integrador-argus-api.geia.vip/v1/regionais
 *
 * Formato real:
 * {
 *   "id": 9,
 *   "nome": "REGIONAL DE CUIABÁ"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionalExternaDTO {

    /**
     * ID numérico da regional (vem da API)
     */
    private Integer id;

    /**
     * Nome da regional
     */
    private String nome;
}
