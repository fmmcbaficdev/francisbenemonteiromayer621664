// ==========================================
// ALBUM STORE - Gerenciamento de Estado Reativo
// ==========================================
// Padrão: BehaviorSubject (RxJS)
//
// INTEGRAÇÃO COM WEBSOCKET:
// Quando o WebSocket recebe notificação de novo álbum,
// o AlbumFacade chama AlbumStore.addAlbum() para atualizar
// a lista em todos os componentes automaticamente.
// ==========================================

import { BehaviorSubject, Observable } from 'rxjs';
import { map, distinctUntilChanged } from 'rxjs/operators';
import type { Album, AlbumResumo, Page } from '../model/types';

/**
 * Estado dos Álbuns
 */
export interface AlbumState {
  // Lista paginada
  albuns: AlbumResumo[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;

  // Álbum selecionado (para edição/visualização)
  albumSelecionado: Album | null;

  // Estado de UI
  loading: boolean;
  uploading: boolean; // Upload de imagens
  error: string | null;

  // Filtros
  busca: string;
  ordenacao: 'titulo,asc' | 'titulo,desc' | 'anoLancamento,desc';
}

const initialState: AlbumState = {
  albuns: [],
  totalElements: 0,
  totalPages: 0,
  currentPage: 0,
  pageSize: 12, // Grid de álbuns geralmente usa mais itens
  albumSelecionado: null,
  loading: false,
  uploading: false,
  error: null,
  busca: '',
  ordenacao: 'titulo,asc',
};

/**
 * Store de Álbuns usando BehaviorSubject
 *
 * INTEGRAÇÃO COM WEBSOCKET:
 * ```typescript
 * // No WebSocketService
 * wsService.onAlbumCriado((album) => {
 *   AlbumStore.addAlbum(album);
 *   toast.success(`Novo álbum: ${album.titulo}`);
 * });
 * ```
 */
export class AlbumStore {
  // BehaviorSubject privado (encapsulamento)
  private static state$ = new BehaviorSubject<AlbumState>(initialState);

  // ═══════════════════════════════════════════════════════════
  // OBSERVABLES PÚBLICOS (read-only)
  // ═══════════════════════════════════════════════════════════

  /**
   * Observable do estado completo
   */
  static get state(): Observable<AlbumState> {
    return this.state$.asObservable();
  }

  /**
   * Observable da lista de álbuns
   */
  static get albuns$(): Observable<AlbumResumo[]> {
    return this.state$.pipe(
      map(state => state.albuns),
      distinctUntilChanged()
    );
  }

  /**
   * Observable do álbum selecionado
   */
  static get albumSelecionado$(): Observable<Album | null> {
    return this.state$.pipe(
      map(state => state.albumSelecionado),
      distinctUntilChanged()
    );
  }

  /**
   * Observable de loading
   */
  static get loading$(): Observable<boolean> {
    return this.state$.pipe(
      map(state => state.loading),
      distinctUntilChanged()
    );
  }

  /**
   * Observable de uploading (imagens)
   */
  static get uploading$(): Observable<boolean> {
    return this.state$.pipe(
      map(state => state.uploading),
      distinctUntilChanged()
    );
  }

  /**
   * Observable de erro
   */
  static get error$(): Observable<string | null> {
    return this.state$.pipe(
      map(state => state.error),
      distinctUntilChanged()
    );
  }

  /**
   * Observable de paginação
   */
  static get paginacao$(): Observable<{ totalElements: number; totalPages: number; currentPage: number }> {
    return this.state$.pipe(
      map(state => ({
        totalElements: state.totalElements,
        totalPages: state.totalPages,
        currentPage: state.currentPage,
      })),
      distinctUntilChanged((prev, curr) =>
        prev.totalElements === curr.totalElements &&
        prev.totalPages === curr.totalPages &&
        prev.currentPage === curr.currentPage
      )
    );
  }

  // ═══════════════════════════════════════════════════════════
  // ACTIONS (modificam estado)
  // ═══════════════════════════════════════════════════════════

  /**
   * Atualiza o estado de forma imutável
   */
  static updateState(newState: Partial<AlbumState>): void {
    const currentState = this.state$.getValue();
    this.state$.next({ ...currentState, ...newState });
  }

