import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiRequest } from '../api/client'
import { fakeSession, jsonResponse, renderApp } from '../test/renderApp'
import { readSession, readToken } from './sessionStorage'

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('sesión', () => {
  it('guarda el token al iniciar sesión y entra a la ruta del día', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(fakeSession('ROLE_REPOSITOR')))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    renderApp('/login')

    await user.type(screen.getByLabelText('Email'), 'repositor@comercio.com')
    await user.type(screen.getByLabelText('Contraseña'), '123456')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(await screen.findByRole('heading', { name: 'Ruta del día' })).toBeInTheDocument()
    expect(readToken()).toBe('token-ROLE_REPOSITOR')
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/auth/login')
    expect(JSON.parse(init.body)).toEqual({ email: 'repositor@comercio.com', password: '123456' })
  })

  it('con credenciales inválidas (401) avisa sin tocar la sesión', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ status: 401, message: 'Email o contraseña incorrectos' }, 401)))
    const user = userEvent.setup()
    renderApp('/login')

    await user.type(screen.getByLabelText('Email'), 'dueno@comercio.com')
    await user.type(screen.getByLabelText('Contraseña'), 'mal')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(await screen.findByText('Email o contraseña incorrectos.')).toBeInTheDocument()
    expect(screen.queryByText('Tu sesión se cerró. Ingresá de nuevo para seguir.')).not.toBeInTheDocument()
    expect(readSession()).toBeNull()
  })

  it('muestra un error si el login falla y no guarda nada', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ message: 'Ocurrió un error interno' }, 500)))
    const user = userEvent.setup()
    renderApp('/login')

    await user.type(screen.getByLabelText('Email'), 'dueno@comercio.com')
    await user.type(screen.getByLabelText('Contraseña'), 'mal')
    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(await screen.findByText('No pudimos iniciar sesión. Revisá el email y la contraseña.')).toBeInTheDocument()
    expect(readSession()).toBeNull()
  })

  it('valida los campos vacíos sin llamar al backend', async () => {
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    renderApp('/login')

    await user.click(screen.getByRole('button', { name: 'Ingresar' }))

    expect(screen.getByText('Ingresá tu email.')).toBeInTheDocument()
    expect(screen.getByText('Ingresá tu contraseña.')).toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('borra el token al cerrar sesión', async () => {
    const user = userEvent.setup()
    renderApp('/', 'ROLE_DUENO')

    await user.click(screen.getAllByRole('button', { name: 'Cerrar sesión' })[0])

    expect(await screen.findByRole('button', { name: 'Ingresar' })).toBeInTheDocument()
    expect(readToken()).toBeNull()
  })

  it('una respuesta 401 cierra la sesión y vuelve al login', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 401 })))
    renderApp('/consulta', 'ROLE_REPOSITOR')
    expect(screen.getByRole('heading', { name: 'Consulta' })).toBeInTheDocument()

    await expect(apiRequest('/api/productos')).rejects.toMatchObject({ status: 401 })

    await waitFor(() => expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument())
    expect(screen.getByText('Tu sesión se cerró. Ingresá de nuevo para seguir.')).toBeInTheDocument()
    expect(readToken()).toBeNull()
  })
})
