import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'

afterEach(() => vi.unstubAllGlobals())

describe('Línea base de merma (HU-26)', () => {
  it('el dueño carga la merma mensual que tenía antes de Rotá', async () => {
    const fetchMock = mockApi({
      'GET /api/categorias': () => jsonResponse([]),
      'GET /api/comercios/1': () => jsonResponse({ id: 1, nombre: 'Almacén Don Carlos', margenMinimoPorcentaje: 20, lineaBaseMermaMensual: null }),
      'PATCH /api/comercios/1/linea-base-merma': () =>
        jsonResponse({ id: 1, nombre: 'Almacén Don Carlos', margenMinimoPorcentaje: 20, lineaBaseMermaMensual: 45000 }),
    })
    const user = userEvent.setup()
    renderApp('/configuracion', 'ROLE_DUENO')

    const campo = await screen.findByLabelText('Merma mensual antes de Rotá ($)')
    expect(campo).toHaveValue('')
    await user.type(campo, '45000')
    await user.click(within(campo.closest('form')!).getByRole('button', { name: 'Guardar' }))

    expect(await screen.findByText(/la línea base de merma es de/)).toBeInTheDocument()
    const patch = fetchMock.mock.calls.find(([, init]) => init?.method === 'PATCH')!
    expect(JSON.parse(patch[1]!.body as string)).toEqual({ lineaBaseMermaMensual: 45000 })
  })

  it('rechaza un monto vacío o negativo', async () => {
    mockApi({
      'GET /api/categorias': () => jsonResponse([]),
      'GET /api/comercios/1': () => jsonResponse({ id: 1, nombre: 'Almacén Don Carlos', margenMinimoPorcentaje: 20 }),
    })
    const user = userEvent.setup()
    renderApp('/configuracion', 'ROLE_DUENO')

    const campo = await screen.findByLabelText('Merma mensual antes de Rotá ($)')
    await user.click(within(campo.closest('form')!).getByRole('button', { name: 'Guardar' }))

    expect(screen.getByText('Ingresá un monto mayor o igual a 0.')).toBeInTheDocument()
  })
})
