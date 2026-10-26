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
  /** HU-15: unidades que se venden por día, en promedio. */
  rotacionDiariaVentas?: number
  /** HU-15: días que tarda en agotarse el lote a ese ritmo (null si el producto nunca se vendió). */
  diasCobertura?: number | null
  /** HU-15: true si, al ritmo actual, el lote no se vende antes de vencer. */
  riesgoPorVelocidad?: boolean
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

export type TipoAccion = 'DESCUENTO' | 'TRASLADO' | 'BAJA'

/** LoteAfectadoVentaDTO */
export interface LoteAfectadoVenta {
  loteId: number
  cantidadDescontada: number
  cantidadRestanteEnLote: number
}

/** VentaResponseDTO (HU-20) */
export interface VentaRegistrada {
  productoId: number
  productoNombre: string
  cantidadVendida: number
  lotesAfectados: LoteAfectadoVenta[]
  rotacionDiariaActualizada: number
  mensaje: string
}

/** DescuentoSugeridoResponseDTO (HU-17 y HU-19) */
export interface DescuentoSugerido {
  loteId: number
  productoNombre: string
  cantidad: number
  diasHastaVencimiento: number
  velocidadVentaDiariaActual: number
  velocidadVentaDiariaNecesaria: number
  porcentajeDescuentoSugerido: number
  margenMinimoPorcentaje: number
  porcentajeDescuentoMaximoPorMargen: number
  porcentajeDescuentoRecomendado: number
  descuentoInsuficientePorMargen: boolean
  mensaje: string
}

/** EscenarioProyectadoDTO */
export interface EscenarioProyectado {
  porcentajeDescuento: number
  precioFinal: number
  unidadesEstimadasVendidas: number
  unidadesEstimadasDescartadas: number
  margenGananciaEstimado: number
  perdidaPorMermaEstimada: number
  margenTotalProyectado: number
}

/** ComparacionEscenariosResponseDTO (HU-18) */
export interface ComparacionEscenarios {
  loteId: number
  productoNombre: string
  cantidad: number
  diasHastaVencimiento: number
  escenarioSinDescuento: EscenarioProyectado
  escenarioConDescuento: EscenarioProyectado
  recomendacion: 'CON_DESCUENTO' | 'SIN_DESCUENTO'
  mensaje: string
}

/** DescuentoAplicadoResponseDTO */
export interface DescuentoAplicado {
  loteId: number
  productoNombre: string
  precioOriginal: number
  porcentajeDescuento: number
  precioConDescuento: number
  mensaje: string
}

/** MermaResponseDTO. El costo y la pérdida solo se muestran al dueño. */
export interface Merma {
  id: number
  productoId: number
  productoNombre: string
  cantidadMermada: number
  motivo: string
  perdidaTotal?: number
}

/** HistorialAccionResponseDTO (HU-22) */
export interface HistorialAccion {
  id: number
  loteId: number
  tipoAccion: TipoAccion
  motivo: string | null
  usuarioNombre: string
  usuarioEmail: string
  fechaHora: string
}

/** ComercioResponseDTO (solo lo que usa el front) */
export interface Comercio {
  id: number
  nombre: string
  margenMinimoPorcentaje: number
}
