import { useState } from 'react'
import { Link } from 'react-router-dom'
import { getCategorias, getComparacionLineaBase, getResumenMermas } from '../api/endpoints'
import { PageHeader } from '../components/PageHeader'
import { SelectField } from '../components/SelectField'
import { EmptyState, ErrorState, InlineAlert, LoadingList } from '../components/States'
import { TextField } from '../components/TextField'
import { CAUSA_MERMA_LABEL, formatMoney, formatPercent, formatUnits, todayIso } from '../format'
import { useApi } from '../hooks/useApi'
import type { CausaMerma, ComparacionLineaBase, MermaGrupo } from '../types/domain'

function nombreDelMes(mes: string): string {
  const fecha = new Date(`${mes}-01T00:00:00`)
  const texto = fecha.toLocaleDateString('es-AR', { month: 'long', year: 'numeric' })
  // "octubre de 2026" -> "Octubre de 2026" (con la clase capitalize quedaba "Octubre De 2026")
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="min-w-0 rounded-2xl border border-border bg-card px-4 py-3">
      <p className="text-sm font-semibold text-muted-foreground">{label}</p>
      <p className="mt-0.5 truncate font-heading text-2xl font-bold">{value}</p>
    </div>
  )
}

function ListaDeGrupos({ id, titulo, grupos }: { id: string; titulo: string; grupos: MermaGrupo[] }) {
  return (
    <section aria-labelledby={id} className="space-y-3">
      <h2 id={id} className="text-lg font-bold">
        {titulo}
      </h2>
      <ul className="space-y-2">
        {grupos.map((g) => (
          <li key={g.clave} className="rounded-xl border border-border bg-card px-4 py-3">
            <div className="flex items-baseline justify-between gap-3">
              <p className="font-semibold [overflow-wrap:anywhere]">{g.nombre}</p>
              <p className="shrink-0 font-bold">{formatMoney(g.perdidaTotal)}</p>
            </div>
            <p className="text-sm text-muted-foreground">
              {formatUnits(g.unidades)} · {formatPercent(g.porcentaje)} de la merma
            </p>
            <div className="mt-2 h-2 rounded-full bg-muted" aria-hidden="true">
              <div className="h-2 rounded-full bg-primary" style={{ width: `${Math.min(100, Math.max(0, g.porcentaje))}%` }} />
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}

/** HU-26: la merma real del período contra lo que se perdía antes de usar Rotá. */
function ComparacionCard({ c }: { c: ComparacionLineaBase }) {
  if (!c.hayLineaBase) {
    return (
      <InlineAlert kind="info">
        {c.mensaje}{' '}
        <Link to="/configuracion" className="underline">
          Cargala en Configuración
        </Link>
        .
      </InlineAlert>
    )
  }
  const mejoro = c.mejoro === true
  const diferencia = c.ahorroPesos ?? 0
  // El mensaje del backend trae el monto sin formato ("$26400.00"): se arma acá con formato de pesos
  const titulo = mejoro
    ? `La merma del período es menor que la línea base: el ahorro estimado es de ${formatMoney(diferencia)}.`
    : diferencia === 0
      ? 'La merma del período es igual a la línea base.'
      : `La merma del período supera la línea base en ${formatMoney(Math.abs(diferencia))}.`
  return (
    <div className={`space-y-3 rounded-2xl border p-4 ${mejoro ? 'border-emerald-300 bg-emerald-50' : 'border-red-200 bg-red-50'}`}>
      <p className={`font-heading text-lg font-bold ${mejoro ? 'text-emerald-900' : 'text-red-800'}`}>{titulo}</p>
      <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
        <div>
          <dt className="text-muted-foreground">Antes de Rotá (período)</dt>
          <dd className="font-semibold">{formatMoney(c.lineaBasePeriodo ?? 0)}</dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Con Rotá</dt>
          <dd className="font-semibold">{formatMoney(c.mermaPeriodo)}</dd>
        </div>
        <div>
          <dt className="text-muted-foreground">{mejoro ? 'Ahorro' : 'Diferencia'}</dt>
          <dd className="font-semibold">{formatMoney(diferencia)}</dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Variación</dt>
          <dd className="font-semibold">{formatPercent(c.ahorroPorcentaje ?? 0)}</dd>
        </div>
      </dl>
    </div>
  )
}

/** HU-25 y HU-26 (solo dueño): cuánto se perdió a valor costo, por categoría y causa, y contra la línea base. */
export function MermasPage() {
  const hoy = todayIso()
  const [desde, setDesde] = useState(`${hoy.slice(0, 8)}01`)
  const [hasta, setHasta] = useState(hoy)
  const [categoriaId, setCategoriaId] = useState('')
  const [causa, setCausa] = useState('')

  const categorias = useApi((signal) => getCategorias(signal))
  const resumen = useApi(
    (signal) =>
      getResumenMermas(
        { desde, hasta, categoriaId: categoriaId ? Number(categoriaId) : undefined, causa: (causa || undefined) as CausaMerma | undefined },
        signal,
      ),
    [desde, hasta, categoriaId, causa],
  )
  const comparacion = useApi((signal) => getComparacionLineaBase({ desde, hasta }, signal), [desde, hasta])
  const datos = resumen.data

  return (
    <section className="space-y-6">
      <PageHeader title="Merma" description="Cuánto se perdió, a valor costo, y en qué categorías y por qué causas." />

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <TextField label="Desde" type="date" value={desde} max={hasta} onChange={(e) => setDesde(e.target.value)} />
        <TextField label="Hasta" type="date" value={hasta} min={desde} max={hoy} onChange={(e) => setHasta(e.target.value)} />
        <SelectField label="Categoría" value={categoriaId} onChange={(e) => setCategoriaId(e.target.value)}>
          <option value="">Todas</option>
          {categorias.data?.map((c) => (
            <option key={c.id} value={c.id}>
              {c.nombre}
            </option>
          ))}
        </SelectField>
        <SelectField label="Causa" value={causa} onChange={(e) => setCausa(e.target.value)}>
          <option value="">Todas</option>
          {(Object.entries(CAUSA_MERMA_LABEL) as [CausaMerma, string][]).map(([valor, etiqueta]) => (
            <option key={valor} value={valor}>
              {etiqueta}
            </option>
          ))}
        </SelectField>
      </div>

      {resumen.loading && !datos ? (
        <LoadingList label="Cargando la merma" />
      ) : resumen.error ? (
        <ErrorState message={resumen.error} onRetry={resumen.reload} />
      ) : datos ? (
        <div className="space-y-6">
          <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
            <div className="col-span-2 lg:col-span-1">
              <Stat label="Pérdida a costo" value={formatMoney(datos.perdidaTotal)} />
            </div>
            <Stat label="Unidades" value={formatUnits(datos.unidadesTotales)} />
            <Stat label="Bajas" value={String(datos.registros)} />
          </div>

          {datos.registros === 0 ? (
            <EmptyState title="Sin bajas en el período">No se registró merma entre estas fechas con los filtros elegidos.</EmptyState>
          ) : (
            <>
              <ListaDeGrupos id="categoria-titulo" titulo="Por categoría" grupos={datos.porCategoria} />
              <ListaDeGrupos id="causa-titulo" titulo="Por causa" grupos={datos.porCausa} />
              {datos.porMes.length > 0 && (
                <section aria-labelledby="mes-titulo" className="space-y-3">
                  <h2 id="mes-titulo" className="text-lg font-bold">
                    Por mes
                  </h2>
                  <ul className="space-y-2">
                    {datos.porMes.map((m) => (
                      <li key={m.mes} className="flex items-baseline justify-between gap-3 rounded-xl border border-border bg-card px-4 py-3">
                        <p className="font-semibold">{nombreDelMes(m.mes)}</p>
                        <p className="font-bold">{formatMoney(m.perdidaTotal)}</p>
                      </li>
                    ))}
                  </ul>
                </section>
              )}
            </>
          )}
        </div>
      ) : null}

      <section aria-labelledby="linea-base-comparacion-titulo" className="space-y-3">
        <h2 id="linea-base-comparacion-titulo" className="text-lg font-bold">
          Contra la línea base
        </h2>
        {comparacion.loading && !comparacion.data ? (
          <LoadingList rows={1} label="Cargando la comparación" />
        ) : comparacion.error ? (
          <ErrorState message={comparacion.error} onRetry={comparacion.reload} />
        ) : comparacion.data ? (
          <ComparacionCard c={comparacion.data} />
        ) : null}
      </section>
    </section>
  )
}
