import type { Rol } from '../types/auth'
import type {
  AlertaRotacion,
  Categoria,
  ComparacionEscenarios,
  Comercio,
  DescuentoAplicado,
  DescuentoSugerido,
  HistorialAccion,
  Lote,
  LoteRiesgo,
  Merma,
  ModoNotificacion,
  Pagina,
  Producto,
  ProductoConLotes,
  RutaDiaria,
  Ubicacion,
  Usuario,
  VentaRegistrada,
} from '../types/domain'
import { apiRequest } from './client'

// Riesgo (MotorRiesgoController)
export const getRutaDiaria = (signal?: AbortSignal) => apiRequest<RutaDiaria>('/api/riesgo/ruta-diaria', { signal })

// Productos (ProductoController)
export const getProductos = (signal?: AbortSignal) => apiRequest<Producto[]>('/api/productos', { signal })

export const getProductoPorCodigo = (codigoBarra: string, signal?: AbortSignal) =>
  apiRequest<Producto>(`/api/productos/codigo/${encodeURIComponent(codigoBarra)}`, { signal })

export const crearProducto = (body: {
  codigoBarra: string
  nombre: string
  precioVenta: number
  costo: number
  categoriaId: number
}) => apiRequest<Producto>('/api/productos', { method: 'POST', body })

// Lotes (LoteController)
export const registrarLote = (body: { productoId: number; cantidad: number; fechaVencimiento: string; ubicacion: Ubicacion }) =>
  apiRequest<Lote>('/api/lotes', { method: 'POST', body })

// Rotación (RotacionController)
export const getLotesFefo = (productoId: number, signal?: AbortSignal) =>
  apiRequest<Lote[]>(`/api/rotacion/fefo/producto/${productoId}`, { signal })

export const getProximoLoteAReponer = (productoId: number, signal?: AbortSignal) =>
  apiRequest<Lote>(`/api/rotacion/fefo/reponer/${productoId}`, { signal })

export const getAlertasRotacion = (signal?: AbortSignal) =>
  apiRequest<AlertaRotacion[]>('/api/rotacion/alertas-rotacion', { signal })

export const trasladarAGondola = (body: { loteOrigenId: number; cantidadATrasladar: number }) =>
  apiRequest<Lote>('/api/rotacion/trasladar', { method: 'POST', body })

// Búsqueda (BusquedaController)
export const buscarProductos = (params: { query?: string; ubicacion?: Ubicacion; page?: number; size?: number }, signal?: AbortSignal) =>
  apiRequest<Pagina<ProductoConLotes>>('/api/busqueda/productos', { query: params, signal })

// Categorías (CategoriaController)
export const getCategorias = (signal?: AbortSignal) => apiRequest<Categoria[]>('/api/categorias', { signal })

export const crearCategoria = (body: {
  nombre: string
  diasUmbralAlerta: number
  diasUmbralCritico: number
  modoNotificacion: ModoNotificacion
}) => apiRequest<Categoria>('/api/categorias', { method: 'POST', body })

export const actualizarConfiguracionCategoria = (id: number, params: { diasUmbral?: number; modoNotificacion?: ModoNotificacion }) =>
  apiRequest<Categoria>(`/api/categorias/${id}/configuracion`, { method: 'PATCH', query: params })

// Usuarios (UsuarioController)
export const crearUsuario = (body: { nombre: string; email: string; password: string; rol: Rol }) =>
  apiRequest<Usuario>('/api/usuarios', { method: 'POST', body })

// Riesgo: todos los lotes con su semáforo y su cobertura por velocidad de venta (HU-15)
export const getEvaluacionGeneral = (signal?: AbortSignal) => apiRequest<LoteRiesgo[]>('/api/riesgo/evaluacion-general', { signal })

// Operaciones (OperacionStockController)
export const registrarVenta = (body: { productoId: number; cantidad: number }) =>
  apiRequest<VentaRegistrada>('/api/operaciones/ventas', { method: 'POST', body }) // HU-20

export const getDescuentoSugerido = (loteId: number, signal?: AbortSignal) =>
  apiRequest<DescuentoSugerido>(`/api/operaciones/lotes/${loteId}/descuento-sugerido`, { signal }) // HU-17 y HU-19

export const compararEscenarios = (loteId: number, signal?: AbortSignal) =>
  apiRequest<ComparacionEscenarios>(`/api/operaciones/lotes/${loteId}/comparar-escenarios`, { signal }) // HU-18

export const getHistorialLote = (loteId: number, signal?: AbortSignal) =>
  apiRequest<HistorialAccion[]>(`/api/operaciones/lotes/${loteId}/historial`, { signal }) // HU-22

export const aplicarDescuento = (body: { loteId: number; porcentajeDescuento: number; motivo?: string }) =>
  apiRequest<DescuentoAplicado>('/api/operaciones/descuentos', { method: 'POST', body })

export const registrarMerma = (body: { loteId: number; cantidad: number; motivo: string }) =>
  apiRequest<Merma>('/api/operaciones/mermas', { method: 'POST', body })

// Comercio (ComercioController, solo dueño): piso de margen (HU-19)
export const getComercio = (id: number, signal?: AbortSignal) => apiRequest<Comercio>(`/api/comercios/${id}`, { signal })

export const actualizarMargenMinimo = (id: number, margenMinimoPorcentaje: number) =>
  apiRequest<Comercio>(`/api/comercios/${id}/margen-minimo`, { method: 'PATCH', body: { margenMinimoPorcentaje } })
