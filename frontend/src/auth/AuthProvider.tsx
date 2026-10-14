import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { login as loginRequest } from '../api/auth'
import { setUnauthorizedHandler } from '../api/client'
import type { Rol, Session } from '../types/auth'
import { AuthContext, type AuthContextValue } from './AuthContext'
import { clearSession, readSession, writeSession } from './sessionStorage'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(() => readSession())
  const [sessionExpired, setSessionExpired] = useState(false)

  const login = useCallback(async (email: string, password: string) => {
    const data = await loginRequest(email, password)
    writeSession(data)
    setSessionExpired(false)
    setSession(data)
  }, [])

  const logout = useCallback(() => {
    clearSession()
    setSessionExpired(false)
    setSession(null)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(() => {
      clearSession()
      setSessionExpired(true)
      setSession(null)
    })
    return () => setUnauthorizedHandler(null)
  }, [])

  const hasRole = useCallback((roles: readonly Rol[]) => session !== null && roles.includes(session.rol), [session])

  const value = useMemo<AuthContextValue>(
    () => ({ session, sessionExpired, login, logout, hasRole }),
    [session, sessionExpired, login, logout, hasRole],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
