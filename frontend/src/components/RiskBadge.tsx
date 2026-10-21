import type { EstadoRiesgo, Ubicacion } from '../types/domain'
import { UBICACION_LABEL } from '../format'

const RIESGO: Record<EstadoRiesgo, { label: string; className: string; dot: string }> = {
  VERDE: { label: 'Verde', className: 'bg-emerald-50 text-emerald-900 ring-emerald-300', dot: 'bg-emerald-600' },
  AMARILLO: { label: 'Amarillo', className: 'bg-amber-50 text-amber-900 ring-amber-300', dot: 'bg-amber-500' },
  ROJO: { label: 'Rojo', className: 'bg-red-50 text-red-800 ring-red-300', dot: 'bg-red-600' },
  CRITICO_VENCIDO: { label: 'Vencido', className: 'bg-red-800 text-white ring-red-800', dot: 'bg-white' },
}

export function RiskBadge({ estado }: { estado: EstadoRiesgo }) {
  const r = RIESGO[estado]
  return (
    <span
      className={`inline-flex shrink-0 items-center gap-1.5 rounded-full px-2.5 py-1 text-sm font-bold ring-1 ring-inset ${r.className}`}
    >
      <span aria-hidden="true" className={`h-2 w-2 rounded-full ${r.dot}`} />
      {r.label}
    </span>
  )
}

export function UbicacionTag({ ubicacion }: { ubicacion: Ubicacion }) {
  return (
    <span className="inline-flex shrink-0 items-center rounded-full bg-muted px-2.5 py-1 text-sm font-semibold text-muted-foreground">
      {UBICACION_LABEL[ubicacion]}
    </span>
  )
}
