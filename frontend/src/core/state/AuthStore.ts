// src/core/state/AuthStore.ts
import { BehaviorSubject, Observable } from 'rxjs';
import type { User } from '../model/types';

// Definimos exatamente a estrutura do estado de autenticação
export interface UserState {
  isAuthenticated: boolean;
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
}

const initialState: UserState = {
  isAuthenticated: !!localStorage.getItem('accessToken'),
  user: null, // Pode ser carregado de um 'profile' após o boot
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  loading: false,
  error: null
};

export class AuthStore {
  // O Subject é privado para evitar manipulação externa direta (Encapsulamento)
  private static state$ = new BehaviorSubject<UserState>(initialState);

  /**
   * Retorna o estado como um Observable para que os componentes 
   * ou a Facade possam "observar" as mudanças.
   */
  static get state(): Observable<UserState> {
    return this.state$.asObservable();
  }

  /**
   * Atualiza o estado de forma imutável.
   * Partial<UserState> permite atualizar apenas os campos necessários.
   */
  static updateState(newState: Partial<UserState>): void {
    const currentState = this.state$.getValue();
    this.state$.next({ ...currentState, ...newState });
  }

  /**
   * Método síncrono para obter o valor atual sem precisar de subscrição.
   * Útil para Interceptors de API.
   */
  static getCurrentState(): UserState {
    return this.state$.getValue();
  }

  /**
   * Limpa o estado (útil para Logout)
   */
  static clear(): void {
    this.state$.next({
      ...initialState,
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null
    });
  }
}