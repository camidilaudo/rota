import { createContext, useContext } from 'react'
import type { Rol, Session } from '../types/auth'

export interface AuthContextValue {
  session: Session | null
  /** true si la última sesión terminó por un 401 del backend. */
  sessionExpired: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  hasRole: (roles: readonly Rol[]) => boolean
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth tiene que usarse dentro de AuthProvider')
  return ctx
}
