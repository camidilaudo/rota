import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import type { AlertaRotacion } from '../types/domain'

afterEach(() => vi.unstubAllGlobals())

function alerta(loteDepositoId: number, loteGondolaId: number, productoNombre: string): AlertaRotacion {
  return {
    productoId: loteDepositoId,
    productoNombre,
    productoCodigoBarra: '779000100001' + loteDepositoId,
    loteGondolaId,
    fechaVencimientoGondola: '2026-12-20',
    diasHastaVencimientoGondola: 38,
    loteDepositoId,
    fechaVencimientoDeposito: '2026-11-30',
    diasHastaVencimientoDeposito: 18,
    diasDiferencia: 20,
    recomendacionAccion: 'Reponer el lote de depósito primero',
  }
}

describe('Campana de alertas en el encabezado', () => {
  it('cuenta las alertas por lote de depósito y lleva a cada lote', async () => {
    mockApi({
      'GET /api/rotacion/alertas-rotacion': () =>
        jsonResponse([alerta(7, 1, 'Leche Entera 1L'), alerta(7, 2, 'Leche Entera 1L'), alerta(9, 3, 'Yogur Frutilla')]),
      'GET /api/productos': () => jsonResponse([]),
    })
    const user = userEvent.setup()
    renderApp('/ventas', 'ROLE_REPOSITOR')

    const [campana] = await screen.findAllByRole('button', { name: 'Alertas: 2 alertas' })
    await user.click(campana)

    const panel = document.getElementById(campana.getAttribute('aria-controls')!)!
    expect(within(panel).getByRole('link', { name: /Leche Entera 1L/ })).toHaveAttribute('href', '/lotes/7')
    expect(within(panel).getByRole('link', { name: /Yogur Frutilla/ })).toHaveAttribute('href', '/lotes/9')
  })

  it('sin alertas no muestra contador', async () => {
    mockApi({ 'GET /api/rotacion/alertas-rotacion': () => jsonResponse([]), 'GET /api/productos': () => jsonResponse([]) })
    renderApp('/ventas', 'ROLE_REPOSITOR')

    expect((await screen.findAllByRole('button', { name: 'Alertas: sin alertas' })).length).toBeGreaterThan(0)
  })
})
