import { useEffect, useState, type FormEvent } from 'react'
import { buscarProductos } from '../api/endpoints'
import { Button } from '../components/Button'
import { Icon } from '../components/Icon'
import { PageHeader } from '../components/PageHeader'
import { UbicacionTag } from '../components/RiskBadge'
import { EmptyState, ErrorState, LoadingList } from '../components/States'
import { describeDaysLeft, formatDate, formatNumber, formatUnits } from '../format'
import { errorMessage } from '../hooks/useApi'
import type { ProductoConLotes } from '../types/domain'

const PAGE_SIZE = 10
const DEBOUNCE_MS = 300

function ProductoCard({ producto }: { producto: ProductoConLotes }) {
  const lotes = [...producto.lotes].sort((a, b) => a.fechaVencimiento.localeCompare(b.fechaVencimiento))
  return (
    <li className="rounded-2xl border border-border bg-card p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-heading text-lg font-semibold leading-snug [overflow-wrap:anywhere]">{producto.nombreProducto}</p>
          <p className="text-sm text-muted-foreground">
            {producto.categoriaNombre ? `${producto.categoriaNombre} · ` : ''}
            {producto.codigoBarra}
          </p>
        </div>
        <div className="shrink-0 text-right">
          <p className="text-sm text-muted-foreground">Stock</p>
          <p className="font-heading text-lg font-bold">{formatNumber(producto.stockTotal)}</p>
        </div>
      </div>
      {lotes.length === 0 ? (
        <p className="mt-3 text-muted-foreground">Sin lotes activos.</p>
      ) : (
        <ul className="mt-3 divide-y divide-border border-t border-border">
          {lotes.map((lote) => (
            <li key={lote.id} className="flex items-center justify-between gap-3 py-2.5">
              <div className="min-w-0">
                <p className="font-semibold">Vence el {formatDate(lote.fechaVencimiento)}</p>
                <p className="text-sm text-muted-foreground">
                  {describeDaysLeft(lote.diasHastaVencimiento)} · {formatUnits(lote.cantidad)}
                </p>
              </div>
              <UbicacionTag ubicacion={lote.ubicacion} />
            </li>
          ))}
        </ul>
      )}
    </li>
  )
}

export function ConsultaPage() {
  const [texto, setTexto] = useState('')
  const [query, setQuery] = useState('')
  const [resultados, setResultados] = useState<ProductoConLotes[]>([])
  const [pagina, setPagina] = useState(0)
  const [hayMas, setHayMas] = useState(false)
  const [total, setTotal] = useState(0)
  const [intento, setIntento] = useState(0)
  const [terminada, setTerminada] = useState<{ clave: string; error: string | null } | null>(null)

  const clave = `${query}|${pagina}|${intento}`
  const cargando = query !== '' && terminada?.clave !== clave
  const error = terminada?.clave === clave ? terminada.error : null

  useEffect(() => {
    const id = setTimeout(() => {
      setQuery(texto.trim())
      setPagina(0)
    }, DEBOUNCE_MS)
    return () => clearTimeout(id)
  }, [texto])

  useEffect(() => {
    if (!query) return
    const controller = new AbortController()
    const actual = `${query}|${pagina}|${intento}`
    buscarProductos({ query, page: pagina, size: PAGE_SIZE }, controller.signal)
      .then((res) => {
        setResultados((prev) => (pagina === 0 ? res.content : [...prev, ...res.content]))
        setHayMas(!res.last)
        setTotal(res.totalElements)
        setTerminada({ clave: actual, error: null })
      })
      .catch((err) => !controller.signal.aborted && setTerminada({ clave: actual, error: errorMessage(err) }))
    return () => controller.abort()
  }, [query, pagina, intento])

  function buscarYa(e: FormEvent) {
    e.preventDefault()
    setPagina(0)
    setQuery(texto.trim())
    setIntento((i) => i + 1)
  }

  return (
    <section className="max-w-3xl">
      <PageHeader title="Consulta" description="Buscá un producto por nombre o código y mirá sus lotes activos." />

      <form role="search" onSubmit={buscarYa} className="mb-5">
        <label htmlFor="consulta-texto" className="mb-1.5 block text-sm font-semibold">
          Nombre o código de barras
        </label>
        <div className="relative">
          <Icon name="search" className="pointer-events-none absolute top-1/2 left-4 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
          <input
            id="consulta-texto"
            type="search"
            autoComplete="off"
            autoCapitalize="none"
            enterKeyHint="search"
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            placeholder="Ej: leche o 7790001000011"
            className="block min-h-12 w-full rounded-xl border border-border-strong bg-card pr-4 pl-12 text-base placeholder:text-muted-foreground/70 focus:border-primary focus:ring-3 focus:ring-ring/25 focus:outline-none"
          />
        </div>
      </form>

      {!query ? (
        <EmptyState title="Buscá un producto">Escribí parte del nombre o el código de barras.</EmptyState>
      ) : error ? (
        <ErrorState message={error} onRetry={() => setIntento((i) => i + 1)} />
      ) : cargando && pagina === 0 ? (
        <LoadingList rows={2} label="Buscando" />
      ) : resultados.length === 0 ? (
        <EmptyState title="Sin resultados">No encontramos productos para "{query}".</EmptyState>
      ) : (
        <div className="space-y-4">
          <p aria-live="polite" className="text-sm font-semibold text-muted-foreground">
            {total === 1 ? '1 producto' : `${formatNumber(total)} productos`}
          </p>
          <ul className="space-y-3">
            {resultados.map((p) => (
              <ProductoCard key={p.productoId} producto={p} />
            ))}
          </ul>
          {hayMas && (
            <Button variant="secondary" fullWidth loading={cargando} onClick={() => setPagina((n) => n + 1)}>
              Ver más
            </Button>
          )}
        </div>
      )}
    </section>
  )
}
