package br.gov.mt.seplag.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respostas de erro padronizadas
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<String> errors
) {
    /**
     * Construtor para erros simples (sem lista de erros)
     */
    public ErrorResponse(Integer status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    /**
     * Construtor para erros de validação (com lista de erros)
     */
    public ErrorResponse(Integer status, String error, String message, String path, List<String> errors) {
        this(LocalDateTime.now(), status, error, message, path, errors);
    }
}