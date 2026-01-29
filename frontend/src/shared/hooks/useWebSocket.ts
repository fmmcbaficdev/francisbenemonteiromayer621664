// ==========================================
// USE WEBSOCKET HOOK
// ==========================================

import { useEffect, useCallback } from 'react';
import { webSocketService } from '../../core/services/websocket';
import type { WebSocketNotification } from '../../core/model/types';
import toast from 'react-hot-toast';

// ==========================================
// HOOK
// ==========================================

export function useWebSocket(
  onNotification?: (notification: WebSocketNotification) => void
) {
  // ==========================================
  // DEFAULT NOTIFICATION HANDLER
  // ==========================================
  
  const defaultHandler = useCallback((notification: WebSocketNotification) => {
    switch (notification.tipo) {
      case 'ALBUM_CRIADO':
        toast.success(`🎵 Novo álbum: ${notification.album?.titulo || 'Álbum criado'}`, {
          duration: 4000,
          icon: '🎉',
        });
        break;
        
      case 'ALBUM_ATUALIZADO':
        toast.success(`✏️ Álbum atualizado: ${notification.album?.titulo || 'Álbum'}`, {
          duration: 3000,
          icon: '📝',
        });
        break;
        
      case 'ALBUM_DELETADO':
        toast(`🗑️ Álbum removido`, {
          duration: 3000,
          icon: '⚠️',
        });
        break;
        
      default:
        toast(notification.mensagem || 'Notificação recebida');
    }
  }, []);

  // ==========================================
  // EFFECT - CONNECT AND SUBSCRIBE
  // ==========================================
  
  useEffect(() => {
    // Conectar WebSocket
    webSocketService.connect();
    
    // Inscrever para notificações
    const handler = onNotification || defaultHandler;
    const unsubscribe = webSocketService.subscribe(handler);
    
    // Cleanup
    return () => {
      unsubscribe();
    };
  }, [onNotification, defaultHandler]);

  // ==========================================
  // RETURN
  // ==========================================
  
  return {
    isConnected: webSocketService.isConnected(),
    disconnect: () => webSocketService.disconnect(),
    connect: () => webSocketService.connect(),
  };
}

export default useWebSocket;