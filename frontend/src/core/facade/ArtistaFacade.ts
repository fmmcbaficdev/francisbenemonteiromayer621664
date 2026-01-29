// src/core/facade/ArtistaFacade.ts
import { artistasApi } from '../services/api';
import { ArtistaStore } from '../state/ArtistaStore';
// CORREÇÃO: Usar 'import type' para interfaces evita erros de 'verbatimModuleSyntax' no build
import type { Artista, ArtistaForm, ArtistaResumo, Page } from '../model/types';
import type { Observable } from 'rxjs';

export class ArtistaFacade {
  // ═══════════════════════════════════════════════════════════
  // OBSERVABLES (delegados para o Store com tipagem explícita)
  // ═══════════════════════════════════════════════════════════

  // CORREÇÃO: Definir explicitamente o tipo de retorno do Observable
  static get artistas$(): Observable<ArtistaResumo[]> {
    return ArtistaStore.artistas$;
  }

  static get artistaSelecionado$(): Observable<Artista | null> {
    return ArtistaStore.artistaSelecionado$;
  }

  static get loading$(): Observable<boolean> {
    return ArtistaStore.loading$;
  }

  static get error$(): Observable<string | null> {
    return ArtistaStore.error$;
  }

  static get paginacao$(): Observable<{ totalElements: number; totalPages: number; currentPage: number }> {
    return ArtistaStore.paginacao$;
  }

  // ═══════════════════════════════════════════════════════════
  // MÉTODOS DE AÇÃO
  // ═══════════════════════════════════════════════════════════

  static async listar(
    page = 0,
    size = 10,
    sort = 'nome,asc',
    busca?: string
  ): Promise<Page<ArtistaResumo>> {
    ArtistaStore.setLoading(true);

    try {
      const response = await artistasApi.listar(page, size, sort, busca);
      ArtistaStore.setArtistas(response);
      return response;
    } catch (error: unknown) { // CORREÇÃO: Usar 'unknown' em vez de 'any' para catch blocks (Sênior)
      const message = this.extractErrorMessage(error, 'Erro ao carregar artistas');
      ArtistaStore.setError(message);
      throw error;
    }
  }

  static async buscarPorId(id: number): Promise<Artista> {
    ArtistaStore.setLoading(true);
    try {
      const artista = await artistasApi.buscarPorId(id);
      ArtistaStore.setArtistaSelecionado(artista);
      ArtistaStore.setLoading(false);
      return artista;
    } catch (error: unknown) {
      const message = this.extractErrorMessage(error, 'Erro ao carregar artista');
      ArtistaStore.setError(message);
      throw error;
    }
  }

  static async criar(data: ArtistaForm): Promise<Artista> {
    ArtistaStore.setLoading(true);
    try {
      const novoArtista = await artistasApi.criar(data);
      // Sincroniza o Store local
      ArtistaStore.addArtista({
        id: novoArtista.id,
        nome: novoArtista.nome,
        totalAlbuns: 0,
      });
      ArtistaStore.setLoading(false);
      return novoArtista;
    } catch (error: unknown) {
      const message = this.extractErrorMessage(error, 'Erro ao criar artista');
      ArtistaStore.setError(message);
      throw error;
    }
  }

  static async atualizar(id: number, data: ArtistaForm): Promise<Artista> {
    ArtistaStore.setLoading(true);
    try {
      const artistaAtualizado = await artistasApi.atualizar(id, data);
      ArtistaStore.updateArtista(id, {
        nome: artistaAtualizado.nome,
        biografia: artistaAtualizado.biografia,
      });
      ArtistaStore.setLoading(false);
      return artistaAtualizado;
    } catch (error: any) { // Aqui mantemos 'any' ou tipagem de erro do Axios para ler o .status
      const status = error.response?.status;
      let message = 'Erro ao atualizar artista';

      if (status === 409) {
        message = 'Este artista foi modificado por outro usuário. Recarregue a página e tente novamente.';
      } else if (error.response?.data?.message) {
        message = error.response.data.message;
      }

      ArtistaStore.setError(message);
      throw error;
    }
  }

  // ═══════════════════════════════════════════════════════════
  // MÉTODOS AUXILIARES E HELPERS
  // ═══════════════════════════════════════════════════════════

  /**
   * Helper privado para evitar repetição de código no tratamento de erro
   */
  private static extractErrorMessage(error: any, defaultMsg: string): string {
    return error.response?.data?.message || defaultMsg;
  }

  static async deletar(id: number): Promise<void> {
    ArtistaStore.setLoading(true);
    try {
      await artistasApi.deletar(id);
      ArtistaStore.removeArtista(id);
      ArtistaStore.setLoading(false);
    } catch (error: unknown) {
      const message = this.extractErrorMessage(error, 'Erro ao deletar artista');
      ArtistaStore.setError(message);
      throw error;
    }
  }

  static selecionarArtista(artista: Artista | null): void {
    ArtistaStore.setArtistaSelecionado(artista);
  }

  static limparSelecao(): void {
    ArtistaStore.clearSelection();
  }

  static reset(): void {
    ArtistaStore.reset();
  }
}