// ==========================================
// AUTH CONTEXT - GERENCIAMENTO DE AUTENTICAÇÃO
// ==========================================

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback
} from 'react';
import type { ReactNode } from 'react';
import {
  setAccessToken,
  setRefreshToken,
  clearTokens,
  isAuthenticated as checkAuth,
  getStoredUser,
  setStoredUser,
} from '../utils/storage';
import { authApi } from '../core/services/api';
import type { AuthResponse, User } from '../core/model/types';

// ==========================================
// TYPES
// ==========================================

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  error: string | null;
}

// ==========================================
// CONTEXT
// ==========================================

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// ==========================================
// PROVIDER
// ==========================================

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ==========================================
  // CHECK INITIAL AUTH
  // ==========================================
  
  useEffect(() => {
    const checkInitialAuth = () => {
      try {
        if (checkAuth()) {
          const storedUser = getStoredUser();
          if (storedUser) {
            setUser({
              id: 0, // Não temos o ID armazenado
              username: storedUser.username,
              nome: storedUser.nome,
            });
          }
        }
      } catch (err) {
        console.error('Erro ao verificar autenticação:', err);
        clearTokens();
      } finally {
        setIsLoading(false);
      }
    };
    
    checkInitialAuth();
  }, []);

  // ==========================================
  // LOGIN
  // ==========================================
  
  const login = useCallback(async (username: string, password: string) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const response: AuthResponse = await authApi.login(username, password);
      
      // Salvar tokens
      setAccessToken(response.accessToken);
      setRefreshToken(response.refreshToken);
      
      // Decodificar token para obter info do usuário
      const payload = JSON.parse(atob(response.accessToken.split('.')[1]));
      
      const userData: User = {
        id: payload.userId || 0,
        username: payload.sub || username,
        nome: payload.nome || username,
      };
      
      // Salvar usuário
      setStoredUser({ username: userData.username, nome: userData.nome });
      setUser(userData);
      
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      const message = axiosError.response?.data?.message || 'Erro ao fazer login. Verifique suas credenciais.';
      setError(message);
      throw new Error(message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // ==========================================
  // LOGOUT
  // ==========================================
  
  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
    setError(null);
  }, []);

  // ==========================================
  // VALUE
  // ==========================================
  
  const value: AuthContextType = {
    user,
    isAuthenticated: !!user && checkAuth(),
    isLoading,
    login,
    logout,
    error,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

// ==========================================
// HOOK
// ==========================================

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  
  if (context === undefined) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  
  return context;
}

export default AuthContext;