package br.gov.mt.seplag.backend.service;

import br.gov.mt.seplag.backend.dto.WebSocketNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service para enviar notificações WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Enviar notificação de novo álbum
     */
    public void notificarNovoAlbum(Object albumDTO) {
        log.info("Enviando notificação WebSocket: NOVO_ALBUM");

        WebSocketNotificationDTO notification = WebSocketNotificationDTO.builder()
                .tipo("NOVO_ALBUM")
                .mensagem("Novo álbum cadastrado!")
                .dados(albumDTO)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/albuns", notification);
    }

    /**
     * Enviar notificação de álbum atualizado
     */
    public void notificarAlbumAtualizado(Object albumDTO) {
        log.info("Enviando notificação WebSocket: ALBUM_ATUALIZADO");

        WebSocketNotificationDTO notification = WebSocketNotificationDTO.builder()
                .tipo("ALBUM_ATUALIZADO")
                .mensagem("Álbum atualizado!")
                .dados(albumDTO)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/albuns", notification);
    }

    /**
     * Enviar notificação de álbum removido
     */
    public void notificarAlbumRemovido(Long albumId) {
        log.info("Enviando notificação WebSocket: ALBUM_REMOVIDO");

        WebSocketNotificationDTO notification = WebSocketNotificationDTO.builder()
                .tipo("ALBUM_REMOVIDO")
                .mensagem("Álbum removido!")
                .dados(albumId)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/albuns", notification);
    }
}
