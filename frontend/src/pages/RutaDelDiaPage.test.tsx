import { screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, renderApp } from '../test/renderApp'
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
})
