export type Rol = 'ROLE_DUENO' | 'ROLE_REPOSITOR'

/** Respuesta de POST /api/auth/login (AuthResponseDTO). */
export interface AuthResponse {
  token: string
  email: string
  rol: Rol
  comercioId: number | null
  comercioNombre: string | null
}

export type Session = AuthResponse
