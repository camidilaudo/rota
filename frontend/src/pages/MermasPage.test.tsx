import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'

afterEach(() => vi.unstubAllGlobals())

const resumen = {
  desde: '2026-10-01',
  hasta: '2026-10-25',
  perdidaTotal: 3000,
  unidadesTotales: 9,
  registros: 3,
  porCategoria: [
    { clave: '1', nombre: 'Lácteos', unidades: 5, perdidaTotal: 2100, porcentaje: 70 },
    { clave: '2', nombre: 'Panadería', unidades: 4, perdidaTotal: 900, porcentaje: 30 },
  ],
  porCausa: [
    { clave: 'VENCIMIENTO', nombre: 'Vencimiento', unidades: 7, perdidaTotal: 1800, porcentaje: 60 },
    { clave: 'DANIO', nombre: 'Daño', unidades: 2, perdidaTotal: 1200, porcentaje: 40 },
  ],
  porMes: [{ mes: '2026-10', unidades: 9, perdidaTotal: 3000 }],
}

const categorias = [
  { id: 1, nombre: 'Lácteos', diasUmbralAlerta: 7, diasUmbralCritico: 3, modoNotificacion: 'DIARIO', esPiloto: true, activa: true },
  { id: 2, nombre: 'Panadería', diasUmbralAlerta: 3, diasUmbralCritico: 1, modoNotificacion: 'DIARIO', esPiloto: false, activa: true },
]

describe('Panel de merma (HU-25 y HU-26)', () => {
  it('muestra la merma a costo por categoría y causa, y el ahorro contra la línea base', async () => {
    mockApi({
      'GET /api/categorias': () => jsonResponse(categorias),
      'GET /api/operaciones/mermas/resumen': () => jsonResponse(resumen),
      'GET /api/operaciones/mermas/comparacion-linea-base': () =>
        jsonResponse({
          desde: '2026-10-01',
          hasta: '2026-10-25',
          hayLineaBase: true,
          lineaBaseMensual: 6000,
          lineaBasePeriodo: 5000,
          mermaPeriodo: 3000,
          ahorroPesos: 2000,
          ahorroPorcentaje: 40,
          mejoro: true,
          mensaje: 'La merma del período es menor que la línea base: el ahorro estimado es de $2000.00.',
        }),
    })
    renderApp('/merma', 'ROLE_DUENO')

    const porCategoria = (await screen.findByRole('heading', { name: 'Por categoría' })).closest('section')!
    expect(within(porCategoria).getByText('Lácteos')).toBeInTheDocument()
    expect(within(porCategoria).getByText('Panadería')).toBeInTheDocument()
    const porCausa = screen.getByRole('heading', { name: 'Por causa' }).closest('section')!
    expect(within(porCausa).getByText('Vencimiento')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Por mes' })).toBeInTheDocument()
    expect(await screen.findByText(/el ahorro estimado es de/)).toBeInTheDocument()
    expect(screen.getByText(/el ahorro estimado es de \$\s2\.000,00\./)).toBeInTheDocument()
  })

  it('avisa si todavía no hay línea base y enlaza a Configuración', async () => {
    mockApi({
      'GET /api/categorias': () => jsonResponse(categorias),
      'GET /api/operaciones/mermas/resumen': () => jsonResponse(resumen),
      'GET /api/operaciones/mermas/comparacion-linea-base': () =>
        jsonResponse({
          desde: '2026-10-01',
          hasta: '2026-10-25',
          hayLineaBase: false,
          mermaPeriodo: 3000,
          mensaje: 'Todavía no se cargó la línea base de merma mensual del comercio.',
        }),
    })
    renderApp('/merma', 'ROLE_DUENO')

    expect(await screen.findByText(/Todavía no se cargó la línea base/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Cargala en Configuración' })).toHaveAttribute('href', '/configuracion')
  })

  it('filtra por causa y pide el resumen filtrado al backend', async () => {
    const fetchMock = mockApi({
      'GET /api/categorias': () => jsonResponse(categorias),
      'GET /api/operaciones/mermas/resumen': () => jsonResponse(resumen),
      'GET /api/operaciones/mermas/comparacion-linea-base': () => jsonResponse({ desde: '2026-10-01', hasta: '2026-10-25', hayLineaBase: false, mermaPeriodo: 0, mensaje: 'Sin línea base.' }),
    })
    const user = userEvent.setup()
    renderApp('/merma', 'ROLE_DUENO')

    await screen.findByRole('heading', { name: 'Por categoría' })
    await user.selectOptions(screen.getByLabelText('Causa'), 'DANIO')

    await vi.waitFor(() => {
      expect(fetchMock.mock.calls.some(([url]) => url.includes('/mermas/resumen') && url.includes('causa=DANIO'))).toBe(true)
    })
  })

  it('muestra un aviso cuando no hubo bajas en el período', async () => {
    mockApi({
      'GET /api/categorias': () => jsonResponse(categorias),
      'GET /api/operaciones/mermas/resumen': () =>
        jsonResponse({ ...resumen, perdidaTotal: 0, unidadesTotales: 0, registros: 0, porCategoria: [], porCausa: [], porMes: [] }),
      'GET /api/operaciones/mermas/comparacion-linea-base': () => jsonResponse({ desde: '2026-10-01', hasta: '2026-10-25', hayLineaBase: false, mermaPeriodo: 0, mensaje: 'Sin línea base.' }),
    })
    renderApp('/merma', 'ROLE_DUENO')

    expect(await screen.findByText('Sin bajas en el período')).toBeInTheDocument()
  })

  it('el repositor no puede entrar al panel de merma', () => {
    mockApi({})
    renderApp('/merma', 'ROLE_REPOSITOR')
    expect(screen.queryByRole('heading', { name: 'Merma' })).not.toBeInTheDocument()
  })
})
