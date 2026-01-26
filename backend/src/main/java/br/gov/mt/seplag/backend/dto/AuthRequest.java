package br.gov.mt.seplag.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de autenticação (login)
 */
public record AuthRequest(
        @NotBlank(message = "Username é obrigatório")
        String username,

        @NotBlank(message = "Password é obrigatório")
        String password
) {}
