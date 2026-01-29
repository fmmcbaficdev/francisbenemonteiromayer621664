// src/core/facade/AuthFacade.ts
import { AuthStore } from '../state/AuthStore';
import type { UserState } from '../state/AuthStore';
import { AuthService } from '../services/AuthService';
import type { Observable } from 'rxjs';
import type { LoginRequest } from '../model/types'; 

export class AuthFacade {
  
  static get authState$(): Observable<UserState> {
    return AuthStore.state;
  }

  static getCurrentState(): UserState {
    return AuthStore.getCurrentState();
  }

  static async login(credentials: LoginRequest): Promise<void> {
    AuthStore.updateState({ loading: true, error: null });
    
    try {
      // Tipando o retorno do axios para garantir que data tenha accessToken/refreshToken
      const { data } = await AuthService.login(credentials);
      
      // Armazenamento consistente com o que definimos no AuthStore
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);

      AuthStore.updateState({ 
        isAuthenticated: true, 
        user: data.user, 
        accessToken: data.accessToken, 
        refreshToken: data.refreshToken,
        loading: false 
      });
    } catch (error: any) {
      AuthStore.updateState({ 
        loading: false, 
        error: error.response?.data?.message || 'Falha na autenticação' 
      });
      throw error;
    }
  }

  static logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    AuthStore.clear();
  }
}