import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import type { ComparacionEscenarios, DescuentoSugerido, HistorialAccion, LoteRiesgo } from '../types/domain'

const lote: LoteRiesgo = {
  loteId: 5,
  productoId: 2,
  productoCodigoBarra: '7790001000028',
  productoNombre: 'Yogur Frutilla 500g',
  categoriaNombre: 'Lácteos',
  cantidad: 30,
  fechaVencimiento: '2026-10-30',
  ubicacion: 'DEPOSITO',
  diasHastaVencimiento: 8,
  diasUmbralCritico: 3,
  estadoRiesgo: 'ROJO',
  modoNotificacion: 'DIARIO',
  valorEnRiesgo: 15000,
  rotacionDiariaVentas: 2,
  diasCobertura: 15,
  riesgoPorVelocidad: true,
}

const sugerido: DescuentoSugerido = {
  loteId: 5,
  productoNombre: 'Yogur Frutilla 500g',
  cantidad: 30,
  diasHastaVencimiento: 8,
  velocidadVentaDiariaActual: 2,
  velocidadVentaDiariaNecesaria: 3.75,
  porcentajeDescuentoSugerido: 35,
  margenMinimoPorcentaje: 20,
  porcentajeDescuentoMaximoPorMargen: 25,
  porcentajeDescuentoRecomendado: 25,
  descuentoInsuficientePorMargen: true,
  mensaje: '',
}

const escenarios: ComparacionEscenarios = {
  loteId: 5,
  productoNombre: 'Yogur Frutilla 500g',
  cantidad: 30,
  diasHastaVencimiento: 8,
  escenarioSinDescuento: {
    porcentajeDescuento: 0,
    precioFinal: 900,
    unidadesEstimadasVendidas: 16,
    unidadesEstimadasDescartadas: 14,
    margenGananciaEstimado: 4800,
    perdidaPorMermaEstimada: 7000,
    margenTotalProyectado: -2200,
  },
  escenarioConDescuento: {
    porcentajeDescuento: 25,
    precioFinal: 675,
    unidadesEstimadasVendidas: 28,
    unidadesEstimadasDescartadas: 2,
    margenGananciaEstimado: 4200,
    perdidaPorMermaEstimada: 1000,
    margenTotalProyectado: 3200,
  },
  recomendacion: 'CON_DESCUENTO',
  mensaje: '',
}

const historial: HistorialAccion[] = [
  { id: 1, loteId: 5, tipoAccion: 'TRASLADO', motivo: 'Reposición a góndola', usuarioNombre: 'Juan Repositor', usuarioEmail: 'repositor@comercio.com', fechaHora: '2026-10-20T09:15:00' },
]

afterEach(() => vi.unstubAllGlobals())

const rutasBase = () => ({
  'GET /api/riesgo/evaluacion-general': () => jsonResponse([lote]),
  'GET /api/operaciones/lotes/5/descuento-sugerido': () => jsonResponse(sugerido),
  'GET /api/operaciones/lotes/5/comparar-escenarios': () => jsonResponse(escenarios),
  'GET /api/operaciones/lotes/5/historial': () => jsonResponse(historial),
})

