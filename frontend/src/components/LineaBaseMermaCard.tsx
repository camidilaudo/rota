import { useState, type FormEvent } from 'react'
import { actualizarLineaBaseMerma, getComercio } from '../api/endpoints'
import { formatMoney } from '../format'
import { errorMessage, useApi } from '../hooks/useApi'
import type { Comercio } from '../types/domain'
import { Button } from './Button'
import { ErrorState, InlineAlert, LoadingList } from './States'
import { TextField } from './TextField'

function Formulario({ comercio, onGuardado }: { comercio: Comercio; onGuardado: (c: Comercio) => void }) {
  const [valor, setValor] = useState(comercio.lineaBaseMermaMensual != null ? String(comercio.lineaBaseMermaMensual) : '')
  const [error, setError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const n = Number(valor.replace(',', '.'))
    if (valor.trim() === '' || !Number.isFinite(n) || n < 0) return setError('Ingresá un monto mayor o igual a 0.')
    setError(null)
    setFormError(null)
    setAviso(null)
    setGuardando(true)
    try {
      const actualizado = await actualizarLineaBaseMerma(comercio.id, n)
      onGuardado(actualizado)
      setAviso(`Listo: la línea base de merma es de ${formatMoney(actualizado.lineaBaseMermaMensual ?? n)} por mes.`)
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
        label="Merma mensual antes de Rotá ($)"
        inputMode="decimal"
        value={valor}
        onChange={(e) => setValor(e.target.value.replace(/[^\d.,]/g, ''))}
        error={error ?? undefined}
        hint="Lo que se perdía por mes a valor costo. Es la referencia para medir el ahorro en el panel de merma."
      />
      <Button type="submit" loading={guardando}>
        {guardando ? 'Guardando' : 'Guardar'}
      </Button>
    </form>
  )
}

/** HU-26: el dueño carga la merma mensual que tenía antes de usar Rotá. */
export function LineaBaseMermaCard({ comercioId }: { comercioId: number }) {
  const { data, error, loading, reload } = useApi((signal) => getComercio(comercioId, signal), [comercioId])
  const [guardado, setGuardado] = useState<Comercio | null>(null)
  const comercio = guardado ?? data

  return (
    <section aria-labelledby="linea-base-titulo" className="space-y-3">
      <h2 id="linea-base-titulo" className="text-lg font-bold">
        Línea base de merma
      </h2>
      {loading && !comercio ? (
        <LoadingList rows={1} label="Cargando la línea base" />
      ) : error && !comercio ? (
        <ErrorState message={error} onRetry={reload} />
      ) : comercio ? (
        <Formulario comercio={comercio} onGuardado={setGuardado} />
      ) : null}
    </section>
  )
}
