import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/client'
import { getRutaDiaria, marcarLoteRevisado } from '../api/endpoints'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../components/Button'
import { PageHeader } from '../components/PageHeader'
import { RiskBadge } from '../components/RiskBadge'
import { EmptyState, ErrorState, InlineAlert, LoadingList } from '../components/States'
import { UBICACION_LABEL, describeDaysLeft, formatDate, formatMoney, formatNumber, formatUnits } from '../format'
import { errorMessage, useApi } from '../hooks/useApi'
import type { LoteRiesgo, Ubicacion } from '../types/domain'

function groupByUbicacion(lotes: LoteRiesgo[]): [Ubicacion, LoteRiesgo[]][] {
  const groups = new Map<Ubicacion, LoteRiesgo[]>()
  for (const lote of lotes) {
    const list = groups.get(lote.ubicacion) ?? []
    list.push(lote)
    groups.set(lote.ubicacion, list)
  }
  return [...groups.entries()]
}

function Stat({ label, value, tone }: { label: string; value: string; tone?: 'danger' }) {
  return (
    <div className="min-w-0 rounded-2xl border border-border bg-card px-4 py-3">
      <p className="text-sm font-semibold text-muted-foreground">{label}</p>
      <p className={`mt-0.5 truncate font-heading text-2xl font-bold ${tone === 'danger' ? 'text-red-700' : ''}`}>{value}</p>
    </div>
  )
}

function LoteCard({ lote, showValue, onRevisado }: { lote: LoteRiesgo; showValue: boolean; onRevisado: () => void }) {
  const vencido = lote.estadoRiesgo === 'CRITICO_VENCIDO'
  const [marcando, setMarcando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [yaRevisado, setYaRevisado] = useState<string | null>(null)

  // HU-16: deja constancia (usuario y hora) y el lote sale de la lista de pendientes del día
  async function marcarRevisado() {
    setError(null)
    setMarcando(true)
    try {
      await marcarLoteRevisado(lote.loteId)
      onRevisado()
    } catch (err) {
      // 409: otro usuario ya lo revisó hoy; se informa y no se reintenta
      if (err instanceof ApiError && err.status === 409) setYaRevisado(err.message)
      else setError(errorMessage(err))
      setMarcando(false)
    }
  }

  return (
    <li className="rounded-2xl border border-border bg-card p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-heading text-lg font-semibold leading-snug [overflow-wrap:anywhere]">{lote.productoNombre}</p>
          <p className="mt-0.5 text-sm text-muted-foreground">
            {lote.categoriaNombre ? `${lote.categoriaNombre} · ` : ''}
            {lote.productoCodigoBarra}
          </p>
        </div>
        <RiskBadge estado={lote.estadoRiesgo} />
      </div>
      <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
        <div>
          <dt className="text-muted-foreground">Vencimiento</dt>
          <dd className={`font-semibold ${vencido ? 'text-red-700' : ''}`}>
            {formatDate(lote.fechaVencimiento)}
            <span className="block font-normal">{describeDaysLeft(lote.diasHastaVencimiento)}</span>
          </dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Cantidad</dt>
          <dd className="font-semibold">{formatUnits(lote.cantidad)}</dd>
        </div>
        {lote.riesgoPorVelocidad && (
          <div className="col-span-2">
            <dt className="text-muted-foreground">Velocidad de venta</dt>
            <dd className="font-semibold text-red-700">
              {lote.diasCobertura != null
                ? `A este ritmo tarda ${formatNumber(lote.diasCobertura)} días en venderse`
                : 'Sin ventas registradas: no se sabe cuándo se agota'}
            </dd>
          </div>
        )}
        {showValue && lote.valorEnRiesgo != null && (
          <div className="col-span-2">
            <dt className="text-muted-foreground">Valor en riesgo</dt>
            <dd className="font-semibold">{formatMoney(lote.valorEnRiesgo)}</dd>
          </div>
        )}
      </dl>
      {error && (
        <div className="mt-3">
          <InlineAlert kind="error">{error}</InlineAlert>
        </div>
      )}
      {yaRevisado && (
        <div className="mt-3">
          <InlineAlert kind="info">{yaRevisado}</InlineAlert>
        </div>
      )}
      <div className="mt-3 grid gap-2 sm:grid-cols-2">
        <Link
          to={`/lotes/${lote.loteId}`}
          className="inline-flex min-h-11 w-full items-center justify-center rounded-xl border border-border-strong bg-card px-4 text-base font-bold text-foreground transition-colors hover:bg-muted"
        >
          Ver y actuar
        </Link>
        <Button variant="secondary" fullWidth loading={marcando} disabled={yaRevisado !== null} onClick={marcarRevisado}>
          {marcando ? 'Guardando' : 'Marcar como revisado'}
        </Button>
      </div>
    </li>
  )
}

export function RutaDelDiaPage() {
  const { session } = useAuth()
  const isDueno = session?.rol === 'ROLE_DUENO'
  const { data, error, loading, reload } = useApi((signal) => getRutaDiaria(signal))

  return (
    <section>
      <PageHeader title="Ruta del día" description="Lotes en rojo o vencidos, ordenados por ubicación y vencimiento." />

      {loading && !data ? (
        <LoadingList label="Cargando la ruta del día" />
      ) : error ? (
        <ErrorState message={error} onRetry={reload} />
      ) : data ? (
        <div className="space-y-6">
          <div className={`grid gap-3 ${isDueno ? 'grid-cols-2 lg:grid-cols-3' : 'grid-cols-2'}`}>
            <Stat label="A revisar" value={formatNumber(data.totalLotesAtencion)} />
            <Stat label="Vencidos" value={formatNumber(data.totalLotesVencidos)} tone={data.totalLotesVencidos > 0 ? 'danger' : undefined} />
            {isDueno && data.valorTotalEnRiesgo != null && (
              <div className="col-span-2 lg:col-span-1">
                <Stat label="Valor en riesgo" value={formatMoney(data.valorTotalEnRiesgo)} tone="danger" />
              </div>
            )}
          </div>

          {data.lotesPriorizados.length === 0 ? (
            <EmptyState title="Todo en orden">No hay lotes en rojo ni vencidos para revisar hoy.</EmptyState>
          ) : (
            groupByUbicacion(data.lotesPriorizados).map(([ubicacion, lotes]) => (
              <section key={ubicacion} aria-labelledby={`ubicacion-${ubicacion}`}>
                <h2 id={`ubicacion-${ubicacion}`} className="mb-3 flex items-baseline gap-2 text-lg font-bold">
                  {UBICACION_LABEL[ubicacion]}
                  <span className="text-sm font-semibold text-muted-foreground">
                    {lotes.length === 1 ? '1 lote' : `${formatNumber(lotes.length)} lotes`}
                  </span>
                </h2>
                <ul className="grid gap-3 lg:grid-cols-2">
                  {lotes.map((lote) => (
                    <LoteCard key={lote.loteId} lote={lote} showValue={isDueno} onRevisado={reload} />
                  ))}
                </ul>
              </section>
            ))
          )}
        </div>
      ) : null}
    </section>
  )
}
