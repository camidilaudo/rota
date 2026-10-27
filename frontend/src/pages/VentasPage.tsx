import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/client'
import { getProductos, registrarVenta } from '../api/endpoints'
import { Button } from '../components/Button'
import { PageHeader } from '../components/PageHeader'
import { SelectField } from '../components/SelectField'
import { ErrorState, InlineAlert } from '../components/States'
import { TextField } from '../components/TextField'
import { formatNumber, formatUnits } from '../format'
import { errorMessage, useApi } from '../hooks/useApi'
import type { VentaRegistrada } from '../types/domain'

/** HU-20: registro de ventas. El backend descuenta el stock por FEFO y recalcula la velocidad de venta. */
export function VentasPage() {
  const productos = useApi((signal) => getProductos(signal))
  const [productoId, setProductoId] = useState('')
  const [cantidad, setCantidad] = useState('1')
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)
  const [resultado, setResultado] = useState<VentaRegistrada | null>(null)

  async function registrar(e: FormEvent) {
    e.preventDefault()
    const nuevos: Record<string, string> = {}
    const n = Number(cantidad)
    if (!productoId) nuevos.producto = 'Elegí un producto.'
    if (!Number.isInteger(n) || n < 1) nuevos.cantidad = 'Ingresá al menos 1 unidad.'
    setErrores(nuevos)
    setFormError(null)
    if (Object.keys(nuevos).length > 0) return

    setGuardando(true)
    setResultado(null)
    try {
      const venta = await registrarVenta({ productoId: Number(productoId), cantidad: n })
      setResultado(venta)
      setCantidad('1')
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.fieldErrors).length > 0) {
        setErrores({ cantidad: err.fieldErrors.cantidad ?? '', producto: err.fieldErrors.productoId ?? '' })
      } else {
        setFormError(errorMessage(err))
      }
    } finally {
      setGuardando(false)
    }
  }

  const agotados = resultado?.lotesAfectados.filter((l) => l.cantidadRestanteEnLote === 0) ?? []

  return (
    <section className="space-y-6">
      <PageHeader title="Ventas" description="Registrá lo que se vendió. Se descuenta primero del lote que vence antes." />

      {productos.error ? (
        <ErrorState message={productos.error} onRetry={productos.reload} />
      ) : (
        <form noValidate onSubmit={registrar} className="space-y-4 rounded-2xl border border-border bg-card p-4">
          {formError && <InlineAlert kind="error">{formError}</InlineAlert>}
          <SelectField
            label="Producto"
            value={productoId}
            disabled={!productos.data}
            onChange={(e) => setProductoId(e.target.value)}
            error={errores.producto || undefined}
          >
            <option value="">{productos.data ? 'Elegí un producto' : 'Cargando productos'}</option>
            {productos.data?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nombre}
              </option>
            ))}
          </SelectField>
          <TextField
            label="Unidades vendidas"
            inputMode="numeric"
            enterKeyHint="done"
            value={cantidad}
            onChange={(e) => setCantidad(e.target.value.replace(/\D/g, ''))}
            error={errores.cantidad || undefined}
          />
          <Button type="submit" fullWidth loading={guardando}>
            {guardando ? 'Registrando' : 'Registrar venta'}
          </Button>
        </form>
      )}

      {resultado && (
        <section aria-labelledby="venta-resultado" className="space-y-3">
          <InlineAlert kind="success">
            Listo: {formatUnits(resultado.cantidadVendida)} de {resultado.productoNombre}.
          </InlineAlert>
          {agotados.length > 0 && (
            <InlineAlert kind="info">
              {agotados.length === 1 ? `Se agotó el lote ${agotados[0].loteId}.` : `Se agotaron los lotes ${agotados.map((l) => l.loteId).join(', ')}.`} Si hay stock en el depósito,{' '}
              <Link to="/reponer" className="underline">
                reponé la góndola
              </Link>
              .
            </InlineAlert>
          )}
          <h2 id="venta-resultado" className="text-lg font-bold">
            De qué lotes salió
          </h2>
          <ul className="space-y-2">
            {resultado.lotesAfectados.map((l) => (
              <li key={l.loteId} className="flex items-center justify-between gap-3 rounded-xl border border-border bg-card px-4 py-3">
                <div className="min-w-0">
                  <p className="font-semibold">
                    Lote {l.loteId}: {formatUnits(l.cantidadDescontada)}
                  </p>
                  <p className="text-sm text-muted-foreground">Quedan {formatUnits(l.cantidadRestanteEnLote)}</p>
                </div>
                <Link to={`/lotes/${l.loteId}`} className="shrink-0 text-sm font-bold text-primary-strong underline-offset-2 hover:underline">
                  Ver lote
                </Link>
              </li>
            ))}
          </ul>
          <p className="text-sm text-muted-foreground">
            Velocidad de venta actualizada: {formatNumber(resultado.rotacionDiariaActualizada)} unidades por día.
          </p>
        </section>
      )}
    </section>
  )
}
