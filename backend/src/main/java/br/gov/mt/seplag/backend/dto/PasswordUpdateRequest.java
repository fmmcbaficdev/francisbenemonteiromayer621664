package br.gov.mt.seplag.backend.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(
        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String novaSenha
) {}
