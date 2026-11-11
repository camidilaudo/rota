import type { Ubicacion, CausaMerma } from './types/domain'

const moneyFormatter = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' })
const numberFormatter = new Intl.NumberFormat('es-AR')

export function formatMoney(value: number): string {
  return moneyFormatter.format(value)
}

export function formatNumber(value: number): string {
  return numberFormatter.format(value)
}

/** "2026-10-15" -> "15/10/2026", sin pasar por Date para no correr la fecha por zona horaria. */
export function formatDate(isoDate: string): string {
  const [y, m, d] = isoDate.slice(0, 10).split('-')
  return `${d}/${m}/${y}`
}

/** "2026-10-21T14:35:10" -> "21/10/2026 14:35". */
export function formatDateTime(isoDateTime: string): string {
  return `${formatDate(isoDateTime)} ${isoDateTime.slice(11, 16)}`
}

export function describeDaysLeft(days: number): string {
  if (days < -1) return `Venció hace ${formatNumber(-days)} días`
  if (days === -1) return 'Venció ayer'
  if (days === 0) return 'Vence hoy'
  if (days === 1) return 'Vence mañana'
  return `Vence en ${formatNumber(days)} días`
}

export function formatPercent(value: number): string {
  return `${formatNumber(value)}%`
}

export function formatUnits(cantidad: number): string {
  return `${formatNumber(cantidad)} ${cantidad === 1 ? 'unidad' : 'unidades'}`
}

export const UBICACION_LABEL: Record<Ubicacion, string> = {
  GONDOLA: 'Góndola',
  DEPOSITO: 'Depósito',
  HELADERA: 'Heladera',
  FREEZER: 'Freezer',
}

/** HU-23: nombre de cada causa de baja. */
export const CAUSA_MERMA_LABEL: Record<CausaMerma, string> = {
  VENCIMIENTO: 'Vencimiento',
  DANIO: 'Daño',
  ROTURA: 'Rotura',
}

/** Fecha local de hoy en formato yyyy-mm-dd, para el min de los inputs de fecha. */
export function todayIso(): string {
  const now = new Date()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${m}-${d}`
}

/** Misma fecha un año antes (yyyy-mm-dd). El 29/02 pasa al 28/02 del año anterior. */
export function unAnioAntes(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number)
  const fecha = new Date(y - 1, m - 1, d)
  if (fecha.getMonth() !== m - 1) fecha.setDate(28)
  return `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}-${String(fecha.getDate()).padStart(2, '0')}`
}
