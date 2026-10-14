import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AppRoutes } from '../App'
import { AuthProvider } from '../auth/AuthProvider'
import { writeSession } from '../auth/sessionStorage'
import type { Rol, Session } from '../types/auth'

export function fakeSession(rol: Rol): Session {
  return {
    token: `token-${rol}`,
    email: rol === 'ROLE_DUENO' ? 'dueno@comercio.com' : 'repositor@comercio.com',
    rol,
    comercioId: 1,
    comercioNombre: 'Almacén Don Carlos',
  }
}

export function renderApp(route: string, rol?: Rol) {
  if (rol) writeSession(fakeSession(rol))
  return render(
    <MemoryRouter initialEntries={[route]}>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </MemoryRouter>,
  )
}

export function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}
