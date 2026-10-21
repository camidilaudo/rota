import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { jsonResponse, mockApi, renderApp } from '../test/renderApp'
import type { Producto } from '../types/domain'

const leche: Producto = {
  id: 1,
  codigoBarra: '7790001000011',
  nombre: 'Leche Entera 1L',
  precioVenta: 1200,
  categoriaId: 1,
  categoriaNombre: 'Lácteos',
  diasUmbralCriticoCategoria: 3,
}

const noEncontrado = () => jsonResponse({ status: 404, message: 'Producto no encontrado' }, 404)

afterEach(() => vi.unstubAllGlobals())

describe('Recepción de lote', () => {
  it('busca por código, carga el lote y confirma', async () => {
    const fetchMock = mockApi({
      'GET /api/productos/codigo/7790001000011': () => jsonResponse(leche),
      'POST /api/lotes': (_u, init) => {
        const body = JSON.parse(String(init.body))
        return jsonResponse({ id: 9, productoId: 1, productoCodigoBarra: leche.codigoBarra, productoNombre: leche.nombre, fechaRecepcion: null, diasHastaVencimiento: 30, ...body }, 201)
      },
    })
    const user = userEvent.setup()
    renderApp('/recepcion', 'ROLE_REPOSITOR')

    await user.type(screen.getByLabelText('Código de barras'), '7790001000011')
    await user.click(screen.getByRole('button', { name: 'Buscar producto' }))
    expect(await screen.findByText('Leche Entera 1L')).toBeInTheDocument()

    await user.type(screen.getByLabelText('Vence el'), '2099-12-31')
    await user.type(screen.getByLabelText('Cantidad'), '24')
    await user.click(screen.getByRole('button', { name: 'Góndola' }))
    await user.click(screen.getByRole('button', { name: 'Registrar lote' }))

    expect(await screen.findByText('Lote registrado')).toBeInTheDocument()
    expect(screen.getByText(/24 unidades · vence el 31\/12\/2099 · Góndola/)).toBeInTheDocument()
    const post = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST')!
    expect(JSON.parse(String(post[1]!.body))).toEqual({ productoId: 1, cantidad: 24, fechaVencimiento: '2099-12-31', ubicacion: 'GONDOLA' })
  })

  it('valida fecha y cantidad antes de enviar', async () => {
    const fetchMock = mockApi({ 'GET /api/productos/codigo/7790001000011': () => jsonResponse(leche) })
    const user = userEvent.setup()
    renderApp('/recepcion', 'ROLE_REPOSITOR')

    await user.type(screen.getByLabelText('Código de barras'), '7790001000011{Enter}')
    await screen.findByText('Leche Entera 1L')
    await user.click(screen.getByRole('button', { name: 'Registrar lote' }))

    expect(screen.getByText('Elegí la fecha de vencimiento.')).toBeInTheDocument()
    expect(screen.getByText('Ingresá una cantidad de al menos 1.')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('si el código no existe, el repositor no puede crear el producto', async () => {
    mockApi({ 'GET /api/productos/codigo/123': noEncontrado })
    const user = userEvent.setup()
    renderApp('/recepcion', 'ROLE_REPOSITOR')

    await user.type(screen.getByLabelText('Código de barras'), '123{Enter}')

    expect(await screen.findByText(/Solo el dueño puede dar de alta productos nuevos/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Crear producto' })).not.toBeInTheDocument()
  })

  it('si el código no existe, el dueño lo crea y sigue con el lote', async () => {
    const fetchMock = mockApi({
      'GET /api/productos/codigo/123': noEncontrado,
      'GET /api/categorias': () =>
        jsonResponse([{ id: 1, nombre: 'Lácteos', diasUmbralAlerta: 7, diasUmbralCritico: 3, modoNotificacion: 'DIARIO', esPiloto: true, activa: true }]),
      'POST /api/productos': () => jsonResponse({ ...leche, id: 7, codigoBarra: '123', nombre: 'Manteca 200g' }, 201),
    })
    const user = userEvent.setup()
    renderApp('/recepcion', 'ROLE_DUENO')

    await user.type(screen.getByLabelText('Código de barras'), '123{Enter}')
    await user.type(await screen.findByLabelText('Nombre del producto'), 'Manteca 200g')
    await user.type(screen.getByLabelText('Precio de venta'), '1500')
    await user.type(screen.getByLabelText('Costo'), '900')
    await user.selectOptions(await screen.findByLabelText('Categoría'), 'Lácteos')
    await user.click(screen.getByRole('button', { name: 'Crear producto' }))

    expect(await screen.findByText('Producto creado. Ahora cargá el lote.')).toBeInTheDocument()
    expect(screen.getByText('Manteca 200g')).toBeInTheDocument()
    const post = fetchMock.mock.calls.find(([u, init]) => u === '/api/productos' && init?.method === 'POST')!
    expect(JSON.parse(String(post[1]!.body))).toEqual({ codigoBarra: '123', nombre: 'Manteca 200g', precioVenta: 1500, costo: 900, categoriaId: 1 })
  })
})
