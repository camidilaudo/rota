import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import type { AlertaRotacion, Lote } from '../types/domain'

const productos = [
  { id: 2, codigoBarra: '7790001000028', nombre: 'Yogur Frutilla 500g', precioVenta: 900, categoriaId: 1, categoriaNombre: 'Lácteos', diasUmbralCriticoCategoria: 3 },
]

const lote = (over: Partial<Lote>): Lote => ({
  id: 2,
  productoId: 2,
  productoCodigoBarra: '7790001000028',
  productoNombre: 'Yogur Frutilla 500g',
  cantidad: 15,
  fechaVencimiento: '2026-10-19',
  fechaRecepcion: null,
  ubicacion: 'DEPOSITO',
  diasHastaVencimiento: 5,
  ...over,
})

const alerta = (loteGondolaId: number): AlertaRotacion => ({
  productoId: 2,
  productoNombre: 'Yogur Frutilla 500g',
  productoCodigoBarra: '7790001000028',
  loteGondolaId,
  fechaVencimientoGondola: '2026-11-20',
  diasHastaVencimientoGondola: 37,
  loteDepositoId: 2,
  fechaVencimientoDeposito: '2026-10-19',
  diasHastaVencimientoDeposito: 5,
  diasDiferencia: 32,
  recomendacionAccion: '',
})

afterEach(() => vi.unstubAllGlobals())

describe('Reponer', () => {
  it('agrupa las alertas por lote de depósito y lleva al traslado FEFO', async () => {
    let fefo = [lote({ ubicacion: 'GONDOLA', id: 9, fechaVencimiento: '2026-10-17', diasHastaVencimiento: 3, cantidad: 4 }), lote({}), lote({ id: 6, ubicacion: 'GONDOLA', fechaVencimiento: '2026-11-20' })]
    const fetchMock = mockApi({
      'GET /api/productos': () => jsonResponse(productos),
      'GET /api/rotacion/alertas-rotacion': () => jsonResponse([alerta(6), alerta(8)]),
      'GET /api/rotacion/fefo/producto/2': () => jsonResponse(fefo),
      'POST /api/rotacion/trasladar': () => {
        fefo = fefo.map((l) => (l.id === 2 ? { ...l, cantidad: 5 } : l))
        return jsonResponse(lote({ id: 10, cantidad: 10, ubicacion: 'GONDOLA' }))
      },
    })
    const user = userEvent.setup()
    renderApp('/reponer', 'ROLE_REPOSITOR')

    const botones = await screen.findAllByRole('button', { name: 'Reponer este producto' })
    expect(botones).toHaveLength(1)
    expect(screen.getByText(/los 2 de góndola/)).toBeInTheDocument()
    await user.click(botones[0])

    const form = (await screen.findByText('Bajá este lote primero')).closest('form')!
    expect(within(form).getByText('Vence el 19/10/2026')).toBeInTheDocument()
    const input = within(form).getByLabelText('Unidades a trasladar')
    expect(input).toHaveValue('15')

    await user.clear(input)
    await user.type(input, '10')
    await user.click(within(form).getByRole('button', { name: 'Trasladar a góndola' }))

    expect(await screen.findByText(/10 unidades pasaron a góndola/)).toBeInTheDocument()
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')!
    expect(JSON.parse(String(post[1]!.body))).toEqual({ loteOrigenId: 2, cantidadATrasladar: 10 })
    expect(await screen.findByText(/5 unidades en depósito/)).toBeInTheDocument()
  })

  it('no deja trasladar más de lo que hay', async () => {
    const fetchMock = mockApi({
      'GET /api/productos': () => jsonResponse(productos),
      'GET /api/rotacion/alertas-rotacion': () => jsonResponse([]),
      'GET /api/rotacion/fefo/producto/2': () => jsonResponse([lote({})]),
    })
    const user = userEvent.setup()
    renderApp('/reponer', 'ROLE_REPOSITOR')

    expect(await screen.findByText('Sin alertas')).toBeInTheDocument()
    await user.selectOptions(await screen.findByLabelText('Producto'), 'Yogur Frutilla 500g')
    const input = await screen.findByLabelText('Unidades a trasladar')
    await user.clear(input)
    await user.type(input, '99')
    await user.click(screen.getByRole('button', { name: 'Trasladar a góndola' }))

    expect(screen.getByText('El lote tiene 15 unidades.')).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false)
  })

  it('sin lotes en depósito lo explica', async () => {
    mockApi({
      'GET /api/productos': () => jsonResponse(productos),
      'GET /api/rotacion/alertas-rotacion': () => jsonResponse([]),
      'GET /api/rotacion/fefo/producto/2': () => jsonResponse([lote({ ubicacion: 'GONDOLA' })]),
    })
    const user = userEvent.setup()
    renderApp('/reponer', 'ROLE_REPOSITOR')

    await user.selectOptions(await screen.findByLabelText('Producto'), 'Yogur Frutilla 500g')
    expect(await screen.findByText(/No hay lotes en depósito para bajar/)).toBeInTheDocument()
  })
})
