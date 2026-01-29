// ==========================================
// STORAGE UTILITIES - JWT TOKEN MANAGEMENT
// ==========================================

const ACCESS_TOKEN_KEY = 'seplag_access_token';
const REFRESH_TOKEN_KEY = 'seplag_refresh_token';
const USER_KEY = 'seplag_user';

// ==========================================
// TOKEN FUNCTIONS
// ==========================================

export const getAccessToken = (): string | null => {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
};

export const setAccessToken = (token: string): void => {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
};

export const getRefreshToken = (): string | null => {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
};

export const setRefreshToken = (token: string): void => {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
};

export const clearTokens = (): void => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
};

// ==========================================
// USER FUNCTIONS
// ==========================================

export const getStoredUser = (): { username: string; nome: string } | null => {
  const user = localStorage.getItem(USER_KEY);
  return user ? JSON.parse(user) : null;
};

export const setStoredUser = (user: { username: string; nome: string }): void => {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
};

// ==========================================
// AUTH CHECK
// ==========================================

export const isAuthenticated = (): boolean => {
  const token = getAccessToken();
  if (!token) return false;
  
  try {
    // Decodificar JWT para verificar expiração
    const payload = JSON.parse(atob(token.split('.')[1]));
    const expiration = payload.exp * 1000; // Converter para milliseconds
    return Date.now() < expiration;
  } catch {
    return false;
  }
};

// ==========================================
// TOKEN EXPIRATION
// ==========================================

export const getTokenExpiration = (): Date | null => {
  const token = getAccessToken();
  if (!token) return null;
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return new Date(payload.exp * 1000);
  } catch {
    return null;
  }
};

export const isTokenExpiringSoon = (thresholdMinutes: number = 1): boolean => {
  const expiration = getTokenExpiration();
  if (!expiration) return true;
  
  const threshold = thresholdMinutes * 60 * 1000;
  return expiration.getTime() - Date.now() < threshold;
};