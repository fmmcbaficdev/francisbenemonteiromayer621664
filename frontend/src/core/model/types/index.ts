// ==========================================
// TIPOS TYPESCRIPT - SEPLAG MVP
// ==========================================

// ==========================================
// AUTH
// ==========================================
export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface User {
  id: number;
  username: string;
  nome: string;
}

// ==========================================
// AUDITORIA BASE (Padrão Sênior)
// ==========================================
export interface Auditable {
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number; // Optimistic Locking
}

// ==========================================
// ARTISTA
// ==========================================
export interface Artista extends Auditable {
  id: number;
  nome: string;
  biografia?: string;
  dataNascimento?: string;
  nacionalidade?: string;
  totalAlbuns?: number;
}

export interface ArtistaResumo {
  id: number;
  nome: string;
  totalAlbuns?: number;
}

export interface ArtistaForm {
  nome: string;
  biografia?: string;
  dataNascimento?: string;
  nacionalidade?: string;
  version?: number; // Para Optimistic Locking no update
}

// ==========================================
// ALBUM
// ==========================================
export interface Album extends Auditable {
  id: number;
  titulo: string;
  anoLancamento?: number;
  descricao?: string;
  artistas: ArtistaResumo[];
  imagensUrls?: string[];
}

export interface AlbumResumo {
  id: number;
  titulo: string;
  anoLancamento?: number;
  primeiraImagemUrl?: string;
  artistas?: ArtistaResumo[];
}

export interface AlbumForm {
  titulo: string;
  anoLancamento?: number;
  descricao?: string;
  artistasIds: number[];
  version?: number; // Para Optimistic Locking no update
}

// ==========================================
// PAGINAÇÃO
// ==========================================
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ==========================================
// WEBSOCKET
// ==========================================
export interface WebSocketNotification {
  tipo: 'ALBUM_CRIADO' | 'ALBUM_ATUALIZADO' | 'ALBUM_DELETADO';
  mensagem: string;
  album?: AlbumResumo;
  timestamp: string;
}

// ==========================================
// API ERROR
// ==========================================
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
  path: string;
  error?: string;
  errors?: string[];
}

// ==========================================
// SYNC REGIONAIS RESULT
// ==========================================
export interface SyncRegionaisResult {
  sucesso: boolean;
  mensagem: string;
  estatisticas?: {
    criados: number;
    atualizados: number;
    desativados: number;
    semMudancas: number;
    totalAPI: number;
    totalBanco: number;
    duracaoMs: number;
  };
}

// ==========================================
// REGIONAL
// ==========================================
export interface Regional {
  id: number;
  codigoExterno: number;
  nome: string;
  ativa: boolean;
  externalHash?: string;
  ultimaSincronizacao?: string;
  createdAt?: string;
  updatedAt?: string;
}

// ==========================================
// UPLOAD RESPONSE
// ==========================================
export interface UploadImagemResponseDTO {
  albumId: number;
  mensagem: string;
  imagens: ImagemInfoDTO[];
  totalImagens: number;
}

export interface ImagemInfoDTO {
  id: number;
  nomeArquivo: string;
  urlPresigned: string;
  contentType: string;
  tamanho: number;
}
