// ==========================================
// REGIONAIS PAGE - SINCRONIZAÇÃO
// ==========================================

import { useState, useEffect, useCallback } from 'react';
import { MapPin, RefreshCw, CheckCircle, XCircle } from 'lucide-react';
import { regionaisApi } from '../core/services/api';
import type { Regional } from '../core/model/types';
import { Layout, Loading } from '../components';
import toast from 'react-hot-toast';

export function Regionais() {
  const [regionais, setRegionais] = useState<Regional[]>([]);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);

  // ==========================================
  // FETCH DATA
  // ==========================================
  
  const fetchRegionais = useCallback(async () => {
    try {
      const data = await regionaisApi.listar();
      setRegionais(data);
    } catch (error) {
      console.error('Erro ao carregar regionais:', error);
      toast.error('Erro ao carregar regionais');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchRegionais();
  }, [fetchRegionais]);

  // ==========================================
  // SYNC
  // ==========================================
  
  const handleSync = async () => {
    setSyncing(true);
    try {
      const result = await regionaisApi.sincronizar();
      if (result.sucesso) {
        toast.success(result.mensagem);
        fetchRegionais();
      } else {
        toast.error(result.mensagem);
      }
    } catch (error) {
      console.error('Erro ao sincronizar:', error);
      toast.error('Erro ao sincronizar regionais');
    } finally {
      setSyncing(false);
    }
  };

  // ==========================================
  // RENDER
  // ==========================================
  
  return (
    <Layout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
              <MapPin className="w-7 h-7 text-primary-600" />
              Regionais
            </h1>
            <p className="text-gray-500 mt-1">
              Regionais sincronizadas da API externa (Algoritmo O(n))
            </p>
          </div>
          <button
            onClick={handleSync}
            disabled={syncing}
            className="btn-primary flex items-center gap-2"
          >
            <RefreshCw className={`w-5 h-5 ${syncing ? 'animate-spin' : ''}`} />
            {syncing ? 'Sincronizando...' : 'Sincronizar'}
          </button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <p className="text-gray-500 text-sm">Total</p>
            <p className="text-2xl font-bold text-gray-900">{regionais.length}</p>
          </div>
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <p className="text-gray-500 text-sm">Ativas</p>
            <p className="text-2xl font-bold text-green-600">
              {regionais.filter(r => r.ativa).length}
            </p>
          </div>
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4">
            <p className="text-gray-500 text-sm">Inativas</p>
            <p className="text-2xl font-bold text-red-600">
              {regionais.filter(r => !r.ativa).length}
            </p>
          </div>
        </div>

        {/* Content */}
        {loading ? (
          <div className="flex justify-center py-12">
            <Loading message="Carregando regionais..." />
          </div>
        ) : regionais.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
            <MapPin className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900">Nenhuma regional encontrada</h3>
            <p className="text-gray-500 mt-2">
              Clique em "Sincronizar" para importar as regionais
            </p>
          </div>
        ) : (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">
                    Código
                  </th>
                  <th className="px-6 py-4 text-left text-sm font-semibold text-gray-900">
                    Nome
                  </th>
                  <th className="px-6 py-4 text-center text-sm font-semibold text-gray-900">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {regionais.map((regional) => (
                  <tr key={regional.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-gray-600">
                      #{regional.codigoExterno}
                    </td>
                    <td className="px-6 py-4 font-medium text-gray-900">
                      {regional.nome}
                    </td>
                    <td className="px-6 py-4 text-center">
                      {regional.ativa ? (
                        <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-green-100 text-green-700 text-sm">
                          <CheckCircle className="w-4 h-4" />
                          Ativa
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-red-100 text-red-700 text-sm">
                          <XCircle className="w-4 h-4" />
                          Inativa
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
}

export default Regionais;