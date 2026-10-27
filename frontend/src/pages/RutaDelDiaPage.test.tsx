import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import type { LoteRiesgo, RutaDiaria } from '../types/domain'

const lote = (over: Partial<LoteRiesgo>): LoteRiesgo => ({
  loteId: 1,
  productoId: 1,
  productoCodigoBarra: '7790001000011',
  productoNombre: 'Leche Entera 1L',
  categoriaNombre: 'Lácteos',
  cantidad: 25,
  fechaVencimiento: '2026-10-15',
  ubicacion: 'GONDOLA',
  diasHastaVencimiento: 1,
  diasUmbralCritico: 3,
  estadoRiesgo: 'ROJO',
  modoNotificacion: 'DIARIO',
  ...over,
})

function mockRuta(ruta: RutaDiaria) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(ruta)))
}

afterEach(() => vi.unstubAllGlobals())

describe('Ruta del día', () => {
  it('muestra el semáforo con texto y agrupa por ubicación', async () => {
    mockRuta({
      totalLotesAtencion: 2,
      totalLotesVencidos: 1,
      lotesPriorizados: [
        lote({ loteId: 1, ubicacion: 'DEPOSITO', estadoRiesgo: 'CRITICO_VENCIDO', diasHastaVencimiento: -2, productoNombre: 'Yogur' }),
        lote({ loteId: 2, ubicacion: 'GONDOLA' }),
      ],
    })
    renderApp('/', 'ROLE_REPOSITOR')

    expect(await screen.findByText('Vencido')).toBeInTheDocument()
    expect(screen.getByText('Rojo')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Depósito/ })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Góndola/ })).toBeInTheDocument()
    expect(screen.getByText('Venció hace 2 días')).toBeInTheDocument()
  })

  it('el repositor no ve el valor en riesgo', async () => {
    mockRuta({ totalLotesAtencion: 1, totalLotesVencidos: 0, lotesPriorizados: [lote({})] })
    renderApp('/', 'ROLE_REPOSITOR')

    await screen.findByText('Leche Entera 1L')
    expect(screen.queryByText('Valor en riesgo')).not.toBeInTheDocument()
  })

  it('el dueño ve el valor total y por lote', async () => {
    mockRuta({
      totalLotesAtencion: 1,
      totalLotesVencidos: 0,
      valorTotalEnRiesgo: 21250,
      lotesPriorizados: [lote({ valorEnRiesgo: 21250 })],
    })
    renderApp('/', 'ROLE_DUENO')

    await screen.findByText('Leche Entera 1L')
    expect(screen.getAllByText('Valor en riesgo')).toHaveLength(2)
  })

  it('sin lotes muestra el estado vacío', async () => {
    mockRuta({ totalLotesAtencion: 0, totalLotesVencidos: 0, lotesPriorizados: [] })
    renderApp('/', 'ROLE_REPOSITOR')

    expect(await screen.findByText('Todo en orden')).toBeInTheDocument()
  })

  it('si falla muestra el error con reintento', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))
    renderApp('/', 'ROLE_REPOSITOR')

    expect(await screen.findByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
  })

  it('marca un lote como revisado y lo saca de la lista de pendientes (HU-16)', async () => {
    let revisado = false
    const rutaCon = (lotes: LoteRiesgo[]): RutaDiaria => ({
      totalLotesAtencion: lotes.length,
      totalLotesVencidos: 0,
      valorTotalEnRiesgo: 0,
      lotesPriorizados: lotes,
    })
    const fetchMock = mockApi({
      'GET /api/riesgo/ruta-diaria': () => jsonResponse(revisado ? rutaCon([]) : rutaCon([lote({ loteId: 7, productoNombre: 'Yogur Frutilla 500g' })])),
      'POST /api/operaciones/lotes/7/revisado': () => {
        revisado = true
        return jsonResponse({ loteId: 7, productoNombre: 'Yogur Frutilla 500g', revisadoEn: '2026-10-25T10:00:00', revisadoPor: 'Juan' })
      },
    })
    const user = userEvent.setup()
    renderApp('/', 'ROLE_REPOSITOR')

    await screen.findByText('Yogur Frutilla 500g')
    await user.click(screen.getByRole('button', { name: 'Marcar como revisado' }))

    expect(await screen.findByText('Todo en orden')).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url, init]) => url.includes('/lotes/7/revisado') && init?.method === 'POST')).toBe(true)
  })

  it('si otro usuario ya lo revisó hoy (409) avisa quién y no deja reintentar (HU-16)', async () => {
    mockApi({
      'GET /api/riesgo/ruta-diaria': () =>
        jsonResponse({ totalLotesAtencion: 1, totalLotesVencidos: 0, lotesPriorizados: [lote({ loteId: 7, productoNombre: 'Yogur Frutilla 500g' })] }),
      'POST /api/operaciones/lotes/7/revisado': () =>
        jsonResponse({ status: 409, error: 'Conflict', message: 'Este lote ya fue revisado hoy por Carlos Dueño a las 08:05' }, 409),
    })
    const user = userEvent.setup()
    renderApp('/', 'ROLE_REPOSITOR')

    await screen.findByText('Yogur Frutilla 500g')
    await user.click(screen.getByRole('button', { name: 'Marcar como revisado' }))

    expect(await screen.findByText('Este lote ya fue revisado hoy por Carlos Dueño a las 08:05')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Marcar como revisado' })).toBeDisabled()
    // la sesión sigue abierta: un 409 no es un 401
    expect(screen.queryByRole('button', { name: 'Ingresar' })).not.toBeInTheDocument()
  })
})
