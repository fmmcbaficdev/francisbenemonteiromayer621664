// ==========================================
// LOGIN PAGE - POLÍCIA CIVIL MT
// ==========================================

import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Shield, User, Lock, Eye, EyeOff, AlertCircle, LogIn } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Loading } from '../components/Loading';

export function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: { pathname?: string } })?.from?.pathname || '/artistas';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      await login(username, password);
      navigate(from, { replace: true });
    } catch (err: unknown) {
      const error = err as Error;
      setError(error.message || 'Erro ao fazer login');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <Loading fullScreen message="Carregando..." />;
  }

  return (
    <div className="min-h-screen gradient-header flex items-center justify-center p-4">
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-5">
        <div className="absolute inset-0" style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23d4af37' fill-opacity='0.4'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
        }} />
      </div>

      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">
        {/* Header Dourado */}
        <div className="bg-gradient-to-r from-gold-400 to-gold-500 p-6 text-center">
          <div className="inline-flex items-center justify-center w-20 h-20 bg-primary-600 rounded-full mb-4 shadow-lg">
            <Shield className="w-10 h-10 text-gold-400" />
          </div>
          <h1 className="text-2xl font-bold text-primary-900">SEPLAG</h1>
          <p className="text-primary-700 font-medium">Polícia Civil do Mato Grosso</p>
        </div>

        {/* Form */}
        <div className="p-8">
          <h2 className="text-xl font-bold text-primary-600 text-center mb-6">
            Sistema de Artistas e Álbuns
          </h2>

          {/* Error Alert */}
          {error && (
            <div className="mb-6 p-4 bg-accent-50 border-2 border-accent-200 rounded-lg flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-accent-500 flex-shrink-0 mt-0.5" />
              <p className="text-accent-700 text-sm font-medium">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Username */}
            <div>
              <label htmlFor="username" className="label">
                Usuário
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-primary-400">
                  <User className="w-5 h-5" />
                </div>
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="input w-full py-3 pr-4 pl-11 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 bg-white"
                  placeholder="Digite seu usuário"
                  required
                  autoComplete="username"
                  autoFocus
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="label">
                Senha
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-primary-400">
                  <Lock className="w-5 h-5" />
                </div>
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="input w-full py-3 pl-11 pr-12 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 bg-white"
                  placeholder="Digite sua senha"
                  required
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 w-12 h-full flex items-center justify-center text-primary-400 hover:text-primary-600 focus:outline-none focus:ring-0"
                  aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
                >
                  {showPassword ? <EyeOff className="w-5 h-5 shrink-0" /> : <Eye className="w-5 h-5 shrink-0" />}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting || !username || !password}
              className="w-full btn-gold py-3 text-lg flex items-center justify-center gap-2 mt-6"
            >
              {isSubmitting ? (
                <>
                  <Loading size="sm" />
                  Entrando...
                </>
              ) : (
                <>
                  <LogIn className="w-5 h-5 shrink-0" />
                  Entrar no Sistema
                </>
              )}
            </button>
          </form>

          {/* Help Text */}
          <div className="mt-6 p-4 bg-primary-50 rounded-lg border border-primary-200">
            <p className="text-center text-sm text-primary-600">
              <strong>Credenciais de teste:</strong><br />
              Usuário: <code className="bg-primary-100 px-2 py-0.5 rounded">admin</code><br />
              Senha: <code className="bg-primary-100 px-2 py-0.5 rounded">admin123</code>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;