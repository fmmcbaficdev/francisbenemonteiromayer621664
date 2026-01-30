import { useState, useEffect, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  ArrowLeft,
  Save,
  Disc3,
  Upload,
  X,
  Music
} from 'lucide-react';
import { albunsApi, artistasApi } from '../core/services/api';
import type { Album, AlbumForm as AlbumFormType, ArtistaResumo } from '../core/model/types';
import { Layout, Loading } from '../components';
import toast from 'react-hot-toast';

// Componente para preview de arquivos com cleanup de Object URLs
function FilePreviewList({ files, onRemove }: { files: File[]; onRemove: (index: number) => void }) {
  const previews = useMemo(() => {
    return files.map(file => URL.createObjectURL(file));
  }, [files]);

  // Cleanup Object URLs quando o componente desmontar ou files mudar
  useEffect(() => {
    return () => {
      previews.forEach(url => URL.revokeObjectURL(url));
    };
  }, [previews]);

  return (
    <div className="mb-4">
      <p className="text-sm text-gray-500 mb-2">Novas imagens:</p>
      <div className="flex flex-wrap gap-3">
        {files.map((file, index) => (
          <div key={index} className="relative group">
            <img
              src={previews[index]}
              alt={file.name}
              className="w-24 h-24 object-cover rounded-lg border border-gray-200"
            />
            <button
              type="button"
              onClick={() => onRemove(index)}
              title="Remover imagem"
              className="absolute -top-2 -right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <X className="w-4 h-4" />
            </button>
            <span className="absolute bottom-0 left-0 right-0 bg-black/50 text-white text-xs p-1 truncate rounded-b-lg">
              {(file.size / 1024 / 1024).toFixed(1)}MB
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

export function AlbumForm() {
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const navigate = useNavigate();

  // State
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [artistas, setArtistas] = useState<ArtistaResumo[]>([]);
  const [formData, setFormData] = useState<AlbumFormType>({
    titulo: '',
    anoLancamento: undefined,
    descricao: '',
    artistasIds: [],
  });
  
  // Upload state
  const [files, setFiles] = useState<File[]>([]);
  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [existingImageIds, setExistingImageIds] = useState<number[]>([]);
  const [uploading, setUploading] = useState(false);
  const [removingImageId, setRemovingImageId] = useState<number | null>(null);

  // ==========================================
  // FETCH DATA
  // ==========================================
  
  useEffect(() => {
    const fetchData = async () => {
      try {
        // Carregar artistas
        const artistasData = await artistasApi.listarTodos();
        setArtistas(artistasData);

        // Carregar álbum se editando
        if (isEditing && id) {
          const album: Album = await albunsApi.buscarPorId(Number(id));
          setFormData({
            titulo: album.titulo,
            anoLancamento: album.anoLancamento,
            descricao: album.descricao || '',
            artistasIds: album.artistas.map(a => a.id),
          });
          setExistingImages(album.imagensUrls || []);
          setExistingImageIds(album.imagensCapaIds || []);
        }
      } catch (error) {
        console.error('Erro ao carregar dados:', error);
        toast.error('Erro ao carregar dados');
        navigate('/albuns');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id, isEditing, navigate]);

  // ==========================================
  // HANDLERS
  // ==========================================
  
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData(prev => ({ 
      ...prev, 
      [name]: name === 'anoLancamento' ? (value ? Number(value) : undefined) : value 
    }));
  };

  const handleArtistaToggle = (artistaId: number) => {
    setFormData(prev => ({
      ...prev,
      artistasIds: prev.artistasIds.includes(artistaId)
        ? prev.artistasIds.filter(id => id !== artistaId)
        : [...prev.artistasIds, artistaId],
    }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(e.target.files || []);
    
    // Validar tamanho (10MB max)
    const validFiles = selectedFiles.filter(file => {
      if (file.size > 10 * 1024 * 1024) {
        toast.error(`${file.name} excede 10MB`);
        return false;
      }
      if (!file.type.startsWith('image/')) {
        toast.error(`${file.name} não é uma imagem`);
        return false;
      }
      return true;
    });

    setFiles(prev => [...prev, ...validFiles]);
  };

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  const removeExistingImage = async (index: number) => {
    if (!id) return;
    const imagemId = existingImageIds[index];
    if (imagemId == null) {
      setExistingImages(prev => prev.filter((_, i) => i !== index));
      setExistingImageIds(prev => prev.filter((_, i) => i !== index));
      return;
    }
    setRemovingImageId(imagemId);
    try {
      await albunsApi.deletarImagem(Number(id), imagemId);
      setExistingImages(prev => prev.filter((_, i) => i !== index));
      setExistingImageIds(prev => prev.filter((_, i) => i !== index));
      toast.success('Imagem removida.');
    } catch (err) {
      console.error('Erro ao remover imagem:', err);
      toast.error('Erro ao remover imagem.');
    } finally {
      setRemovingImageId(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.titulo.trim()) {
      toast.error('O título é obrigatório');
      return;
    }

    if (formData.artistasIds.length === 0) {
      toast.error('Selecione pelo menos um artista');
      return;
    }

    setSaving(true);
    try {
      let albumId: number;

      if (isEditing && id) {
        await albunsApi.atualizar(Number(id), formData);
        albumId = Number(id);
        toast.success('Álbum atualizado com sucesso!');
      } else {
        const novoAlbum = await albunsApi.criar(formData);
        albumId = novoAlbum.id;
        toast.success('Álbum criado com sucesso!');
      }

      // Upload de imagens
      if (files.length > 0) {
        setUploading(true);
        try {
          await albunsApi.uploadImagens(albumId, files);
          toast.success(`${files.length} imagem(ns) enviada(s)!`);
        } catch (error) {
          console.error('Erro no upload:', error);
          toast.error('Erro ao enviar imagens');
        }
        setUploading(false);
      }

      navigate('/albuns');
    } catch (error) {
      console.error('Erro ao salvar álbum:', error);
      toast.error('Erro ao salvar álbum');
    } finally {
      setSaving(false);
    }
  };

  // ==========================================
  // RENDER
  // ==========================================
  
  if (loading) {
    return (
      <Layout>
        <Loading fullScreen message="Carregando..." />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-3xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <button
            onClick={() => navigate('/albuns')}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
          >
            <ArrowLeft className="w-5 h-5" />
            Voltar para Álbuns
          </button>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Disc3 className="w-7 h-7 text-primary-600" />
            {isEditing ? 'Editar Álbum' : 'Novo Álbum'}
          </h1>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Info */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 space-y-6">
            <h2 className="text-lg font-semibold text-gray-900">Informações Básicas</h2>
            
            {/* Título */}
            <div>
              <label htmlFor="titulo" className="label">
                Título <span className="text-red-500">*</span>
              </label>
              <input
                id="titulo"
                name="titulo"
                type="text"
                value={formData.titulo}
                onChange={handleChange}
                className="input"
                placeholder="Ex: Harakiri"
                required
                autoFocus
              />
            </div>

            {/* Ano de Lançamento */}
            <div>
              <label htmlFor="anoLancamento" className="label">
                Ano de Lançamento
              </label>
              <input
                id="anoLancamento"
                name="anoLancamento"
                type="number"
                min="1900"
                max={new Date().getFullYear() + 1}
                value={formData.anoLancamento || ''}
                onChange={handleChange}
                className="input"
                placeholder="Ex: 2012"
              />
            </div>

            {/* Descrição */}
            <div>
              <label htmlFor="descricao" className="label">
                Descrição
              </label>
              <textarea
                id="descricao"
                name="descricao"
                value={formData.descricao}
                onChange={handleChange}
                rows={4}
                className="input resize-none"
                placeholder="Descrição do álbum..."
              />
            </div>
          </div>

          {/* Artistas */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">
              Artistas <span className="text-red-500">*</span>
            </h2>
            <p className="text-sm text-gray-500 mb-4">
              Selecione os artistas deste álbum (pode selecionar múltiplos)
            </p>
            
            {artistas.length === 0 ? (
              <p className="text-gray-500 text-center py-4">
                Nenhum artista cadastrado. 
                <a href="/artistas/novo" className="text-primary-600 hover:underline ml-1">
                  Cadastrar artista
                </a>
              </p>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-64 overflow-y-auto">
                {artistas.map((artista) => (
                  <label
                    key={artista.id}
                    className={`
                      flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-colors
                      ${formData.artistasIds.includes(artista.id)
                        ? 'bg-primary-50 border-primary-300'
                        : 'bg-white border-gray-200 hover:bg-gray-50'
                      }
                    `}
                  >
                    <input
                      type="checkbox"
                      checked={formData.artistasIds.includes(artista.id)}
                      onChange={() => handleArtistaToggle(artista.id)}
                      className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
                    />
                    <Music className="w-5 h-5 text-gray-400" />
                    <span className="font-medium text-gray-900">{artista.nome}</span>
                  </label>
                ))}
              </div>
            )}
            
            {formData.artistasIds.length > 0 && (
              <p className="mt-3 text-sm text-primary-600">
                {formData.artistasIds.length} artista(s) selecionado(s)
              </p>
            )}
          </div>

          {/* Images */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Imagens da Capa</h2>
            
            {/* Existing Images */}
            {existingImages.length > 0 && (
              <div className="mb-4">
                <p className="text-sm text-gray-500 mb-2">Imagens existentes:</p>
                <div className="flex flex-wrap gap-3">
                  {existingImages.map((url, index) => (
                    <div key={index} className="relative group">
                      <img
                        src={url}
                        alt={`Capa ${index + 1}`}
                        className="w-24 h-24 object-cover rounded-lg border border-gray-200"
                      />
                      <button
                        type="button"
                        onClick={() => removeExistingImage(index)}
                        disabled={removingImageId === existingImageIds[index]}
                        className="absolute -top-2 -right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity disabled:opacity-70"
                        aria-label={`Remover imagem ${index + 1}`}
                      >
                        {removingImageId === existingImageIds[index] ? (
                          <Loading size="sm" />
                        ) : (
                          <X className="w-4 h-4" />
                        )}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* New Files */}
            {files.length > 0 && (
              <FilePreviewList files={files} onRemove={removeFile} />
            )}

            {/* Upload Button */}
            <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
              <div className="flex flex-col items-center justify-center pt-5 pb-6">
                <Upload className="w-8 h-8 text-gray-400 mb-2" />
                <p className="text-sm text-gray-600">
                  <span className="font-medium text-primary-600">Clique para enviar</span> ou arraste arquivos
                </p>
                <p className="text-xs text-gray-500 mt-1">PNG, JPG até 10MB</p>
              </div>
              <input
                type="file"
                className="hidden"
                accept="image/*"
                multiple
                onChange={handleFileChange}
              />
            </label>
          </div>

          {/* Actions */}
          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => navigate('/albuns')}
              className="flex-1 btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={saving || uploading || !formData.titulo.trim() || formData.artistasIds.length === 0}
              className="flex-1 btn-primary flex items-center justify-center gap-2"
            >
              {saving || uploading ? (
                <>
                  <Loading size="sm" />
                  {uploading ? 'Enviando imagens...' : 'Salvando...'}
                </>
              ) : (
                <>
                  <Save className="w-5 h-5" />
                  {isEditing ? 'Atualizar' : 'Criar'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </Layout>
  );
}

export default AlbumForm;