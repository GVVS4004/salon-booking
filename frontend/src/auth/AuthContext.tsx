import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { AuthResponse } from '../api/types';
import { getToken, setToken } from '../api/client';

interface AuthUser {
  role: 'CUSTOMER' | 'ADMIN';
  name: string;
  email: string;
  customerId?: number;
}

interface AuthContextValue {
  user: AuthUser | null;
  isAdmin: boolean;
  isCustomer: boolean;
  login: (res: AuthResponse) => void;
  logout: () => void;
}

const USER_KEY = 'salon.user';
const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw || !getToken()) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadUser);

  useEffect(() => {
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
    else localStorage.removeItem(USER_KEY);
  }, [user]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAdmin: user?.role === 'ADMIN',
      isCustomer: user?.role === 'CUSTOMER',
      login: (res: AuthResponse) => {
        setToken(res.token);
        setUser({
          role: res.role as 'CUSTOMER' | 'ADMIN',
          name: res.name,
          email: res.email,
          customerId: res.customer?.id,
        });
      },
      logout: () => {
        setToken(null);
        setUser(null);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
