import { useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import { aplicarDescuento, compararEscenarios, getDescuentoSugerido, getEvaluacionGeneral, getHistorialLote, registrarMerma } from '../api/endpoints'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../components/Button'
import { PageHeader } from '../components/PageHeader'
import { RiskBadge, UbicacionTag } from '../components/RiskBadge'
import { SelectField } from '../components/SelectField'
import { EmptyState, ErrorState, InlineAlert, LoadingList } from '../components/States'
import { TextField } from '../components/TextField'
import { describeDaysLeft, formatDate, formatDateTime, formatMoney, formatNumber, formatPercent, formatUnits } from '../format'
import { errorMessage, useApi } from '../hooks/useApi'
import type { DescuentoSugerido, EscenarioProyectado, LoteRiesgo, TipoAccion } from '../types/domain'

const DESCUENTO_MIN = 5
const DESCUENTO_MAX = 90

const ACCION_LABEL: Record<TipoAccion, string> = {
  DESCUENTO: 'Descuento',
  TRASLADO: 'Traslado',
  BAJA: 'Baja',
}

const MOTIVOS_BAJA = ['Vencido', 'Deteriorado', 'Roto o dañado', 'Otro']

interface SeccionProps {
  lote: LoteRiesgo
  version: number
  isDueno: boolean
  onCambio: () => void
}

function Seccion({ id, titulo, children }: { id: string; titulo: string; children: React.ReactNode }) {
  return (
    <section aria-labelledby={id} className="space-y-3">
      <h2 id={id} className="text-lg font-bold">
        {titulo}
      </h2>
      {children}
    </section>
  )
}

/** HU-15: cuánto tarda en venderse el lote a la velocidad real de venta, contra los días que le quedan. */
function describirCobertura(lote: LoteRiesgo): { texto: string; tono: 'ok' | 'riesgo' | 'neutro' } {
  if (lote.cantidad === 0) return { texto: 'El lote ya no tiene stock.', tono: 'neutro' }
  if (lote.diasHastaVencimiento <= 0) return { texto: 'El lote ya venció.', tono: 'riesgo' }
  if (lote.diasCobertura == null) {
    return { texto: 'Todavía no hay ventas registradas de este producto, así que no se puede estimar cuándo se agota.', tono: 'riesgo' }
  }
  if (lote.riesgoPorVelocidad) {
    return {
      texto: `A este ritmo tarda ${formatNumber(lote.diasCobertura)} días en venderse y vence en ${formatNumber(lote.diasHastaVencimiento)}: no llega a tiempo.`,
      tono: 'riesgo',
    }
  }
  return { texto: `A este ritmo se vende en ${formatNumber(lote.diasCobertura)} días: llega a tiempo.`, tono: 'ok' }
}

function Resumen({ lote, isDueno }: { lote: LoteRiesgo; isDueno: boolean }) {
  const cobertura = describirCobertura(lote)
  const estilo = { ok: 'bg-emerald-50 text-emerald-900', riesgo: 'bg-red-50 text-red-800', neutro: 'bg-muted text-foreground' }[cobertura.tono]
  return (
    <div className="rounded-2xl border border-border bg-card p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-heading text-xl font-semibold leading-snug [overflow-wrap:anywhere]">{lote.productoNombre}</p>
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
          <dd className="font-semibold">
            {formatDate(lote.fechaVencimiento)}
            <span className="block font-normal">{describeDaysLeft(lote.diasHastaVencimiento)}</span>
          </dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Cantidad</dt>
          <dd className="font-semibold">{formatUnits(lote.cantidad)}</dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Ubicación</dt>
          <dd>
            <UbicacionTag ubicacion={lote.ubicacion} />
          </dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Velocidad de venta</dt>
          <dd className="font-semibold">{formatNumber(lote.rotacionDiariaVentas ?? 0)} u. por día</dd>
        </div>
        {isDueno && lote.valorEnRiesgo != null && (
          <div className="col-span-2">
            <dt className="text-muted-foreground">Valor en riesgo</dt>
            <dd className="font-semibold">{formatMoney(lote.valorEnRiesgo)}</dd>
          </div>
        )}
      </dl>
      <p role="status" className={`mt-3 rounded-xl px-4 py-3 text-sm font-semibold ${estilo}`}>
        {cobertura.texto}
      </p>
    </div>
  )
}

function AplicarDescuentoForm({
  sugerido,
  lote,
  confirmacion,
  onConfirmacion,
  onAplicado,
}: {
  sugerido: DescuentoSugerido
  lote: LoteRiesgo
  confirmacion: string | null
  onConfirmacion: (mensaje: string | null) => void
  onAplicado: () => void
}) {
  const recomendado = sugerido.porcentajeDescuentoRecomendado
  const [porcentaje, setPorcentaje] = useState(recomendado >= DESCUENTO_MIN ? String(Math.round(recomendado * 100) / 100) : '')
  const [motivo, setMotivo] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function aplicar(e: FormEvent) {
    e.preventDefault()
    const n = Number(porcentaje.replace(',', '.'))
    if (!Number.isFinite(n) || n < DESCUENTO_MIN || n > DESCUENTO_MAX) {
      return setError(`Ingresá un descuento entre ${DESCUENTO_MIN}% y ${DESCUENTO_MAX}%.`)
    }
    setError(null)
    setFormError(null)
    onConfirmacion(null)
    setGuardando(true)
    try {
      const r = await aplicarDescuento({ loteId: lote.loteId, porcentajeDescuento: n, motivo: motivo.trim() || undefined })
      onConfirmacion(`${r.mensaje}. Precio con descuento: ${formatMoney(r.precioConDescuento)}.`)
      onAplicado()
    } catch (err) {
      if (err instanceof ApiError && err.fieldErrors.porcentajeDescuento) setError(err.fieldErrors.porcentajeDescuento)
      else setFormError(errorMessage(err))
      setGuardando(false)
    }
  }

  return (
    <form noValidate onSubmit={aplicar} className="space-y-3">
      {formError && <InlineAlert kind="error">{formError}</InlineAlert>}
      <TextField
        label="Descuento a aplicar (%)"
        inputMode="decimal"
        value={porcentaje}
        onChange={(e) => setPorcentaje(e.target.value.replace(/[^\d.,]/g, ''))}
        error={error ?? undefined}
        hint={`Entre ${DESCUENTO_MIN}% y ${DESCUENTO_MAX}%.`}
      />
      <TextField label="Motivo (opcional)" value={motivo} onChange={(e) => setMotivo(e.target.value)} />
      <Button type="submit" fullWidth loading={guardando} disabled={lote.cantidad === 0}>
        {guardando ? 'Aplicando' : 'Aplicar descuento al lote'}
      </Button>
      {confirmacion && <InlineAlert kind="success">{confirmacion}</InlineAlert>}
    </form>
  )
}

/** HU-17 y HU-19: descuento que liquida el lote a tiempo, topeado por el piso de margen del comercio. */
function DescuentoSugeridoSeccion({ lote, version, isDueno, onCambio }: SeccionProps) {
  const [confirmacion, setConfirmacion] = useState<string | null>(null)
  const { data, error, loading, reload } = useApi((signal) => getDescuentoSugerido(lote.loteId, signal), [lote.loteId, version])

  return (
    <Seccion id="descuento-titulo" titulo="Descuento sugerido">
      {loading && !data ? (
        <LoadingList rows={1} label="Calculando el descuento" />
      ) : error ? (
        <ErrorState message={error} onRetry={reload} />
      ) : data ? (
        <div className="space-y-4 rounded-2xl border border-border bg-card p-4">
          <div>
            <p className="text-sm font-semibold text-muted-foreground">Descuento recomendado</p>
            <p className="font-heading text-3xl font-bold">{formatPercent(data.porcentajeDescuentoRecomendado)}</p>
          </div>
          <p className="text-sm text-muted-foreground">
            Para vender {formatUnits(data.cantidad)} antes de vencer hace falta vender {formatNumber(data.velocidadVentaDiariaNecesaria)} por día. Hoy se venden{' '}
            {formatNumber(data.velocidadVentaDiariaActual)}.
          </p>
          {isDueno && (
            <p className="text-sm text-muted-foreground">
              Piso de margen del comercio: {formatPercent(data.margenMinimoPorcentaje)}. Con ese piso, el descuento máximo es{' '}
              {formatPercent(data.porcentajeDescuentoMaximoPorMargen)}.
            </p>
          )}
          {data.descuentoInsuficientePorMargen && (
            <InlineAlert kind="info">
              {isDueno
                ? `Para vender todo a tiempo hacía falta ${formatPercent(data.porcentajeDescuentoSugerido)}, pero eso perfora el piso de margen. Se recomienda el tope y puede quedar stock sin vender.`
                : 'Con el límite de descuento del comercio puede quedar stock sin vender. Consultá con el dueño.'}
            </InlineAlert>
          )}
          <AplicarDescuentoForm
            key={`${data.loteId}-${version}`}
            sugerido={data}
            lote={lote}
            confirmacion={confirmacion}
            onConfirmacion={setConfirmacion}
            onAplicado={onCambio}
          />
        </div>
      ) : null}
    </Seccion>
  )
}

function Escenario({ titulo, e, destacado }: { titulo: string; e: EscenarioProyectado; destacado: boolean }) {
  return (
    <div className={`rounded-2xl border bg-card p-4 ${destacado ? 'border-2 border-primary' : 'border-border'}`}>
      <p className="font-heading text-lg font-semibold">{titulo}</p>
      <dl className="mt-2 space-y-1.5 text-sm">
        <Fila label="Precio final" valor={formatMoney(e.precioFinal)} />
        <Fila label="Se venden" valor={formatUnits(e.unidadesEstimadasVendidas)} />
        <Fila label="Se descartan" valor={formatUnits(e.unidadesEstimadasDescartadas)} />
        <Fila label="Margen de lo vendido" valor={formatMoney(e.margenGananciaEstimado)} />
        <Fila label="Pérdida por merma" valor={formatMoney(e.perdidaPorMermaEstimada)} />
        <div className="flex justify-between gap-3 border-t border-border pt-2 text-base font-bold">
          <dt>Margen total</dt>
          <dd>{formatMoney(e.margenTotalProyectado)}</dd>
        </div>
      </dl>
    </div>
  )
}

function Fila({ label, valor }: { label: string; valor: string }) {
  return (
    <div className="flex justify-between gap-3">
      <dt className="text-muted-foreground">{label}</dt>
      <dd className="font-semibold">{valor}</dd>
    </div>
  )
}

/** HU-18: margen proyectado con y sin descuento. Solo el dueño, porque muestra márgenes y costos. */
function EscenariosSeccion({ lote, version }: SeccionProps) {
  const { data, error, loading, reload } = useApi((signal) => compararEscenarios(lote.loteId, signal), [lote.loteId, version])

  return (
    <Seccion id="escenarios-titulo" titulo="Con descuento o sin descuento">
      {loading && !data ? (
        <LoadingList rows={1} label="Comparando escenarios" />
      ) : error ? (
        <ErrorState message={error} onRetry={reload} />
      ) : data ? (
        <div className="space-y-3">
          <InlineAlert kind="info">
            {data.recomendacion === 'CON_DESCUENTO' ? 'Conviene aplicar el descuento: deja más margen.' : 'Conviene no aplicar descuento: deja más margen.'}
          </InlineAlert>
          <div className="grid gap-3 sm:grid-cols-2">
            <Escenario titulo="Sin descuento" e={data.escenarioSinDescuento} destacado={data.recomendacion === 'SIN_DESCUENTO'} />
            <Escenario
              titulo={`Con ${formatPercent(data.escenarioConDescuento.porcentajeDescuento)} de descuento`}
              e={data.escenarioConDescuento}
              destacado={data.recomendacion === 'CON_DESCUENTO'}
            />
          </div>
        </div>
      ) : null}
    </Seccion>
  )
}

/** Baja del lote (merma). Queda registrada en el historial del lote (HU-22). */
function BajaSeccion({ lote, onCambio }: SeccionProps) {
  const [confirmacion, setConfirmacion] = useState<string | null>(null)
  const [cantidad, setCantidad] = useState('1')
  const [motivo, setMotivo] = useState(MOTIVOS_BAJA[0])
  const [error, setError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function dar(e: FormEvent) {
    e.preventDefault()
    const n = Number(cantidad)
    if (!Number.isInteger(n) || n < 1) return setError('Ingresá al menos 1 unidad.')
    if (n > lote.cantidad) return setError(`El lote tiene ${formatUnits(lote.cantidad)}.`)
    setError(null)
    setFormError(null)
    setConfirmacion(null)
    setGuardando(true)
    try {
      await registrarMerma({ loteId: lote.loteId, cantidad: n, motivo })
      setCantidad('1')
      setConfirmacion(`Listo: se dieron de baja ${formatUnits(n)} (${motivo.toLowerCase()}).`)
      onCambio()
    } catch (err) {
      setFormError(errorMessage(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Seccion id="baja-titulo" titulo="Dar de baja unidades">
      <form noValidate onSubmit={dar} className="space-y-3 rounded-2xl border border-border bg-card p-4">
        {formError && <InlineAlert kind="error">{formError}</InlineAlert>}
        <TextField
          label="Unidades a dar de baja"
          inputMode="numeric"
          value={cantidad}
          onChange={(e) => setCantidad(e.target.value.replace(/\D/g, ''))}
          error={error ?? undefined}
        />
        <SelectField label="Motivo" value={motivo} onChange={(e) => setMotivo(e.target.value)}>
          {MOTIVOS_BAJA.map((m) => (
            <option key={m} value={m}>
              {m}
            </option>
          ))}
        </SelectField>
        <Button type="submit" variant="danger" fullWidth loading={guardando} disabled={lote.cantidad === 0}>
          {guardando ? 'Registrando' : 'Dar de baja'}
        </Button>
        {confirmacion && <InlineAlert kind="success">{confirmacion}</InlineAlert>}
      </form>
    </Seccion>
  )
}

/** HU-22: qué se hizo con este lote, quién y cuándo. */
function HistorialSeccion({ lote, version }: SeccionProps) {
  const { data, error, loading, reload } = useApi((signal) => getHistorialLote(lote.loteId, signal), [lote.loteId, version])

  return (
    <Seccion id="historial-titulo" titulo="Historial de acciones">
      {loading && !data ? (
        <LoadingList rows={1} label="Cargando el historial" />
      ) : error ? (
        <ErrorState message={error} onRetry={reload} />
      ) : data && data.length === 0 ? (
        <EmptyState title="Sin acciones">Todavía no se registró ninguna acción sobre este lote.</EmptyState>
      ) : (
        <ol className="space-y-2">
          {data?.map((a) => (
            <li key={a.id} className="rounded-xl border border-border bg-card px-4 py-3">
              <p className="font-semibold">
                {ACCION_LABEL[a.tipoAccion]}
                {a.motivo ? <span className="font-normal text-muted-foreground"> · {a.motivo}</span> : null}
              </p>
              <p className="text-sm text-muted-foreground">
                {a.usuarioNombre} · {formatDateTime(a.fechaHora)}
              </p>
            </li>
          ))}
        </ol>
      )}
    </Seccion>
  )
}

export function LotePage() {
  const { loteId } = useParams()
  const id = Number(loteId)
  const { session } = useAuth()
  const isDueno = session?.rol === 'ROLE_DUENO'
  const [version, setVersion] = useState(0)
  const evaluacion = useApi((signal) => getEvaluacionGeneral(signal), [version])
  const lote = evaluacion.data?.find((l) => l.loteId === id)

  function alCambiar() {
    setVersion((v) => v + 1)
  }

  const props = lote && { lote, version, isDueno, onCambio: alCambiar }

  return (
    <section className="space-y-6">
      <div>
        <Link to="/" className="text-sm font-bold text-primary-strong underline-offset-2 hover:underline">
          Volver a la ruta del día
        </Link>
      </div>
      <PageHeader title="Lote" description="Cuándo se vende, qué descuento conviene y qué se hizo con él." />

      {evaluacion.loading && !evaluacion.data ? (
        <LoadingList label="Cargando el lote" />
      ) : evaluacion.error ? (
        <ErrorState message={evaluacion.error} onRetry={evaluacion.reload} />
      ) : !lote || !props ? (
        <EmptyState title="No encontramos este lote">Puede que ya no exista o que el enlace sea incorrecto.</EmptyState>
      ) : (
        <>
          <Resumen lote={lote} isDueno={isDueno} />
          {lote.cantidad > 0 && <DescuentoSugeridoSeccion {...props} />}
          {lote.cantidad > 0 && isDueno && <EscenariosSeccion {...props} />}
          {lote.cantidad > 0 && <BajaSeccion {...props} />}
          <HistorialSeccion {...props} />
        </>
      )}
    </section>
  )
}
