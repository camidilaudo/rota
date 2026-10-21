import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import type { Categoria } from '../types/domain'

const lacteos: Categoria = { id: 1, nombre: 'Lácteos', diasUmbralAlerta: 7, diasUmbralCritico: 3, modoNotificacion: 'DIARIO', esPiloto: true, activa: true }

afterEach(() => vi.unstubAllGlobals())

describe('Configuración', () => {
  it('edita el umbral crítico y el modo de aviso', async () => {
    let categorias = [lacteos]
    const fetchMock = mockApi({
      'GET /api/categorias': () => jsonResponse(categorias),
      'PATCH /api/categorias/1/configuracion': () => {
        categorias = [{ ...lacteos, diasUmbralCritico: 2, modoNotificacion: 'SEMANAL' }]
        return jsonResponse(categorias[0])
      },
    })
    const user = userEvent.setup()
    renderApp('/configuracion', 'ROLE_DUENO')

    await user.click(await screen.findByRole('button', { name: 'Editar Lácteos' }))
    const input = screen.getByLabelText('Umbral crítico (días antes de vencer)')
    await user.clear(input)
    await user.type(input, '9')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))
    expect(screen.getByText('No puede superar el umbral de alerta (7 días).')).toBeInTheDocument()

    await user.clear(input)
    await user.type(input, '2')
    await user.selectOptions(screen.getByLabelText('Modo de aviso'), 'Semanal')
    await user.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(await screen.findByText('Guardamos los cambios de Lácteos.')).toBeInTheDocument()
    expect(await screen.findByText('Alerta: 7 días · Crítico: 2 días')).toBeInTheDocument()
    const patch = fetchMock.mock.calls.find(([, init]) => init?.method === 'PATCH')!
    expect(patch[0]).toBe('/api/categorias/1/configuracion?diasUmbral=2&modoNotificacion=SEMANAL')
  })

  it('crea una categoría nueva', async () => {
    let categorias = [lacteos]
    const fetchMock = mockApi({
      'GET /api/categorias': () => jsonResponse(categorias),
      'POST /api/categorias': (_u, init) => {
        const body = JSON.parse(String(init.body))
        const nueva = { id: 4, esPiloto: false, activa: true, ...body }
        categorias = [...categorias, nueva]
        return jsonResponse(nueva, 201)
      },
    })
    const user = userEvent.setup()
    renderApp('/configuracion', 'ROLE_DUENO')

    await user.click(await screen.findByRole('button', { name: 'Agregar categoría' }))
    const form = screen.getByRole('heading', { name: 'Nueva categoría' }).closest('form')!
    await user.type(within(form).getByLabelText('Nombre'), 'Bebidas')
    await user.type(within(form).getByLabelText('Alerta (días)'), '30')
    await user.type(within(form).getByLabelText('Crítico (días)'), '10')
    await user.click(within(form).getByRole('button', { name: 'Crear' }))

    expect(await screen.findByText('Creamos la categoría Bebidas.')).toBeInTheDocument()
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')!
    expect(JSON.parse(String(post[1]!.body))).toEqual({ nombre: 'Bebidas', diasUmbralAlerta: 30, diasUmbralCritico: 10, modoNotificacion: 'DIARIO' })
  })

  it('da de alta un usuario con rol', async () => {
    const fetchMock = mockApi({
      'GET /api/categorias': () => jsonResponse([lacteos]),
      'POST /api/usuarios': (_u, init) => {
        const body = JSON.parse(String(init.body))
        return jsonResponse({ id: 3, nombre: body.nombre, email: body.email, rol: body.rol, comercioId: 1 }, 201)
      },
    })
    const user = userEvent.setup()
    renderApp('/configuracion', 'ROLE_DUENO')

    const form = (await screen.findByRole('button', { name: 'Crear usuario' })).closest('form')!
    await user.type(within(form).getByLabelText('Nombre'), 'Ana')
    await user.type(within(form).getByLabelText('Email'), 'ana@comercio.com')
    await user.type(within(form).getByLabelText('Contraseña inicial'), '123')
    await user.click(within(form).getByRole('button', { name: 'Crear usuario' }))
    expect(screen.getByText('Usá al menos 6 caracteres.')).toBeInTheDocument()

    await user.type(within(form).getByLabelText('Contraseña inicial'), '456')
    await user.click(within(form).getByRole('button', { name: 'Crear usuario' }))

    expect(await screen.findByText(/Creamos a Ana \(Encargado\)/)).toBeInTheDocument()
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')!
    expect(JSON.parse(String(post[1]!.body))).toEqual({ nombre: 'Ana', email: 'ana@comercio.com', password: '123456', rol: 'ROLE_REPOSITOR' })
  })
})
