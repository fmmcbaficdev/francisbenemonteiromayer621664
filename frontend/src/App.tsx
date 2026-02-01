// ==========================================
// APP - MAIN ROUTES
// ==========================================

import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { PrivateRoute, Loading } from './components';

// Lazy load pages
const Login = lazy(() => import('./pages/Login'));
const Artistas = lazy(() => import('./pages/Artistas'));
const ArtistaForm = lazy(() => import('./pages/ArtistaForm'));
const ArtistaDetalhes = lazy(() => import('./pages/ArtistaDetalhes'));
const Albuns = lazy(() => import('./pages/Albuns'));
const AlbumForm = lazy(() => import('./pages/AlbumForm'));
const Regionais = lazy(() => import('./pages/Regionais'));

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Suspense fallback={<Loading fullScreen message="Carregando..." />}>
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<Login />} />
            
            {/* Protected Routes */}
            <Route path="/artistas" element={
              <PrivateRoute><Artistas /></PrivateRoute>
            } />
            <Route path="/artistas/novo" element={
              <PrivateRoute><ArtistaForm /></PrivateRoute>
            } />
            <Route path="/artistas/:id" element={
              <PrivateRoute><ArtistaDetalhes /></PrivateRoute>
            } />
            <Route path="/artistas/:id/editar" element={
              <PrivateRoute><ArtistaForm /></PrivateRoute>
            } />
            
            <Route path="/albuns" element={
              <PrivateRoute><Albuns /></PrivateRoute>
            } />
            <Route path="/albuns/novo" element={
              <PrivateRoute><AlbumForm /></PrivateRoute>
            } />
            <Route path="/albuns/:id/editar" element={
              <PrivateRoute><AlbumForm /></PrivateRoute>
            } />
            
            <Route path="/regionais" element={
              <PrivateRoute><Regionais /></PrivateRoute>
            } />
            
            {/* Redirect */}
            <Route path="/" element={<Navigate to="/artistas" replace />} />
            <Route path="*" element={<Navigate to="/artistas" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;