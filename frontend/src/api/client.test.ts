import { afterEach, describe, expect, it, vi } from 'vitest'
import { writeSession } from '../auth/sessionStorage'
import { fakeSession, jsonResponse } from '../test/renderApp'
import { ApiError, apiRequest, setUnauthorizedHandler } from './client'

afterEach(() => {
  vi.unstubAllGlobals()
  setUnauthorizedHandler(null)
})

describe('apiRequest', () => {
  it('manda el token como Bearer y arma la query', async () => {
    writeSession(fakeSession('ROLE_DUENO'))
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse([]))
    vi.stubGlobal('fetch', fetchMock)

    await apiRequest('/api/lotes/buscar', { query: { filtro: 'leche', vacio: '' } })

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/lotes/buscar?filtro=leche')
    expect(init.headers.Authorization).toBe('Bearer token-ROLE_DUENO')
  })

  it('expone los errores de validación del backend', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(
        jsonResponse({ status: 400, message: 'Los datos enviados contienen errores', validationErrors: { cantidad: 'Mínimo 1' } }, 400),
      ),
    )

    const err = await apiRequest('/api/lotes', { method: 'POST', body: {} }).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).fieldErrors).toEqual({ cantidad: 'Mínimo 1' })
  })

  it('avisa del 401 salvo en el login', async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    vi.stubGlobal('fetch', vi.fn().mockImplementation(() => Promise.resolve(new Response(null, { status: 401 }))))

    await apiRequest('/api/auth/login', { method: 'POST', body: {}, skipUnauthorizedHandler: true }).catch(() => undefined)
    expect(handler).not.toHaveBeenCalled()

    await apiRequest('/api/productos').catch(() => undefined)
    expect(handler).toHaveBeenCalledTimes(1)
  })

  it('un 403 avisa que no hay permiso y no cierra la sesión', async () => {
    const handler = vi.fn()
    setUnauthorizedHandler(handler)
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ status: 403, message: 'Forbidden' }, 403)))

    const err = (await apiRequest('/api/usuarios', { method: 'POST', body: {} }).catch((e: unknown) => e)) as ApiError

    expect(err.status).toBe(403)
    expect(err.message).toBe('No tenés permiso para esta acción.')
    expect(handler).not.toHaveBeenCalled()
  })

  it('marca como error de red cuando el backend no responde', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    const err = (await apiRequest('/api/productos').catch((e: unknown) => e)) as ApiError

    expect(err.isNetworkError).toBe(true)
  })
})
