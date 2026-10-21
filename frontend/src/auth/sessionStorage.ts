import type { Rol, Session } from '../types/auth'

const STORAGE_KEY = 'rota.session'
const ROLES: Rol[] = ['ROLE_DUENO', 'ROLE_REPOSITOR']

function isSession(value: unknown): value is Session {
  if (typeof value !== 'object' || value === null) return false
  const v = value as Record<string, unknown>
  return typeof v.token === 'string' && v.token.length > 0 && typeof v.email === 'string' && ROLES.includes(v.rol as Rol)
}

export function readSession(): Session | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed: unknown = JSON.parse(raw)
    return isSession(parsed) ? parsed : null
  } catch {
    return null
  }
}

export function writeSession(session: Session): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export function clearSession(): void {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Sin acceso a storage no hay nada que borrar.
  }
}

export function readToken(): string | null {
  return readSession()?.token ?? null
}
