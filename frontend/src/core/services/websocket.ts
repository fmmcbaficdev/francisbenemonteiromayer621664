// ==========================================
// WEBSOCKET SERVICE - STOMP OVER SOCKJS
// ==========================================

import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { WebSocketNotification } from '../model/types';

// ==========================================
// TYPES
// ==========================================

type NotificationCallback = (notification: WebSocketNotification) => void;

// ==========================================
// WEBSOCKET SERVICE CLASS
// ==========================================

class WebSocketService {
  private client: Client | null = null;
  private callbacks: NotificationCallback[] = [];
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  // ==========================================
  // CONNECT
  // ==========================================

  connect(): void {
    if (this.client?.connected) {
      console.log('WebSocket já conectado');
      return;
    }

    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),

      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('STOMP Debug:', str);
        }
      },

      reconnectDelay: this.reconnectDelay,

      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('✅ WebSocket conectado!');
        this.reconnectAttempts = 0;
        this.subscribeToNotifications();
      },

      onStompError: (frame) => {
        console.error('❌ Erro STOMP:', frame.headers['message']);
        console.error('Detalhes:', frame.body);
      },

      onWebSocketClose: () => {
        console.log('⚠️ WebSocket desconectado');
        this.handleReconnect();
      },

      onWebSocketError: (error) => {
        console.error('❌ Erro WebSocket:', error);
      },
    });

    this.client.activate();
  }

  // ==========================================
  // SUBSCRIBE TO NOTIFICATIONS
  // ==========================================

  private subscribeToNotifications(): void {
    if (!this.client?.connected) return;

    // Inscrever no tópico de notificações de álbuns
    this.client.subscribe('/topic/albuns', (message: IMessage) => {
      try {
        const notification: WebSocketNotification = JSON.parse(message.body);
        console.log('📨 Notificação recebida:', notification);
        this.notifyCallbacks(notification);
      } catch (error) {
        console.error('Erro ao processar notificação:', error);
      }
    });

    console.log('📡 Inscrito no tópico /topic/albuns');
  }

  // ==========================================
  // HANDLE RECONNECT
  // ==========================================

  private handleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`🔄 Tentando reconectar... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);

      setTimeout(() => {
        this.connect();
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('❌ Máximo de tentativas de reconexão atingido');
    }
  }

  // ==========================================
  // DISCONNECT
  // ==========================================

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      console.log('🔌 WebSocket desconectado manualmente');
    }
  }

  // ==========================================
  // CALLBACK MANAGEMENT
  // ==========================================

  subscribe(callback: NotificationCallback): () => void {
    this.callbacks.push(callback);

    // Retorna função de unsubscribe
    return () => {
      const index = this.callbacks.indexOf(callback);
      if (index > -1) {
        this.callbacks.splice(index, 1);
      }
    };
  }

  private notifyCallbacks(notification: WebSocketNotification): void {
    this.callbacks.forEach((callback) => {
      try {
        callback(notification);
      } catch (error) {
        console.error('Erro em callback de notificação:', error);
      }
    });
  }

  // ==========================================
  // STATUS
  // ==========================================

  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

// ==========================================
// SINGLETON INSTANCE
// ==========================================

export const webSocketService = new WebSocketService();
export default webSocketService;