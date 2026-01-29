// ==========================================
// ARTISTA STORE - Gerenciamento de Estado Reativo
// ==========================================
// Padrão: BehaviorSubject (RxJS)
//
// VANTAGENS SOBRE OUTRAS ABORDAGENS:
// - useState: Estado local, não compartilhado entre componentes
// - Context API: Re-renderiza toda árvore de componentes
// - Redux: Boilerplate excessivo para projetos médios
// - BehaviorSubject: Simples, reativo, eficiente, sempre tem valor
//
// COMO FUNCIONA:
// 1. Estado centralizado no BehaviorSubject
// 2. Componentes se inscrevem via .subscribe()
// 3. Mudanças são propagadas automaticamente
// 4. Facade orquestra operações complexas
// ==========================================

import { BehaviorSubject, Observable } from 'rxjs';
import { map, distinctUntilChanged } from 'rxjs/operators';
import type { Artista, ArtistaResumo, Page } from '../model/types';

/**
 * Estado dos Artistas
 */
export interface ArtistaState {
  // Lista paginada
  artistas: ArtistaResumo[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;

  // Artista selecionado (para edição/visualização)
  artistaSelecionado: Artista | null;

  // Estado de UI
  loading: boolean;
  error: string | null;

  // Filtros
  busca: string;
  ordenacao: 'nome,asc' | 'nome,desc';
}

const initialState: ArtistaState = {
  artistas: [],
  totalElements: 0,
  totalPages: 0,
  currentPage: 0,
  pageSize: 10,
  artistaSelecionado: null,
  loading: false,
  error: null,
  busca: '',
  ordenacao: 'nome,asc',
};

/**
 * Store de Artistas usando BehaviorSubject
 *
 * EXEMPLO DE USO:
 * ```typescript
 * // No componente
 * useEffect(() => {
 *   const sub = ArtistaStore.artistas$.subscribe(setArtistas);
 *   return () => sub.unsubscribe();
 * }, []);
 * ```
 */
export class ArtistaStore {
  // BehaviorSubject privado (encapsulamento)
  private static state$ = new BehaviorSubject<ArtistaState>(initialState);

  // ═══════════════════════════════════════════════════════════
  // OBSERVABLES PÚBLICOS (read-only)
  // ═══════════════════════════════════════════════════════════

  /**
   * Observable do estado completo
   */
  static get state(): Observable<ArtistaState> {
    return this.state$.asObservable();
  }

  /**
   * Observable da lista de artistas
   */
  static get artistas$(): Observable<ArtistaResumo[]> {
    return this.state$.pipe(
      map(state => state.artistas),
      distinctUntilChanged()
    );
  }

  /**
   * Observable do artista selecionado
   */
  static get artistaSelecionado$(): Observable<Artista | null> {
    return this.state$.pipe(
      map(state => state.artistaSelecionado),
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
  static updateState(newState: Partial<ArtistaState>): void {
    const currentState = this.state$.getValue();
    this.state$.next({ ...currentState, ...newState });
  }

  /**
   * Define a lista de artistas (após busca na API)
   */
  static setArtistas(page: Page<ArtistaResumo>): void {
    this.updateState({
      artistas: page.content,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      currentPage: page.number,
      loading: false,
      error: null,
    });
  }

  /**
   * Define o artista selecionado
   */
  static setArtistaSelecionado(artista: Artista | null): void {
    this.updateState({ artistaSelecionado: artista });
  }

  /**
   * Adiciona um novo artista à lista
   */
  static addArtista(artista: ArtistaResumo): void {
    const current = this.state$.getValue();
    this.updateState({
      artistas: [artista, ...current.artistas],
      totalElements: current.totalElements + 1,
    });
  }

  /**
   * Atualiza um artista na lista
   */
  static updateArtista(id: number, updated: Partial<Artista>): void {
    const current = this.state$.getValue();
    const artistas = current.artistas.map(a =>
      a.id === id ? { ...a, ...updated } : a
    );
    this.updateState({ artistas });

    // Se é o artista selecionado, atualizar também
    if (current.artistaSelecionado?.id === id) {
      this.updateState({
        artistaSelecionado: { ...current.artistaSelecionado, ...updated }
      });
    }
  }

  /**
   * Remove um artista da lista
   */
  static removeArtista(id: number): void {
    const current = this.state$.getValue();
    const artistas = current.artistas.filter(a => a.id !== id);
    this.updateState({
      artistas,
      totalElements: current.totalElements - 1,
    });

    // Se é o artista selecionado, limpar
    if (current.artistaSelecionado?.id === id) {
      this.updateState({ artistaSelecionado: null });
    }
  }

  /**
   * Define estado de loading
   */
  static setLoading(loading: boolean): void {
    this.updateState({ loading, error: loading ? null : this.state$.getValue().error });
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
  static setFiltros(busca: string, ordenacao?: 'nome,asc' | 'nome,desc'): void {
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
  // GETTERS SÍNCRONOS (para Interceptors, etc)
  // ═══════════════════════════════════════════════════════════

  /**
   * Obtém valor atual do estado (síncrono)
   */
  static getCurrentState(): ArtistaState {
    return this.state$.getValue();
  }

  /**
   * Obtém artista por ID da lista atual
   */
  static getArtistaById(id: number): ArtistaResumo | undefined {
    return this.state$.getValue().artistas.find(a => a.id === id);
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
   * Limpa apenas o artista selecionado
   */
  static clearSelection(): void {
    this.updateState({ artistaSelecionado: null });
  }
}
