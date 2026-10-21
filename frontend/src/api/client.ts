import { readToken } from '../auth/sessionStorage'

/** Cuerpo de error del backend (ErrorResponseDTO). */
interface ErrorBody {
  status?: number
  error?: string
  message?: string
  validationErrors?: Record<string, string>
}

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>
  /** true cuando no hubo respuesta del backend (red caída, backend apagado). */
  readonly isNetworkError: boolean

  constructor(message: string, status: number, fieldErrors: Record<string, string> = {}, isNetworkError = false) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
    this.isNetworkError = isNetworkError
  }
}

type QueryValue = string | number | boolean | undefined | null

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: Record<string, QueryValue>
  /** Para el login: un 401 ahí no significa sesión vencida. */
  skipUnauthorizedHandler?: boolean
  signal?: AbortSignal
}

let unauthorizedHandler: (() => void) | null = null

export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler
}

const NETWORK_MESSAGE = 'No pudimos conectarnos con el servidor. Revisá tu conexión e intentá de nuevo.'
export const FORBIDDEN_MESSAGE = 'No tenés permiso para esta acción.'

function buildUrl(path: string, query?: Record<string, QueryValue>): string {
  if (!query) return path
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') params.append(key, String(value))
  }
  const qs = params.toString()
  return qs ? `${path}?${qs}` : path
}

async function parseBody(response: Response): Promise<unknown> {
  const text = await response.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, query, skipUnauthorizedHandler = false, signal } = options
  const headers: Record<string, string> = { Accept: 'application/json' }
  const token = readToken()
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let response: Response
  try {
    response = await fetch(buildUrl(path, query), {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal,
    })
  } catch (err) {
    if (err instanceof DOMException && err.name === 'AbortError') throw err
    throw new ApiError(NETWORK_MESSAGE, 0, {}, true)
  }

  const data = await parseBody(response)

  if (response.ok) return data as T

  // 401 = sesión inválida o vencida: se cierra. 403 = sesión válida sin permiso: se mantiene.
  if (response.status === 401 && !skipUnauthorizedHandler) {
    unauthorizedHandler?.()
  }
  if (response.status === 403) {
    throw new ApiError(FORBIDDEN_MESSAGE, 403)
  }

  const errorBody = (data ?? {}) as ErrorBody
  // El proxy de Vite responde sin cuerpo JSON cuando el backend está apagado.
  const backendUnreachable = data === null && response.status >= 500
  if (backendUnreachable) throw new ApiError(NETWORK_MESSAGE, response.status, {}, true)

  throw new ApiError(
    errorBody.message ?? 'Algo salió mal. Intentá de nuevo.',
    response.status,
    errorBody.validationErrors ?? {},
  )
}
