// ==========================================
// ARTISTAS PAGE - LISTAGEM
// ==========================================

import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Search, Music, Disc3, Edit, Trash2, ArrowUpDown, Users } from 'lucide-react';
import { artistasApi } from '../core/services/api';
import type { ArtistaResumo, Page } from '../core/model/types';
import { Layout, Loading } from '../components';
import toast from 'react-hot-toast';

export function Artistas() {
  const [artistas, setArtistas] = useState<ArtistaResumo[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [busca, setBusca] = useState('');
  const [buscaAplicada, setBuscaAplicada] = useState(''); // Termo de busca efetivamente aplicado
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const navigate = useNavigate();
  const pageSize = 10;

  const fetchArtistas = useCallback(async () => {
    setLoading(true);
    try {
      const response: Page<ArtistaResumo> = await artistasApi.listar(
        page, pageSize, `nome,${sortOrder}`, buscaAplicada || undefined
      );
      setArtistas(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error) {
      toast.error('Erro ao carregar artistas');
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, sortOrder, buscaAplicada]);

  useEffect(() => {
    fetchArtistas();
  }, [fetchArtistas]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    setBuscaAplicada(busca); // Aplica o termo de busca somente ao clicar
  };

  const toggleSortOrder = () => {
    setSortOrder(prev => prev === 'asc' ? 'desc' : 'asc');
    setPage(0);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setIsDeleting(true);
    try {
      await artistasApi.deletar(deleteId);
      toast.success('Artista excluído com sucesso!');
      setDeleteId(null);
      fetchArtistas();
    } catch (error) {
      toast.error('Erro ao excluir artista');
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <Layout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-3xl font-bold text-primary-600 flex items-center gap-3">
              <div className="w-12 h-12 bg-gold-400 rounded-xl flex items-center justify-center">
                <Music className="w-7 h-7 text-primary-900" />
              </div>
              Artistas
            </h1>
            <p className="text-gray-500 mt-1 ml-15">
              {totalElements} artista(s) cadastrado(s)
            </p>
          </div>
          <Link to="/artistas/novo" className="btn-gold flex items-center gap-2">
            <Plus className="w-5 h-5" />
            Novo Artista
          </Link>
        </div>

        {/* Filters */}
        <div className="card p-4">
          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
                placeholder="Buscar por nome do artista..."
                className="input pl-12"
              />
            </div>
            <button
              type="button"
              onClick={toggleSortOrder}
              className="btn-secondary flex items-center gap-2"
            >
              <ArrowUpDown className="w-4 h-4" />
              {sortOrder === 'asc' ? 'A-Z' : 'Z-A'}
            </button>
            <button type="submit" className="btn-primary">
              Buscar
            </button>
          </form>
        </div>

        {/* Content */}
        {loading ? (
          <div className="flex justify-center py-12">
            <Loading message="Carregando artistas..." />
          </div>
        ) : artistas.length === 0 ? (
          <div className="card p-12 text-center">
            <Users className="w-20 h-20 text-gray-300 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-700">Nenhum artista encontrado</h3>
            <p className="text-gray-500 mt-2">
              {busca ? 'Tente buscar por outro termo' : 'Comece cadastrando um novo artista'}
            </p>
            {!busca && (
              <Link to="/artistas/novo" className="btn-gold mt-6 inline-flex items-center gap-2">
                <Plus className="w-5 h-5" />
                Novo Artista
              </Link>
            )}
          </div>
        ) : (
          <>
            {/* Table */}
            <div className="card overflow-hidden">
              <table className="w-full">
                <thead className="table-header">
                  <tr>
                    <th className="px-6 py-4 text-left text-sm font-semibold">Artista</th>
                    <th className="px-6 py-4 text-center text-sm font-semibold">Álbuns</th>
                    <th className="px-6 py-4 text-right text-sm font-semibold">Ações</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {artistas.map((artista) => (
                    <tr key={artista.id} className="table-row">
                      <td className="px-6 py-4">
                        <Link 
                          to={`/artistas/${artista.id}`}
                          className="flex items-center gap-4 hover:bg-primary-50 -mx-2 px-2 py-2 rounded-lg transition-colors"
                        >
                          <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                            <Music className="w-6 h-6 text-primary-600" />
                          </div>
                          <span className="font-semibold text-primary-600 hover:text-primary-700">
                            {artista.nome}
                          </span>
                        </Link>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className="badge-gold">
                          <Disc3 className="w-4 h-4 mr-1" />
                          {artista.totalAlbuns} álbuns
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => navigate(`/artistas/${artista.id}/editar`)}
                            className="p-2 text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                            title="Editar"
                          >
                            <Edit className="w-5 h-5" />
                          </button>
                          <button
                            onClick={() => setDeleteId(artista.id)}
                            className="p-2 text-accent-500 hover:bg-accent-50 rounded-lg transition-colors"
                            title="Excluir"
                          >
                            <Trash2 className="w-5 h-5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex justify-center gap-2">
                {Array.from({ length: totalPages }, (_, i) => (
                  <button
                    key={i}
                    onClick={() => setPage(i)}
                    className={`w-10 h-10 rounded-lg font-medium transition-colors ${
                      page === i
                        ? 'bg-primary-600 text-white'
                        : 'bg-white text-primary-600 border border-primary-200 hover:bg-primary-50'
                    }`}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
            )}
          </>
        )}
      </div>

      {/* Delete Modal */}
      {deleteId && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-900 mb-2">Excluir Artista</h3>
            <p className="text-gray-600 mb-6">
              Tem certeza que deseja excluir este artista? Esta ação não pode ser desfeita.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setDeleteId(null)}
                className="flex-1 btn-secondary"
                disabled={isDeleting}
              >
                Cancelar
              </button>
              <button
                onClick={handleDelete}
                className="flex-1 btn-danger"
                disabled={isDeleting}
              >
                {isDeleting ? 'Excluindo...' : 'Excluir'}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default Artistas;