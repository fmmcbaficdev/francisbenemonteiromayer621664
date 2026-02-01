// ==========================================
// ARTISTA DETALHES - VISUALIZAÇÃO COMPLETA
// ==========================================

import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Edit,
  Music,
  Calendar,
  MapPin,
  Disc3,
  Plus,
  FileText
} from 'lucide-react';
import { artistasApi, albunsApi } from '../core/services/api';
import type { Artista, AlbumResumo, Page } from '../core/model/types';
import { Layout, Loading } from '../components';
import toast from 'react-hot-toast';

export function ArtistaDetalhes() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [artista, setArtista] = useState<Artista | null>(null);
  const [albuns, setAlbuns] = useState<AlbumResumo[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingAlbuns, setLoadingAlbuns] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      if (!id) {
        navigate('/artistas');
        return;
      }

      try {
        // Carregar dados do artista
        const artistaData = await artistasApi.buscarPorId(Number(id));
        setArtista(artistaData);
        setLoading(false);

        // Carregar álbuns do artista usando o endpoint correto
        setLoadingAlbuns(true);
        try {
          const response: Page<AlbumResumo> = await albunsApi.buscarPorArtista(
            Number(id),
            0,
            100
          );
          setAlbuns(response.content);
        } catch (error) {
          console.error('Erro ao carregar álbuns:', error);
          // Não exibe erro para o usuário, apenas não mostra álbuns
          setAlbuns([]);
        } finally {
          setLoadingAlbuns(false);
        }
      } catch (error) {
        toast.error('Erro ao carregar dados do artista');
        navigate('/artistas');
      }
    };

    fetchData();
  }, [id, navigate]);

  if (loading || !artista) {
    return (
      <Layout>
        <Loading fullScreen message="Carregando artista..." />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="space-y-6">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-2 text-sm text-gray-600">
          <Link to="/artistas" className="hover:text-primary-600 transition-colors">
            Artistas
          </Link>
          <span>/</span>
          <span className="text-gray-900 font-medium">{artista.nome}</span>
        </nav>

        {/* Header do Artista */}
        <div className="card p-8">
          <div className="flex flex-col md:flex-row justify-between items-start gap-6">
            {/* Info Principal */}
            <div className="flex-1">
              <div className="flex items-center gap-4 mb-4">
                <div className="w-16 h-16 bg-gold-400 rounded-2xl flex items-center justify-center">
                  <Music className="w-9 h-9 text-primary-900" />
                </div>
                <div>
                  <h1 className="text-4xl font-bold text-primary-600">
                    {artista.nome}
                  </h1>
                  <p className="text-gray-500 mt-1">
                    {albuns.length} álbum(ns) cadastrado(s)
                  </p>
                </div>
              </div>

              {/* Metadados */}
              <div className="flex flex-wrap gap-4 mb-6">
                {artista.nacionalidade && (
                  <div className="flex items-center gap-2 text-gray-600">
                    <MapPin className="w-5 h-5" />
                    <span>{artista.nacionalidade}</span>
                  </div>
                )}
                {artista.dataNascimento && (
                  <div className="flex items-center gap-2 text-gray-600">
                    <Calendar className="w-5 h-5" />
                    <span>
                      {new Date(artista.dataNascimento).toLocaleDateString('pt-BR')}
                    </span>
                  </div>
                )}
              </div>

              {/* Biografia */}
              {artista.biografia && (
                <div className="bg-gray-50 rounded-lg p-4">
                  <div className="flex items-center gap-2 mb-2">
                    <FileText className="w-5 h-5 text-gray-400" />
                    <h3 className="font-semibold text-gray-700">Biografia</h3>
                  </div>
                  <p className="text-gray-600 whitespace-pre-wrap">
                    {artista.biografia}
                  </p>
                </div>
              )}
            </div>

            {/* Ações */}
            <div className="flex flex-col gap-3">
              <Link
                to={`/artistas/${id}/editar`}
                className="btn-primary flex items-center gap-2"
              >
                <Edit className="w-5 h-5" />
                Editar Artista
              </Link>
              <button
                onClick={() => navigate('/artistas')}
                className="btn-secondary flex items-center gap-2"
              >
                <ArrowLeft className="w-5 h-5" />
                Voltar
              </button>
            </div>
          </div>
        </div>

        {/* Seção de Álbuns */}
        <div>
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
              <Disc3 className="w-7 h-7 text-primary-600" />
              Álbuns
            </h2>
            <Link
              to={`/albuns/novo?artistaId=${id}`}
              className="btn-gold flex items-center gap-2 text-sm"
            >
              <Plus className="w-4 h-4" />
              Novo Álbum
            </Link>
          </div>

          {/* Grid de Álbuns */}
          {loadingAlbuns ? (
            <div className="flex justify-center py-12">
              <Loading message="Carregando álbuns..." />
            </div>
          ) : albuns.length === 0 ? (
            <div className="card p-12 text-center">
              <Disc3 className="w-20 h-20 text-gray-300 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-700">
                Nenhum álbum cadastrado
              </h3>
              <p className="text-gray-500 mt-2">
                Este artista ainda não possui álbuns cadastrados.
              </p>
              <Link
                to={`/albuns/novo?artistaId=${id}`}
                className="btn-gold mt-6 inline-flex items-center gap-2"
              >
                <Plus className="w-5 h-5" />
                Cadastrar Primeiro Álbum
              </Link>
            </div>
          ) : (
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
                      <Link
                        to={`/albuns/${album.id}/editar`}
                        className="p-3 bg-white rounded-full text-primary-600 hover:bg-gold-400 hover:text-primary-900 transition-colors"
                      >
                        <Edit className="w-5 h-5" />
                      </Link>
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
          )}
        </div>
      </div>
    </Layout>
  );
}

export default ArtistaDetalhes;
