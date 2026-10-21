import { useRef, useState, type FormEvent } from 'react'
import { ApiError } from '../api/client'
import { getAlertasRotacion, getLotesFefo, getProductos, trasladarAGondola } from '../api/endpoints'
import { Button } from '../components/Button'
import { PageHeader } from '../components/PageHeader'
import { UbicacionTag } from '../components/RiskBadge'
import { SelectField } from '../components/SelectField'
import { EmptyState, ErrorState, InlineAlert, LoadingList } from '../components/States'
import { TextField } from '../components/TextField'
import { describeDaysLeft, formatDate, formatNumber, formatUnits } from '../format'
import { errorMessage, useApi } from '../hooks/useApi'
import type { AlertaRotacion, Lote } from '../types/domain'

interface AlertaAgrupada {
  alerta: AlertaRotacion
  lotesGondola: number
}

/** El backend emite una alerta por cada lote en góndola; acá se agrupan por lote de depósito. */
function agruparAlertas(alertas: AlertaRotacion[]): AlertaAgrupada[] {
  const porLote = new Map<number, AlertaAgrupada>()
  for (const a of alertas) {
    const actual = porLote.get(a.loteDepositoId)
    if (actual) actual.lotesGondola += 1
    else porLote.set(a.loteDepositoId, { alerta: a, lotesGondola: 1 })
  }
  return [...porLote.values()]
}

