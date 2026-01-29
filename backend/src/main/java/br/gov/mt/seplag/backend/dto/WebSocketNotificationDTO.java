package br.gov.mt.seplag.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para notificações WebSocket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketNotificationDTO {

    private String tipo; // NOVO_ALBUM, ALBUM_ATUALIZADO, ALBUM_REMOVIDO
    private String mensagem;
    private Object dados; // Dados do álbum/artista
    private LocalDateTime timestamp;
}