package br.gov.mt.seplag.backend.dto;

import java.time.LocalDateTime;

public record WebSocketNotification(
        TipoNotificacao tipo,
        String mensagem,
        Object dados,
        LocalDateTime timestamp
) {
    public enum TipoNotificacao {
        ALBUM_CRIADO,
        ALBUM_ATUALIZADO,
        ALBUM_REMOVIDO,
        ARTISTA_CRIADO,
        ARTISTA_ATUALIZADO,
        ARTISTA_REMOVIDO
    }

    /**
     * Construtor com timestamp automático
     */
    public WebSocketNotification(TipoNotificacao tipo, String mensagem, Object dados) {
        this(tipo, mensagem, dados, LocalDateTime.now());
    }
}