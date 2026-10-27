import { act, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import { leerMediciones } from '../scan/medicionEscaneo'
import type { Producto } from '../types/domain'

type Callback = (result: { getText: () => string } | undefined, error: unknown, controls: { stop: () => void }) => void

const zx = vi.hoisted(() => ({
  callback: null as Callback | null,
  stop: vi.fn(),
  error: null as unknown,
}))

vi.mock('@zxing/browser', () => ({
  BrowserMultiFormatReader: class {
    decodeFromConstraints(_constraints: unknown, _video: unknown, callback: Callback) {
      zx.callback = callback
      return zx.error ? Promise.reject(zx.error) : Promise.resolve({ stop: zx.stop })
    }
  },
}))

const leche: Producto = {
  id: 1,
  codigoBarra: '7790001000019',
  nombre: 'Leche Entera 1L',
  precioVenta: 1200,
  categoriaId: 1,
  categoriaNombre: 'Lácteos',
  diasUmbralCriticoCategoria: 3,
}

function leer(codigo: string) {
  act(() => zx.callback!({ getText: () => codigo }, undefined, { stop: zx.stop }))
}

beforeEach(() => {
  zx.callback = null
  zx.error = null
  zx.stop.mockClear()
  Object.defineProperty(navigator, 'mediaDevices', { value: { getUserMedia: vi.fn() }, configurable: true })
})

afterEach(() => {
  vi.unstubAllGlobals()
  Object.defineProperty(window, 'isSecureContext', { value: undefined, configurable: true })
})

async function abrirCamara() {
  const user = userEvent.setup()
  await user.click(screen.getByRole('button', { name: 'Escanear con la cámara' }))
  await waitFor(() => expect(zx.callback).not.toBeNull())
  return user
}

describe('Recepción con escaneo (HU-27)', () => {
  it('lee el código, identifica el producto y sigue con la carga del lote', async () => {
    const fetchMock = mockApi({ 'GET /api/productos/codigo/7790001000019': () => jsonResponse(leche) })
    renderApp('/recepcion', 'ROLE_REPOSITOR')
    await abrirCamara()
    expect(screen.getByRole('dialog', { name: 'Escanear código de barras' })).toBeInTheDocument()

    leer('7790001000019')

    expect(await screen.findByText('Leche Entera 1L')).toBeInTheDocument()
    expect(screen.getByLabelText('Vence el')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(zx.stop).toHaveBeenCalled()
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('ignora lecturas repetidas del mismo escaneo', async () => {
    const fetchMock = mockApi({ 'GET /api/productos/codigo/7790001000019': () => jsonResponse(leche) })
    renderApp('/recepcion', 'ROLE_REPOSITOR')
    await abrirCamara()
    const callback = zx.callback!

    act(() => {
      callback({ getText: () => '7790001000019' }, undefined, { stop: zx.stop })
      callback({ getText: () => '7790001000019' }, undefined, { stop: zx.stop })
      callback({ getText: () => '7790001000019' }, undefined, { stop: zx.stop })
    })

    await screen.findByText('Leche Entera 1L')
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('si el código leído no existe, sigue el flujo de carga manual', async () => {
    mockApi({ 'GET /api/productos/codigo/7790009999999': () => jsonResponse({ status: 404, message: 'Producto no encontrado' }, 404) })
    renderApp('/recepcion', 'ROLE_REPOSITOR')
    await abrirCamara()

    leer('7790009999999')

    expect(await screen.findByText(/El código 7790009999999 no está cargado/)).toBeInTheDocument()
  })

  it('sin permiso de cámara avisa y se puede seguir tipeando', async () => {
    zx.error = new DOMException('Permission denied', 'NotAllowedError')
    mockApi({ 'GET /api/productos/codigo/7790001000019': () => jsonResponse(leche) })
    renderApp('/recepcion', 'ROLE_REPOSITOR')
    const user = await abrirCamara()

    expect(await screen.findByText(/No hay permiso para usar la cámara/)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Tipear el código' }))
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()

    await user.type(screen.getByLabelText('Código de barras'), '7790001000019{Enter}')
    expect(await screen.findByText('Leche Entera 1L')).toBeInTheDocument()
  })

  it('cerrar la cámara libera el video', async () => {
    renderApp('/recepcion', 'ROLE_REPOSITOR')
    const user = await abrirCamara()

    await user.click(screen.getByRole('button', { name: 'Cerrar cámara' }))

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(zx.stop).toHaveBeenCalled()
  })

  it('sin HTTPS explica por qué no abre la cámara', async () => {
    Object.defineProperty(window, 'isSecureContext', { value: false, configurable: true })
    renderApp('/recepcion', 'ROLE_REPOSITOR')
    const user = userEvent.setup()

    await user.click(screen.getByRole('button', { name: 'Escanear con la cámara' }))

    expect(await screen.findByText(/La cámara solo funciona con conexión segura \(HTTPS\)/)).toBeInTheDocument()
    expect(zx.callback).toBeNull()
  })
})

describe('Medición del escaneo (NF-02)', () => {
  it('registra los dos tiempos de cada identificación', async () => {
    mockApi({ 'GET /api/productos/codigo/7790001000019': () => jsonResponse(leche) })
    renderApp('/recepcion', 'ROLE_REPOSITOR')
    await abrirCamara()

    leer('7790001000019')
    await screen.findByText('Leche Entera 1L')

    const [m] = leerMediciones()
    expect(m.codigo).toBe('7790001000019')
    expect(m.resultado).toBe('encontrado')
    expect(m.lecturaAIdentificacionMs).toBeGreaterThanOrEqual(0)
    expect(m.aperturaAIdentificacionMs).toBeGreaterThanOrEqual(m.lecturaAIdentificacionMs)
  })

  it('el código tipeado no se mide', async () => {
    mockApi({ 'GET /api/productos/codigo/7790001000019': () => jsonResponse(leche) })
    const user = userEvent.setup()
    renderApp('/recepcion', 'ROLE_REPOSITOR')

    await user.type(screen.getByLabelText('Código de barras'), '7790001000019{Enter}')
    await screen.findByText('Leche Entera 1L')

    expect(leerMediciones()).toEqual([])
  })
})
