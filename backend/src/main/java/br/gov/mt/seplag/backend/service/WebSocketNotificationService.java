package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.AlbumDTO;
import br.gov.mt.seplag.backend.dto.WebSocketNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service para enviar notificações WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private static final String TOPIC_ALBUNS = "/topic/albuns";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Enviar notificação de novo álbum
     */
    public void notificarNovoAlbum(AlbumDTO album) {
        log.info("Enviando notificação WebSocket: ALBUM_CRIADO - {}", album.titulo());

        var notification = new WebSocketNotification(
                WebSocketNotification.TipoNotificacao.ALBUM_CRIADO,
                "Novo álbum cadastrado: " + album.titulo(),
                album
        );

        messagingTemplate.convertAndSend(TOPIC_ALBUNS, notification);
    }

    /**
     * Enviar notificação de álbum atualizado
     */
    public void notificarAlbumAtualizado(AlbumDTO album) {
        log.info("Enviando notificação WebSocket: ALBUM_ATUALIZADO - {}", album.titulo());

        var notification = new WebSocketNotification(
                WebSocketNotification.TipoNotificacao.ALBUM_ATUALIZADO,
                "Álbum atualizado: " + album.titulo(),
                album
        );

        messagingTemplate.convertAndSend(TOPIC_ALBUNS, notification);
    }

    /**
     * Enviar notificação de álbum removido
     */
    public void notificarAlbumRemovido(Long albumId) {
        log.info("Enviando notificação WebSocket: ALBUM_REMOVIDO - ID {}", albumId);

        var notification = new WebSocketNotification(
                WebSocketNotification.TipoNotificacao.ALBUM_REMOVIDO,
                "Álbum removido",
                albumId
        );

        messagingTemplate.convertAndSend(TOPIC_ALBUNS, notification);
    }
}
