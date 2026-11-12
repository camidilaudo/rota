import { useEffect, useId, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { formatDate } from '../format'
import type { AlertaAgrupada } from '../rotacion'
import { Icon } from './Icon'

/** Campana del encabezado: cuenta las alertas de rotación y lleva al lote de depósito de cada una. */
export function AlertasCampana({ alertas, align = 'right' }: { alertas: AlertaAgrupada[]; align?: 'left' | 'right' }) {
  const [abierta, setAbierta] = useState(false)
  const raiz = useRef<HTMLDivElement>(null)
  const panelId = useId()
  const total = alertas.length

  useEffect(() => {
    if (!abierta) return
    const cerrarAfuera = (e: MouseEvent) => {
      if (!raiz.current?.contains(e.target as Node)) setAbierta(false)
    }
    const cerrarConEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setAbierta(false)
    }
    document.addEventListener('mousedown', cerrarAfuera)
    document.addEventListener('keydown', cerrarConEscape)
    return () => {
      document.removeEventListener('mousedown', cerrarAfuera)
      document.removeEventListener('keydown', cerrarConEscape)
    }
  }, [abierta])

  return (
    <div ref={raiz} className="relative">
      <button
        type="button"
        onClick={() => setAbierta((v) => !v)}
        aria-expanded={abierta}
        aria-controls={panelId}
        aria-label={total === 0 ? 'Alertas: sin alertas' : `Alertas: ${total} ${total === 1 ? 'alerta' : 'alertas'}`}
        className="relative flex h-12 w-12 shrink-0 items-center justify-center rounded-xl text-muted-foreground transition-colors duration-150 hover:bg-muted active:bg-muted"
      >
        <Icon name="bell" className="h-6 w-6" />
        {total > 0 && (
          <span
            aria-hidden="true"
            className="absolute right-1.5 top-1.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-xs font-bold text-white"
          >
            {total > 9 ? '9+' : total}
          </span>
        )}
      </button>

      {abierta && (
        <div
          id={panelId}
          className={`absolute top-full z-30 mt-2 w-[min(20rem,calc(100vw-2rem))] rounded-2xl border border-border bg-card p-3 shadow-lg ${
            align === 'right' ? 'right-0' : 'left-0'
          }`}
        >
          <p className="px-1 pb-2 font-heading text-base font-bold">Alertas de rotación</p>
          {total === 0 ? (
            <p className="px-1 pb-1 text-sm text-muted-foreground">Sin alertas. La góndola está bien rotada.</p>
          ) : (
            <ul className="max-h-80 space-y-1 overflow-y-auto">
              {alertas.map(({ alerta }) => (
                <li key={alerta.loteDepositoId}>
                  <Link
                    to={`/lotes/${alerta.loteDepositoId}`}
                    onClick={() => setAbierta(false)}
                    className="block rounded-xl px-2 py-2 transition-colors duration-150 hover:bg-muted"
                  >
                    <span className="block font-semibold [overflow-wrap:anywhere]">{alerta.productoNombre}</span>
                    <span className="block text-sm text-muted-foreground">
                      Lote {alerta.loteDepositoId} en depósito vence el {formatDate(alerta.fechaVencimientoDeposito)}, antes que el de góndola.
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}
