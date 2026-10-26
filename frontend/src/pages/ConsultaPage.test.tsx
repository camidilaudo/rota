import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import type { ProductoConLotes } from '../types/domain'

const leche: ProductoConLotes = {
  productoId: 1,
  codigoBarra: '7790001000011',
  nombreProducto: 'Leche Entera 1L',
  categoriaNombre: 'Lácteos',
  stockTotal: 75,
  lotes: [
    { id: 4, productoId: 1, productoCodigoBarra: '7790001000011', productoNombre: 'Leche Entera 1L', cantidad: 50, fechaVencimiento: '2026-11-08', fechaRecepcion: null, ubicacion: 'DEPOSITO', diasHastaVencimiento: 25 },
    { id: 1, productoId: 1, productoCodigoBarra: '7790001000011', productoNombre: 'Leche Entera 1L', cantidad: 25, fechaVencimiento: '2026-10-15', fechaRecepcion: null, ubicacion: 'GONDOLA', diasHastaVencimiento: 1 },
  ],
}

const pagina = (content: ProductoConLotes[]) => ({ content, totalElements: content.length, totalPages: 1, number: 0, size: 10, last: true })

afterEach(() => vi.unstubAllGlobals())

describe('Consulta rápida', () => {
  it('busca por nombre y muestra los lotes activos ordenados por vencimiento', async () => {
    const fetchMock = mockApi({ 'GET /api/busqueda/productos': () => jsonResponse(pagina([leche])) })
    const user = userEvent.setup()
    renderApp('/consulta', 'ROLE_REPOSITOR')

    expect(screen.getByText('Buscá un producto')).toBeInTheDocument()
    await user.type(screen.getByLabelText('Nombre o código de barras'), 'leche')

    expect(await screen.findByText('Leche Entera 1L')).toBeInTheDocument()
    const fechas = screen.getAllByText(/^Vence el /).map((el) => el.textContent)
    expect(fechas).toEqual(['Vence el 15/10/2026', 'Vence el 08/11/2026'])
    expect(screen.getByText('Góndola')).toBeInTheDocument()
    expect(screen.getByText('Depósito')).toBeInTheDocument()
    expect(fetchMock.mock.calls.at(-1)![0]).toContain('query=leche')
    // cada lote se puede abrir desde la consulta, aunque no esté en la ruta del día
    expect(screen.getAllByRole('link', { name: 'Ver lote' }).map((a) => a.getAttribute('href'))).toEqual(['/lotes/1', '/lotes/4'])
  })

  it('avisa cuando no hay resultados', async () => {
    mockApi({ 'GET /api/busqueda/productos': () => jsonResponse(pagina([])) })
    const user = userEvent.setup()
    renderApp('/consulta', 'ROLE_REPOSITOR')

    await user.type(screen.getByLabelText('Nombre o código de barras'), 'zzz')

    expect(await screen.findByText('Sin resultados')).toBeInTheDocument()
  })
})
