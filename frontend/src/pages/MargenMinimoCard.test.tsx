import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'

afterEach(() => vi.unstubAllGlobals())

describe('Piso de margen (HU-19)', () => {
  it('el dueño lo cambia desde Configuración', async () => {
    const fetchMock = mockApi({
      'GET /api/categorias': () => jsonResponse([]),
      'GET /api/comercios/1': () => jsonResponse({ id: 1, nombre: 'Almacén Don Carlos', margenMinimoPorcentaje: 20 }),
      'PATCH /api/comercios/1/margen-minimo': () => jsonResponse({ id: 1, nombre: 'Almacén Don Carlos', margenMinimoPorcentaje: 30 }),
    })
    const user = userEvent.setup()
    renderApp('/configuracion', 'ROLE_DUENO')

    const campo = await screen.findByLabelText('Piso de margen (%)')
    expect(campo).toHaveValue('20')
    await user.clear(campo)
    await user.type(campo, '30')
    await user.click(within(campo.closest('form')!).getByRole('button', { name: 'Guardar' }))

    expect(await screen.findByText(/por debajo de 30%/)).toBeInTheDocument()
    const patch = fetchMock.mock.calls.find(([, init]) => init?.method === 'PATCH')!
    expect(JSON.parse(patch[1]!.body as string)).toEqual({ margenMinimoPorcentaje: 30 })
  })

  it('rechaza un margen mayor al 95%', async () => {
    mockApi({
      'GET /api/categorias': () => jsonResponse([]),
      'GET /api/comercios/1': () => jsonResponse({ id: 1, nombre: 'Almacén Don Carlos', margenMinimoPorcentaje: 20 }),
    })
    const user = userEvent.setup()
    renderApp('/configuracion', 'ROLE_DUENO')

    const campo = await screen.findByLabelText('Piso de margen (%)')
    await user.clear(campo)
    await user.type(campo, '99')
    await user.click(within(campo.closest('form')!).getByRole('button', { name: 'Guardar' }))

    expect(screen.getByText('Ingresá un margen entre 0% y 95%.')).toBeInTheDocument()
  })
})
