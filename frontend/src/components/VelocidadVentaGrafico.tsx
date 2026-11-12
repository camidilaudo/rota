import { formatNumber } from '../format'
import type { VelocidadVentaLote } from '../types/domain'

const ANCHO = 320
const ALTO = 150
const MARGEN = { arriba: 12, abajo: 22, izq: 26, der: 8 }

/** Barras de unidades vendidas por día y una línea punteada con la velocidad necesaria para agotar el lote a tiempo. SVG a mano, sin librerías. */
export function VelocidadVentaGrafico({ datos }: { datos: VelocidadVentaLote }) {
  const dias = datos.ventasPorDia
  const necesaria = datos.velocidadVentaDiariaNecesaria
  const maximo = Math.max(1, necesaria ?? 0, ...dias.map((d) => d.unidades))
  const techo = Math.ceil(maximo * 1.15)

  const anchoUtil = ANCHO - MARGEN.izq - MARGEN.der
  const altoUtil = ALTO - MARGEN.arriba - MARGEN.abajo
  const paso = anchoUtil / dias.length
  const y = (v: number) => MARGEN.arriba + altoUtil - (v / techo) * altoUtil
  const llega = necesaria == null || datos.velocidadVentaDiariaActual >= necesaria

  const resumen =
    necesaria == null
      ? `Promedio de ${formatNumber(datos.velocidadVentaDiariaActual)} unidades por día en los últimos ${dias.length} días.`
      : `Se venden ${formatNumber(datos.velocidadVentaDiariaActual)} unidades por día; para agotar el lote antes de vencer hacen falta ${formatNumber(necesaria)}.`

  return (
    <figure className="space-y-3 rounded-2xl border border-border bg-card p-4">
      <svg viewBox={`0 0 ${ANCHO} ${ALTO}`} className="h-auto w-full" role="img" aria-label={resumen}>
        {[0, techo].map((v) => (
          <g key={v}>
            <line x1={MARGEN.izq} x2={ANCHO - MARGEN.der} y1={y(v)} y2={y(v)} stroke="var(--color-border-strong)" strokeWidth={1} />
            <text x={MARGEN.izq - 4} y={y(v) + 3} textAnchor="end" fontSize={9} fill="var(--color-muted-foreground)">
              {v}
            </text>
          </g>
        ))}
        {dias.map((d, i) => {
          const alto = y(0) - y(d.unidades)
          return (
            <rect
              key={d.fecha}
              x={MARGEN.izq + i * paso + paso * 0.15}
              y={y(d.unidades)}
              width={paso * 0.7}
              height={Math.max(alto, d.unidades > 0 ? 1 : 0)}
              rx={2}
              fill="var(--color-primary)"
            >
              <title>{`${d.fecha.slice(8, 10)}/${d.fecha.slice(5, 7)}: ${d.unidades} u.`}</title>
            </rect>
          )
        })}
        {necesaria != null && (
          <line
            x1={MARGEN.izq}
            x2={ANCHO - MARGEN.der}
            y1={y(necesaria)}
            y2={y(necesaria)}
            stroke="var(--color-destructive)"
            strokeWidth={2}
            strokeDasharray="5 4"
          />
        )}
        {[0, dias.length - 1].map((i) =>
          dias[i] ? (
            <text
              key={i}
              x={MARGEN.izq + i * paso + paso / 2}
              y={ALTO - 6}
              textAnchor="middle"
              fontSize={9}
              fill="var(--color-muted-foreground)"
            >
              {i === dias.length - 1 ? 'Hoy' : `${dias[i].fecha.slice(8, 10)}/${dias[i].fecha.slice(5, 7)}`}
            </text>
          ) : null,
        )}
      </svg>
      <ul className="flex flex-wrap gap-x-4 gap-y-1 text-sm">
        <li className="flex items-center gap-2">
          <span aria-hidden="true" className="h-3 w-3 rounded-sm bg-primary" />
          Unidades vendidas por día
        </li>
        {necesaria != null && (
          <li className="flex items-center gap-2">
            <span aria-hidden="true" className="w-4 border-t-2 border-dashed border-destructive" />
            Necesarias por día: {formatNumber(necesaria)}
          </li>
        )}
      </ul>
      <figcaption className={`rounded-xl px-4 py-3 text-sm font-semibold ${llega ? 'bg-emerald-50 text-emerald-900' : 'bg-red-50 text-red-800'}`}>
        {resumen}
      </figcaption>
    </figure>
  )
}
