import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'

const productos = [
  { id: 2, codigoBarra: '7790001000028', nombre: 'Yogur Frutilla 500g', precioVenta: 900, categoriaId: 1, categoriaNombre: 'Lácteos', diasUmbralCriticoCategoria: 3 },
]

afterEach(() => vi.unstubAllGlobals())

describe('Ventas (HU-20)', () => {
  it('registra una venta y muestra de qué lotes salió', async () => {
    const fetchMock = mockApi({
      'GET /api/productos': () => jsonResponse(productos),
      'POST /api/operaciones/ventas': () =>
        jsonResponse(
          {
            productoId: 2,
            productoNombre: 'Yogur Frutilla 500g',
            cantidadVendida: 6,
            lotesAfectados: [
              { loteId: 4, cantidadDescontada: 4, cantidadRestanteEnLote: 0 },
              { loteId: 7, cantidadDescontada: 2, cantidadRestanteEnLote: 13 },
            ],
            rotacionDiariaActualizada: 1.5,
            mensaje: 'Venta registrada',
          },
          201,
        ),
    })
    const user = userEvent.setup()
    renderApp('/ventas', 'ROLE_REPOSITOR')

    await screen.findByRole('option', { name: 'Yogur Frutilla 500g' })
    await user.selectOptions(screen.getByLabelText('Producto'), '2')
    const cantidad = screen.getByLabelText('Unidades vendidas')
    await user.clear(cantidad)
    await user.type(cantidad, '6')
    await user.click(screen.getByRole('button', { name: 'Registrar venta' }))

    expect(await screen.findByText(/6 unidades de Yogur Frutilla 500g/)).toBeInTheDocument()
    expect(screen.getByText(/Lote 4: 4 unidades/)).toBeInTheDocument()
    expect(screen.getByText(/Quedan 13 unidades/)).toBeInTheDocument()
    expect(screen.getByText(/Se agotó el lote 4/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'reponé la góndola' })).toHaveAttribute('href', '/reponer')
    const post = fetchMock.mock.calls.find(([url, init]) => url.includes('/api/operaciones/ventas') && init?.method === 'POST')!
    expect(JSON.parse(post[1]!.body as string)).toEqual({ productoId: 2, cantidad: 6 })
  })

  it('pide elegir un producto antes de registrar', async () => {
    const fetchMock = mockApi({ 'GET /api/productos': () => jsonResponse(productos) })
    const user = userEvent.setup()
    renderApp('/ventas', 'ROLE_DUENO')

    await screen.findByRole('option', { name: 'Yogur Frutilla 500g' })
    await user.click(screen.getByRole('button', { name: 'Registrar venta' }))

    expect(screen.getByText('Elegí un producto.')).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false)
  })
})
