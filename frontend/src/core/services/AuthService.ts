// ==========================================
// AUTH SERVICE
// ==========================================

import { api } from './api';
import type { LoginRequest, AuthResponse, User } from '../model/types';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User | null;
}

export class AuthService {
  static async login(credentials: LoginRequest): Promise<{ data: LoginResponse }> {
    const response = await api.post<AuthResponse>('/v1/auth/login', credentials);

    return {
      data: {
        accessToken: response.data.accessToken,
        refreshToken: response.data.refreshToken,
        user: null, // User profile can be loaded separately if needed
      }
    };
  }
}
