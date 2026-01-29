// ==========================================
// ALBUNS PAGE - GRID VIEW
// ==========================================

import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Plus, Search, Disc3, Edit, Trash2, ArrowUpDown, Calendar } from 'lucide-react';
import { albunsApi } from '../core/services/api';
import type { AlbumResumo, Page } from '../core/model/types';
import { Layout, Loading } from '../components';
import toast from 'react-hot-toast';

export function Albuns() {
  const [albuns, setAlbuns] = useState<AlbumResumo[]>([]);
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
  const pageSize = 12;

  const fetchAlbuns = useCallback(async () => {
    setLoading(true);
    try {
      const response: Page<AlbumResumo> = await albunsApi.listar(
        page, pageSize, `titulo,${sortOrder}`, buscaAplicada || undefined
      );
      setAlbuns(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error) {
      toast.error('Erro ao carregar álbuns');
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, sortOrder, buscaAplicada]);

  useEffect(() => {
    fetchAlbuns();
  }, [fetchAlbuns]);

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
      await albunsApi.deletar(deleteId);
      toast.success('Álbum excluído com sucesso!');
      setDeleteId(null);
      fetchAlbuns();
    } catch (error) {
      toast.error('Erro ao excluir álbum');
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
                <Disc3 className="w-7 h-7 text-primary-900" />
              </div>
              Álbuns
            </h1>
            <p className="text-gray-500 mt-1 ml-15">
              {totalElements} álbum(ns) cadastrado(s)
            </p>
          </div>
          <Link to="/albuns/novo" className="btn-gold flex items-center gap-2">
            <Plus className="w-5 h-5" />
            Novo Álbum
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
                placeholder="Buscar por título do álbum..."
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
            <Loading message="Carregando álbuns..." />
          </div>
        ) : albuns.length === 0 ? (
          <div className="card p-12 text-center">
            <Disc3 className="w-20 h-20 text-gray-300 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-700">Nenhum álbum encontrado</h3>
            <p className="text-gray-500 mt-2">
              {busca ? 'Tente buscar por outro termo' : 'Comece cadastrando um novo álbum'}
            </p>
            {!busca && (
              <Link to="/albuns/novo" className="btn-gold mt-6 inline-flex items-center gap-2">
                <Plus className="w-5 h-5" />
                Novo Álbum
              </Link>
            )}
          </div>
        ) : (
          <>
            {/* Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {albuns.map((album) => (
                <div key={album.id} className="card-hover group">
                  {/* Cover */}
                  <div className="aspect-square bg-gradient-to-br from-primary-100 to-primary-200 relative overflow-hidden">
                    {album.primeiraImagemUrl ? (
                      <img
                        src={album.primeiraImagemUrl}
                        alt={album.titulo}
                        className="w-full h-full object-cover"
                        loading="lazy"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center">
                        <Disc3 className="w-24 h-24 text-primary-300" />
                      </div>
                    )}
                    {/* Overlay */}
                    <div className="absolute inset-0 bg-primary-900/0 group-hover:bg-primary-900/60 transition-all flex items-center justify-center opacity-0 group-hover:opacity-100">
                      <div className="flex gap-2">
                        <button
                          onClick={() => navigate(`/albuns/${album.id}/editar`)}
                          className="p-3 bg-white rounded-full text-primary-600 hover:bg-gold-400 hover:text-primary-900 transition-colors"
                          aria-label={`Editar álbum ${album.titulo}`}
                        >
                          <Edit className="w-5 h-5" />
                        </button>
                        <button
                          onClick={() => setDeleteId(album.id)}
                          className="p-3 bg-white rounded-full text-accent-500 hover:bg-accent-500 hover:text-white transition-colors"
                          aria-label={`Excluir álbum ${album.titulo}`}
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                    </div>
                  </div>
                  
                  {/* Info */}
                  <div className="p-4">
                    <h3 className="font-bold text-gray-900 truncate" title={album.titulo}>
                      {album.titulo}
                    </h3>
                    {album.anoLancamento && (
                      <p className="text-sm text-gray-500 flex items-center gap-1 mt-1">
                        <Calendar className="w-4 h-4" />
                        {album.anoLancamento}
                      </p>
                    )}
                  </div>
                </div>
              ))}
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
            <h3 className="text-xl font-bold text-gray-900 mb-2">Excluir Álbum</h3>
            <p className="text-gray-600 mb-6">
              Tem certeza que deseja excluir este álbum? Esta ação não pode ser desfeita.
            </p>
            <div className="flex gap-3">
              <button onClick={() => setDeleteId(null)} className="flex-1 btn-secondary" disabled={isDeleting}>
                Cancelar
              </button>
              <button onClick={handleDelete} className="flex-1 btn-danger" disabled={isDeleting}>
                {isDeleting ? 'Excluindo...' : 'Excluir'}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default Albuns;