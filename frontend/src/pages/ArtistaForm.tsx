// ==========================================
// ARTISTA FORM - CRIAR/EDITAR
// ==========================================

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Save, User } from 'lucide-react';
import { artistasApi } from '../core/services/api';
import type { Artista, ArtistaForm as ArtistaFormType } from '../core/model/types';
import { Layout, Loading } from '../components';
import toast from 'react-hot-toast';

export function ArtistaForm() {
  const { id } = useParams<{ id: string }>();
  const isEditing = Boolean(id);
  const navigate = useNavigate();

  const [loading, setLoading] = useState(isEditing);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState<ArtistaFormType>({
    nome: '',
    biografia: '',
    dataNascimento: '',
    nacionalidade: '',
  });

  useEffect(() => {
    if (isEditing && id) {
      const fetchArtista = async () => {
        try {
          const artista: Artista = await artistasApi.buscarPorId(Number(id));
          setFormData({
            nome: artista.nome,
            biografia: artista.biografia || '',
            dataNascimento: artista.dataNascimento || '',
            nacionalidade: artista.nacionalidade || '',
          });
        } catch (error) {
          toast.error('Erro ao carregar artista');
          navigate('/artistas');
        } finally {
          setLoading(false);
        }
      };
      fetchArtista();
    }
  }, [id, isEditing, navigate]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.nome.trim()) {
      toast.error('O nome é obrigatório');
      return;
    }

    setSaving(true);
    try {
      if (isEditing && id) {
        await artistasApi.atualizar(Number(id), formData);
        toast.success('Artista atualizado com sucesso!');
      } else {
        await artistasApi.criar(formData);
        toast.success('Artista criado com sucesso!');
      }
      navigate('/artistas');
    } catch (error) {
      toast.error('Erro ao salvar artista');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <Layout><Loading fullScreen message="Carregando artista..." /></Layout>;
  }

  return (
    <Layout>
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <button
            onClick={() => navigate('/artistas')}
            className="flex items-center gap-2 text-primary-600 hover:text-primary-700 mb-4 font-medium"
          >
            <ArrowLeft className="w-5 h-5" />
            Voltar para Artistas
          </button>
          <h1 className="text-3xl font-bold text-primary-600 flex items-center gap-3">
            <div className="w-12 h-12 bg-gold-400 rounded-xl flex items-center justify-center">
              <User className="w-7 h-7 text-primary-900" />
            </div>
            {isEditing ? 'Editar Artista' : 'Novo Artista'}
          </h1>
        </div>

        {/* Form */}
        <div className="card p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Nome */}
            <div>
              <label htmlFor="nome" className="label">
                Nome <span className="text-accent-500">*</span>
              </label>
              <input
                id="nome"
                name="nome"
                type="text"
                value={formData.nome}
                onChange={handleChange}
                className="input"
                placeholder="Ex: Serj Tankian"
                required
                autoFocus
              />
            </div>

            {/* Nacionalidade */}
            <div>
              <label htmlFor="nacionalidade" className="label">
                Nacionalidade
              </label>
              <input
                id="nacionalidade"
                name="nacionalidade"
                type="text"
                value={formData.nacionalidade}
                onChange={handleChange}
                className="input"
                placeholder="Ex: Americana"
              />
            </div>

            {/* Data de Nascimento */}
            <div>
              <label htmlFor="dataNascimento" className="label">
                Data de Nascimento
              </label>
              <input
                id="dataNascimento"
                name="dataNascimento"
                type="date"
                value={formData.dataNascimento}
                onChange={handleChange}
                className="input"
              />
            </div>

            {/* Biografia */}
            <div>
              <label htmlFor="biografia" className="label">
                Biografia
              </label>
              <textarea
                id="biografia"
                name="biografia"
                value={formData.biografia}
                onChange={handleChange}
                rows={5}
                className="input resize-none"
                placeholder="Conte um pouco sobre o artista..."
              />
            </div>

            {/* Actions */}
            <div className="flex gap-4 pt-4 border-t border-gray-200">
              <button
                type="button"
                onClick={() => navigate('/artistas')}
                className="flex-1 btn-secondary"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={saving || !formData.nome.trim()}
                className="flex-1 btn-gold flex items-center justify-center gap-2"
              >
                {saving ? (
                  <>
                    <Loading size="sm" />
                    Salvando...
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
      </div>
    </Layout>
  );
}

export default ArtistaForm;