describe('Detalle de lote', () => {
  it('muestra la cobertura por velocidad de venta (HU-15)', async () => {
    mockApi(rutasBase())
    renderApp('/lotes/5', 'ROLE_DUENO')

    expect(await screen.findByText('A este ritmo tarda 15 días en venderse y vence en 8: no llega a tiempo.')).toBeInTheDocument()
    expect(screen.getByText('2 u. por día')).toBeInTheDocument()
  })

  it('el dueño ve el descuento sugerido con el piso de margen (HU-17 y HU-19) y los escenarios (HU-18)', async () => {
    mockApi(rutasBase())
    renderApp('/lotes/5', 'ROLE_DUENO')

    expect(await screen.findByText('Descuento recomendado')).toBeInTheDocument()
    expect(screen.getByText(/Piso de margen del comercio: 20%/)).toBeInTheDocument()
    expect(screen.getByText(/perfora el piso de margen/)).toBeInTheDocument()
    expect(await screen.findByText('Conviene aplicar el descuento: deja más margen.')).toBeInTheDocument()
    expect(screen.getByText('Con 25% de descuento')).toBeInTheDocument()
  })

  it('el repositor no ve márgenes ni escenarios', async () => {
    const fetchMock = mockApi({ ...rutasBase(), 'GET /api/riesgo/evaluacion-general': () => jsonResponse([{ ...lote, valorEnRiesgo: undefined }]) })
    renderApp('/lotes/5', 'ROLE_REPOSITOR')

    expect(await screen.findByText('Descuento recomendado')).toBeInTheDocument()
    expect(screen.queryByText(/Piso de margen del comercio/)).not.toBeInTheDocument()
    expect(screen.queryByText('Con descuento o sin descuento')).not.toBeInTheDocument()
    expect(screen.queryByText('Valor en riesgo')).not.toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([url]) => url.includes('comparar-escenarios'))).toBe(false)
  })

  it('aplica el descuento recomendado y recarga el lote (HU-17)', async () => {
    const fetchMock = mockApi({
      ...rutasBase(),
      'POST /api/operaciones/descuentos': () =>
        jsonResponse({ loteId: 5, productoNombre: 'Yogur Frutilla 500g', precioOriginal: 900, porcentajeDescuento: 25, precioConDescuento: 675, mensaje: 'Descuento del 25% aplicado correctamente al lote' }),
    })
    const user = userEvent.setup()
    renderApp('/lotes/5', 'ROLE_DUENO')

    const campo = await screen.findByLabelText('Descuento a aplicar (%)')
    expect(campo).toHaveValue('25')
    await user.click(screen.getByRole('button', { name: 'Aplicar descuento al lote' }))

    const confirmacion = await screen.findByText(/Descuento del 25% aplicado correctamente al lote/)
    expect(confirmacion).toBeInTheDocument()
    // la confirmación queda dentro del formulario, debajo del botón, y no arriba de la página
    const formulario = confirmacion.closest('form')!
    const boton = within(formulario).getByRole('button', { name: 'Aplicar descuento al lote' })
    expect(boton.compareDocumentPosition(confirmacion) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
    const post = fetchMock.mock.calls.find(([url, init]) => url.includes('/descuentos') && init?.method === 'POST')!
    expect(JSON.parse(post[1]!.body as string)).toEqual({ loteId: 5, porcentajeDescuento: 25 })
  })

  it('no deja aplicar un descuento fuera de rango', async () => {
    const fetchMock = mockApi(rutasBase())
    const user = userEvent.setup()
    renderApp('/lotes/5', 'ROLE_DUENO')

    const campo = await screen.findByLabelText('Descuento a aplicar (%)')
    await user.clear(campo)
    await user.type(campo, '95')
    await user.click(screen.getByRole('button', { name: 'Aplicar descuento al lote' }))

    expect(screen.getByText('Ingresá un descuento entre 5% y 90%.')).toBeInTheDocument()
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false)
  })

  it('lista el historial de acciones del lote (HU-22)', async () => {
    mockApi(rutasBase())
    renderApp('/lotes/5', 'ROLE_DUENO')

    expect(await screen.findByText('Traslado')).toBeInTheDocument()
    expect(screen.getByText(/Juan Repositor · 20\/10\/2026 09:15/)).toBeInTheDocument()
  })

  it('da de baja unidades con su motivo y las suma al historial (HU-22)', async () => {
    const fetchMock = mockApi({
      ...rutasBase(),
      'POST /api/operaciones/mermas': () => jsonResponse({ id: 1, productoId: 2, productoNombre: 'Yogur Frutilla 500g', cantidadMermada: 3, motivo: 'Deteriorado' }, 201),
    })
    const user = userEvent.setup()
    renderApp('/lotes/5', 'ROLE_REPOSITOR')

    await screen.findByText('Dar de baja unidades')
    const cantidad = screen.getByLabelText('Unidades a dar de baja')
    await user.clear(cantidad)
    await user.type(cantidad, '3')
    await user.selectOptions(screen.getByLabelText('Motivo'), 'Deteriorado')
    await user.click(screen.getByRole('button', { name: 'Dar de baja' }))

    expect(await screen.findByText('Listo: se dieron de baja 3 unidades (deteriorado).')).toBeInTheDocument()
    const post = fetchMock.mock.calls.find(([url, init]) => url.includes('/mermas') && init?.method === 'POST')!
    expect(JSON.parse(post[1]!.body as string)).toEqual({ loteId: 5, cantidad: 3, motivo: 'Deteriorado' })
  })

  it('avisa si el lote no existe', async () => {
    mockApi({ 'GET /api/riesgo/evaluacion-general': () => jsonResponse([lote]) })
    renderApp('/lotes/99', 'ROLE_DUENO')

    expect(await screen.findByText('No encontramos este lote')).toBeInTheDocument()
  })
})
