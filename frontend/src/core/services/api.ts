// ==========================================
// API SERVICE
// ==========================================

import axios from 'axios';
import { 
  getAccessToken, 
  getRefreshToken, 
  setAccessToken, 
  clearTokens,
  isTokenExpiringSoon 
} from '../../utils/storage';
import type { AuthResponse, Artista, ArtistaForm, ArtistaResumo, Album, AlbumForm, AlbumResumo, Page, Regional } from '../model/types';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
api.interceptors.request.use(
  async (config) => {
    const token = getAccessToken();
    
    if (token) {
      if (isTokenExpiringSoon(1) && !config.url?.includes('/auth/')) {
        try {
          await refreshAccessToken();
        } catch (error) {
          console.error('Erro ao renovar token:', error);
        }
      }
      
      const currentToken = getAccessToken();
      if (currentToken) {
        config.headers.Authorization = `Bearer ${currentToken}`;
      }
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        await refreshAccessToken();
        const newToken = getAccessToken();
        if (newToken) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
        }
        return api(originalRequest);
      } catch (refreshError) {
        clearTokens();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

async function refreshAccessToken(): Promise<void> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error('No refresh token');

  const response = await axios.post<AuthResponse>(
    `${API_URL}/v1/auth/refresh`,
    {},
    {
      headers: {
        Authorization: `Bearer ${refreshToken}`
      }
    }
  );
  setAccessToken(response.data.accessToken);
}

// AUTH API
export const authApi = {
  login: async (username: string, password: string): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/v1/auth/login', { username, password });
    return response.data;
  },
};

// ARTISTAS API
export const artistasApi = {
  listar: async (page = 0, size = 10, sort = 'nome,asc', busca?: string): Promise<Page<ArtistaResumo>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size), sort });

    // Se houver termo de busca, usa o endpoint /buscar com parâmetro 'nome'
    if (busca && busca.trim()) {
      params.append('nome', busca.trim());
      const response = await api.get<Page<ArtistaResumo>>(`/v1/artistas/buscar?${params}`);
      return response.data;
    }

    const response = await api.get<Page<ArtistaResumo>>(`/v1/artistas?${params}`);
    return response.data;
  },
  
  buscarPorId: async (id: number): Promise<Artista> => {
    const response = await api.get<Artista>(`/v1/artistas/${id}`);
    return response.data;
  },
  
  criar: async (data: ArtistaForm): Promise<Artista> => {
    const response = await api.post<Artista>('/v1/artistas', data);
    return response.data;
  },
  
  atualizar: async (id: number, data: ArtistaForm): Promise<Artista> => {
    const response = await api.put<Artista>(`/v1/artistas/${id}`, data);
    return response.data;
  },
  
  deletar: async (id: number): Promise<void> => {
    await api.delete(`/v1/artistas/${id}`);
  },
  
  listarTodos: async (): Promise<ArtistaResumo[]> => {
    const response = await api.get<Page<ArtistaResumo>>('/v1/artistas?size=1000');
    return response.data.content;
  },
};

// ALBUNS API
export const albunsApi = {
  listar: async (page = 0, size = 12, sort = 'titulo,asc', busca?: string): Promise<Page<AlbumResumo>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size), sort });

    // Se houver termo de busca, usa o endpoint /buscar com parâmetro 'titulo'
    if (busca && busca.trim()) {
      params.append('titulo', busca.trim());
      const response = await api.get<Page<AlbumResumo>>(`/v1/albuns/buscar?${params}`);
      return response.data;
    }

    const response = await api.get<Page<AlbumResumo>>(`/v1/albuns?${params}`);
    return response.data;
  },
  
  buscarPorId: async (id: number): Promise<Album> => {
    const response = await api.get<Album>(`/v1/albuns/${id}`);
    return response.data;
  },
  
  criar: async (data: AlbumForm): Promise<Album> => {
    const response = await api.post<Album>('/v1/albuns', data);
    return response.data;
  },
  
  atualizar: async (id: number, data: AlbumForm): Promise<Album> => {
    const response = await api.put<Album>(`/v1/albuns/${id}`, data);
    return response.data;
  },
  
  deletar: async (id: number): Promise<void> => {
    await api.delete(`/v1/albuns/${id}`);
  },
  
  uploadImagens: async (albumId: number, files: File[]): Promise<string[]> => {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    const response = await api.post<string[]>(`/v1/albuns/${albumId}/imagens`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
};

// REGIONAIS API
export const regionaisApi = {
  listar: async (): Promise<Regional[]> => {
    const response = await api.get<{ content: Regional[] }>('/v1/regionais?size=1000');
    return response.data.content;
  },
  
  sincronizar: async (): Promise<{ sucesso: boolean; mensagem: string }> => {
    const response = await api.post('/v1/regionais/sincronizar');
    return response.data;
  },
};

export default api;