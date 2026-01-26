package br.gov.mt.seplag.backend.dto;

/**
 * DTO para resposta de autenticação
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
    /**
     * Construtor padrão com tokenType "Bearer"
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
