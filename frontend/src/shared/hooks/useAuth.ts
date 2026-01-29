// src/shared/hooks/useAuth.ts
import { useEffect, useState } from 'react';
import { AuthFacade } from '../../core/facade/AuthFacade';
import type { UserState } from '../../core/state/AuthStore';

export function useAuth() {
  const [state, setState] = useState<UserState>(AuthFacade.getCurrentState());

  useEffect(() => {
    // Subscreve ao BehaviorSubject quando o componente monta
    const subscription = AuthFacade.authState$.subscribe(setState);
    
    // Limpa a subscrição quando o componente desmonta (evita memory leak)
    return () => subscription.unsubscribe();
  }, []);

  return {
    ...state,
    login: AuthFacade.login,
    logout: AuthFacade.logout
  };
}