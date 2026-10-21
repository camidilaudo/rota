import type { Rol } from '../types/auth'
import type {
  AlertaRotacion,
  Categoria,
  Lote,
  ModoNotificacion,
  Pagina,
  Producto,
  ProductoConLotes,
  RutaDiaria,
  Ubicacion,
  Usuario,
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
