import { useState, type FormEvent } from 'react'
import { actualizarMargenMinimo, getComercio } from '../api/endpoints'
import { useApi, errorMessage } from '../hooks/useApi'
import { formatPercent } from '../format'
import type { Comercio } from '../types/domain'
import { Button } from './Button'
import { ErrorState, InlineAlert, LoadingList } from './States'
import { TextField } from './TextField'

const MARGEN_MAX = 95

function Formulario({ comercio, onGuardado }: { comercio: Comercio; onGuardado: (c: Comercio) => void }) {
  const [valor, setValor] = useState(String(comercio.margenMinimoPorcentaje))
  const [error, setError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const n = Number(valor.replace(',', '.'))
    if (!Number.isFinite(n) || n < 0 || n > MARGEN_MAX) return setError(`Ingresá un margen entre 0% y ${MARGEN_MAX}%.`)
    setError(null)
    setFormError(null)
    setAviso(null)
    setGuardando(true)
    try {
      const actualizado = await actualizarMargenMinimo(comercio.id, n)
      onGuardado(actualizado)
      setAviso(`Listo: ningún descuento va a dejar el margen por debajo de ${formatPercent(actualizado.margenMinimoPorcentaje)}.`)
    } catch (err) {
      setFormError(errorMessage(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form noValidate onSubmit={guardar} className="space-y-3 rounded-2xl border border-border bg-card p-4">
      {formError && <InlineAlert kind="error">{formError}</InlineAlert>}
      {aviso && <InlineAlert kind="success">{aviso}</InlineAlert>}
      <TextField
        label="Piso de margen (%)"
        inputMode="decimal"
        value={valor}
        onChange={(e) => setValor(e.target.value.replace(/[^\d.,]/g, ''))}
        error={error ?? undefined}
        hint="Los descuentos sugeridos y aplicados nunca dejan el margen por debajo de este valor."
      />
      <Button type="submit" loading={guardando}>
        {guardando ? 'Guardando' : 'Guardar'}
      </Button>
    </form>
  )
}

/** HU-19: el dueño define el piso de margen que ningún descuento puede perforar. */
export function MargenMinimoCard({ comercioId }: { comercioId: number }) {
  const { data, error, loading, reload } = useApi((signal) => getComercio(comercioId, signal), [comercioId])
  const [guardado, setGuardado] = useState<Comercio | null>(null)
  const comercio = guardado ?? data

  return (
    <section aria-labelledby="margen-titulo" className="space-y-3">
      <h2 id="margen-titulo" className="text-lg font-bold">
        Piso de margen
      </h2>
      {loading && !comercio ? (
        <LoadingList rows={1} label="Cargando el piso de margen" />
      ) : error && !comercio ? (
        <ErrorState message={error} onRetry={reload} />
      ) : comercio ? (
        <Formulario comercio={comercio} onGuardado={setGuardado} />
      ) : null}
    </section>
  )
}