  /**
   * Define a lista de álbuns (após busca na API)
   */
  static setAlbuns(page: Page<AlbumResumo>): void {
    this.updateState({
      albuns: page.content,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      currentPage: page.number,
      loading: false,
      error: null,
    });
  }

  /**
   * Define o álbum selecionado
   */
  static setAlbumSelecionado(album: Album | null): void {
    this.updateState({ albumSelecionado: album });
  }

  /**
   * Adiciona um novo álbum à lista (chamado via WebSocket)
   */
  static addAlbum(album: AlbumResumo): void {
    const current = this.state$.getValue();

    // Evitar duplicatas
    if (current.albuns.some(a => a.id === album.id)) {
      return;
    }

    this.updateState({
      albuns: [album, ...current.albuns],
      totalElements: current.totalElements + 1,
    });
  }

  /**
   * Atualiza um álbum na lista (chamado via WebSocket)
   */
  static updateAlbum(id: number, updated: Partial<Album>): void {
    const current = this.state$.getValue();
    const albuns = current.albuns.map(a =>
      a.id === id ? { ...a, ...updated } : a
    );
    this.updateState({ albuns });

    // Se é o álbum selecionado, atualizar também
    if (current.albumSelecionado?.id === id) {
      this.updateState({
        albumSelecionado: { ...current.albumSelecionado, ...updated }
      });
    }
  }

  /**
   * Remove um álbum da lista (chamado via WebSocket)
   */
  static removeAlbum(id: number): void {
    const current = this.state$.getValue();
    const albuns = current.albuns.filter(a => a.id !== id);
    this.updateState({
      albuns,
      totalElements: current.totalElements - 1,
    });

    // Se é o álbum selecionado, limpar
    if (current.albumSelecionado?.id === id) {
      this.updateState({ albumSelecionado: null });
    }
  }

  /**
   * Adiciona URL de imagem ao álbum selecionado
   */
  static addImagemAoAlbumSelecionado(url: string): void {
    const current = this.state$.getValue();
    if (current.albumSelecionado) {
      const imagens = current.albumSelecionado.imagensUrls || [];
      this.updateState({
        albumSelecionado: {
          ...current.albumSelecionado,
          imagensUrls: [...imagens, url],
        },
      });
    }
  }

  /**
   * Define estado de loading
   */
  static setLoading(loading: boolean): void {
    this.updateState({ loading, error: loading ? null : this.state$.getValue().error });
  }

  /**
   * Define estado de uploading
   */
  static setUploading(uploading: boolean): void {
    this.updateState({ uploading });
  }

  /**
   * Define erro
   */
  static setError(error: string | null): void {
    this.updateState({ error, loading: false });
  }

  /**
   * Define filtros de busca
   */
  static setFiltros(busca: string, ordenacao?: 'titulo,asc' | 'titulo,desc' | 'anoLancamento,desc'): void {
    this.updateState({
      busca,
      ordenacao: ordenacao || this.state$.getValue().ordenacao,
      currentPage: 0, // Reset para primeira página
    });
  }

  /**
   * Define página atual
   */
  static setPage(page: number): void {
    this.updateState({ currentPage: page });
  }

  // ═══════════════════════════════════════════════════════════
  // GETTERS SÍNCRONOS
  // ═══════════════════════════════════════════════════════════

  /**
   * Obtém valor atual do estado (síncrono)
   */
  static getCurrentState(): AlbumState {
    return this.state$.getValue();
  }

  /**
   * Obtém álbum por ID da lista atual
   */
  static getAlbumById(id: number): AlbumResumo | undefined {
    return this.state$.getValue().albuns.find(a => a.id === id);
  }

  // ═══════════════════════════════════════════════════════════
  // RESET
  // ═══════════════════════════════════════════════════════════

  /**
   * Reseta o estado para inicial
   */
  static reset(): void {
    this.state$.next(initialState);
  }

  /**
   * Limpa apenas o álbum selecionado
   */
  static clearSelection(): void {
    this.updateState({ albumSelecionado: null });
  }
}
