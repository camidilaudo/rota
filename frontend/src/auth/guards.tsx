import { Navigate, Outlet, useLocation } from 'react-router-dom'
import type { Rol } from '../types/auth'
import { AccessDeniedPage } from '../pages/AccessDeniedPage'
import { useAuth } from './AuthContext'

export function RequireAuth() {
  const { session } = useAuth()
  const location = useLocation()
  if (!session) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />
  }
  return <Outlet />
}

export function RequireRole({ roles }: { roles: readonly Rol[] }) {
  const { hasRole } = useAuth()
  if (!hasRole(roles)) return <AccessDeniedPage />
  return <Outlet />
}