function Alertas({ onElegir, version }: { onElegir: (productoId: number) => void; version: number }) {
  const { data, error, loading, reload } = useApi((signal) => getAlertasRotacion(signal), [version])

  return (
    <section aria-labelledby="alertas-titulo" className="space-y-3">
      <h2 id="alertas-titulo" className="text-lg font-bold">
        Alertas de rotación
      </h2>
      {loading && !data ? (
        <LoadingList rows={1} label="Cargando alertas" />
      ) : error ? (
        <ErrorState message={error} onRetry={reload} />
      ) : data && data.length === 0 ? (
        <EmptyState title="Sin alertas">La góndola está bien rotada.</EmptyState>
      ) : (
        <ul className="grid gap-3 lg:grid-cols-2">
          {agruparAlertas(data ?? []).map(({ alerta, lotesGondola }) => (
            <li key={alerta.loteDepositoId} className="rounded-2xl border border-amber-300 bg-amber-50 p-4">
              <p className="font-heading text-lg font-semibold leading-snug [overflow-wrap:anywhere]">{alerta.productoNombre}</p>
              <p className="mt-1 text-amber-950">
                En depósito hay un lote que vence el <strong>{formatDate(alerta.fechaVencimientoDeposito)}</strong>, antes que{' '}
                {lotesGondola === 1 ? 'el de góndola' : `los ${lotesGondola} de góndola`} ({formatDate(alerta.fechaVencimientoGondola)}).
              </p>
              <Button variant="secondary" className="mt-3 w-full sm:w-auto" onClick={() => onElegir(alerta.productoId)}>
                Reponer este producto
              </Button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

function Traslado({ lote, onTrasladado }: { lote: Lote; onTrasladado: (cantidad: number) => void }) {
  const [cantidad, setCantidad] = useState(String(lote.cantidad))
  const [error, setError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function trasladar(e: FormEvent) {
    e.preventDefault()
    const n = Number(cantidad)
    if (!Number.isInteger(n) || n < 1) return setError('Ingresá al menos 1 unidad.')
    if (n > lote.cantidad) return setError(`El lote tiene ${formatUnits(lote.cantidad)}.`)
    setError(null)
    setFormError(null)
    setGuardando(true)
    try {
      await trasladarAGondola({ loteOrigenId: lote.id, cantidadATrasladar: n })
      onTrasladado(n)
    } catch (err) {
      if (err instanceof ApiError && err.fieldErrors.cantidadATrasladar) setError(err.fieldErrors.cantidadATrasladar)
      else setFormError(errorMessage(err))
      setGuardando(false)
    }
  }

  return (
    <form noValidate onSubmit={trasladar} className="rounded-2xl border-2 border-primary bg-card p-4">
      <p className="text-sm font-bold uppercase tracking-wide text-primary-strong">Bajá este lote primero</p>
      <p className="mt-1 font-heading text-xl font-bold">Vence el {formatDate(lote.fechaVencimiento)}</p>
      <p className="text-muted-foreground">
        {describeDaysLeft(lote.diasHastaVencimiento)} · {formatUnits(lote.cantidad)} en depósito
      </p>
      {formError && (
        <div className="mt-3">
          <InlineAlert kind="error">{formError}</InlineAlert>
        </div>
      )}
      <div className="mt-4 space-y-3">
        <TextField
          label="Unidades a trasladar"
          inputMode="numeric"
          enterKeyHint="done"
          value={cantidad}
          onChange={(e) => setCantidad(e.target.value.replace(/\D/g, ''))}
          error={error ?? undefined}
        />
        <Button type="submit" fullWidth loading={guardando}>
          {guardando ? 'Trasladando' : 'Trasladar a góndola'}
        </Button>
      </div>
    </form>
  )
}

function LotesProducto({ productoId, version, onTrasladado }: { productoId: number; version: number; onTrasladado: (n: number) => void }) {
  const { data, error, loading, reload } = useApi((signal) => getLotesFefo(productoId, signal), [productoId, version])

  if (loading && !data) return <LoadingList rows={2} label="Cargando lotes" />
  if (error) return <ErrorState message={error} onRetry={reload} />
  if (!data) return null
  if (data.length === 0) return <EmptyState title="Sin stock">Este producto no tiene lotes activos.</EmptyState>

  const proximo = data.find((l) => l.ubicacion === 'DEPOSITO')

  return (
    <div className="space-y-4">
      {proximo ? (
        <Traslado key={`${proximo.id}-${proximo.cantidad}`} lote={proximo} onTrasladado={onTrasladado} />
      ) : (
        <InlineAlert kind="info">No hay lotes en depósito para bajar: todo el stock ya está a la venta.</InlineAlert>
      )}
      <div>
        <h3 className="mb-2 font-bold">Lotes en orden de vencimiento</h3>
        <ol className="space-y-2">
          {data.map((lote, i) => (
            <li
              key={lote.id}
              className={`flex items-center justify-between gap-3 rounded-xl border bg-card px-4 py-3 ${
                lote.id === proximo?.id ? 'border-primary' : 'border-border'
              }`}
            >
              <div className="min-w-0">
                <p className="font-semibold">
                  <span className="text-muted-foreground">{i + 1}.</span> Vence el {formatDate(lote.fechaVencimiento)}
                </p>
                <p className="text-sm text-muted-foreground">
                  {describeDaysLeft(lote.diasHastaVencimiento)} · {formatUnits(lote.cantidad)}
                </p>
              </div>
              <UbicacionTag ubicacion={lote.ubicacion} />
            </li>
          ))}
        </ol>
      </div>
    </div>
  )
}

export function ReponerPage() {
  const productos = useApi((signal) => getProductos(signal))
  const [productoId, setProductoId] = useState<number | null>(null)
  const [version, setVersion] = useState(0)
  const [aviso, setAviso] = useState<string | null>(null)
  const seleccionRef = useRef<HTMLDivElement>(null)

  function elegir(id: number | null) {
    setProductoId(id)
    setAviso(null)
  }

  function elegirDesdeAlerta(id: number) {
    elegir(id)
    const reducido = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
    seleccionRef.current?.scrollIntoView?.({ block: 'start', behavior: reducido ? 'auto' : 'smooth' })
  }

  const nombre = productos.data?.find((p) => p.id === productoId)?.nombre

  return (
    <section className="space-y-8">
      <PageHeader title="Reponer" description="Qué lote bajar primero a la góndola, según su vencimiento." />

      <Alertas version={version} onElegir={elegirDesdeAlerta} />

      <section aria-labelledby="producto-titulo" className="scroll-mt-20 space-y-4" ref={seleccionRef}>
        <h2 id="producto-titulo" className="text-lg font-bold">
          Reponer un producto
        </h2>
        {productos.error ? (
          <ErrorState message={productos.error} onRetry={productos.reload} />
        ) : (
          <SelectField
            label="Producto"
            value={productoId ?? ''}
            disabled={!productos.data}
            onChange={(e) => elegir(e.target.value ? Number(e.target.value) : null)}
          >
            <option value="">{productos.data ? 'Elegí un producto' : 'Cargando productos'}</option>
            {productos.data?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nombre}
              </option>
            ))}
          </SelectField>
        )}
        {aviso && <InlineAlert kind="success">{aviso}</InlineAlert>}
        {productoId !== null && (
          <LotesProducto
            productoId={productoId}
            version={version}
            onTrasladado={(n) => {
              setAviso(`Listo: ${formatNumber(n)} ${n === 1 ? 'unidad pasó' : 'unidades pasaron'} a góndola${nombre ? ` (${nombre})` : ''}.`)
              setVersion((v) => v + 1)
            }}
          />
        )}
      </section>
    </section>
  )
}
