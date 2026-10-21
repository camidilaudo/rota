import type { Rol } from './auth'

export type Ubicacion = 'DEPOSITO' | 'GONDOLA' | 'HELADERA' | 'FREEZER'
export type ModoNotificacion = 'DIARIO' | 'CADA_2_DIAS' | 'SEMANAL' | 'PANTALLA'
export type EstadoRiesgo = 'VERDE' | 'AMARILLO' | 'ROJO' | 'CRITICO_VENCIDO'

/** LoteRiesgoResponseDTO */
export interface LoteRiesgo {
  loteId: number
  productoId: number
  productoCodigoBarra: string
  productoNombre: string
  categoriaNombre: string | null
  cantidad: number
  fechaVencimiento: string
  ubicacion: Ubicacion
  diasHastaVencimiento: number
  diasUmbralCritico: number | null
  estadoRiesgo: EstadoRiesgo
  modoNotificacion: ModoNotificacion | null
  /** Solo llega para el dueño. */
  valorEnRiesgo?: number
}

/** RutaDiariaResponseDTO */
export interface RutaDiaria {
  totalLotesAtencion: number
  totalLotesVencidos: number
  /** Solo llega para el dueño. */
  valorTotalEnRiesgo?: number
  lotesPriorizados: LoteRiesgo[]
}

/** ProductoResponseDTO */
export interface Producto {
  id: number
  codigoBarra: string
  nombre: string
  precioVenta: number
  costo?: number
  margenGanancia?: number
  porcentajeMargen?: number
  categoriaId: number
  categoriaNombre: string
  diasUmbralCriticoCategoria: number | null
}

/** LoteResponseDTO */
export interface Lote {
  id: number
  productoId: number
  productoCodigoBarra: string
  productoNombre: string
  cantidad: number
  fechaVencimiento: string
  fechaRecepcion: string | null
  ubicacion: Ubicacion
  diasHastaVencimiento: number
}

/** AlertaRotacionResponseDTO */
export interface AlertaRotacion {
  productoId: number
  productoNombre: string
  productoCodigoBarra: string
  loteGondolaId: number
  fechaVencimientoGondola: string
  diasHastaVencimientoGondola: number
  loteDepositoId: number
  fechaVencimientoDeposito: string
  diasHastaVencimientoDeposito: number
  diasDiferencia: number
  recomendacionAccion: string
}

/** BusquedaProductoLotesResponseDTO */
export interface ProductoConLotes {
  productoId: number
  codigoBarra: string
  nombreProducto: string
  categoriaNombre: string | null
  stockTotal: number
  lotes: Lote[]
}

export interface Pagina<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  last: boolean
}

/** CategoriaResponseDTO */
export interface Categoria {
  id: number
  nombre: string
  diasUmbralAlerta: number
  diasUmbralCritico: number
  modoNotificacion: ModoNotificacion
  esPiloto: boolean
  activa: boolean
}

/** UsuarioResponseDTO */
export interface Usuario {
  id: number
  nombre: string
  email: string
  rol: Rol
  comercioId: number
}
