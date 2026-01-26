package br.gov.mt.seplag.backend.dto;

/**
 * DTO para dados da API externa de regionais
 * API: https://integrador-argus-api.geia.vip/v1/regionais
 *
 * Formato esperado:
 * {
 *   "id": 9,
 *   "nome": "REGIONAL DE CUIABÁ"
 * }
 */
public record RegionalExternaDTO(
        Integer id,
        String nome
) {}
