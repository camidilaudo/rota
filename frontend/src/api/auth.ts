import type { AuthResponse } from '../types/auth'
import { apiRequest } from './client'

export function login(email: string, password: string): Promise<AuthResponse> {
  return apiRequest<AuthResponse>('/api/auth/login', {
    method: 'POST',
    body: { email, password },
    skipUnauthorizedHandler: true,
  })
}
