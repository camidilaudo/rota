import type { ReactNode } from 'react'
import { Button } from './Button'
import { Icon } from './Icon'

export function LoadingList({ rows = 3, label = 'Cargando' }: { rows?: number; label?: string }) {
  return (
    <div role="status" aria-label={label} className="space-y-3">
      {Array.from({ length: rows }, (_, i) => (
        <div key={i} className="h-24 animate-pulse rounded-2xl border border-border bg-card" />
      ))}
    </div>
  )
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div role="alert" className="rounded-2xl border border-red-200 bg-red-50 px-5 py-6 text-center">
      <Icon name="alert" className="mx-auto h-7 w-7 text-red-700" />
      <p className="mt-2 font-semibold text-red-900">{message}</p>
      {onRetry && (
        <Button variant="secondary" className="mt-4" onClick={onRetry}>
          Reintentar
        </Button>
      )}
    </div>
  )
}

export function EmptyState({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <div className="rounded-2xl border border-dashed border-border-strong bg-card px-5 py-10 text-center">
      <p className="font-heading text-lg font-semibold">{title}</p>
      {children && <div className="mt-1 text-muted-foreground">{children}</div>}
    </div>
  )
}

export function InlineAlert({ kind, children }: { kind: 'error' | 'success' | 'info'; children: ReactNode }) {
  const styles = {
    error: 'bg-red-50 text-red-800',
    success: 'bg-emerald-50 text-emerald-900',
    info: 'bg-sky-50 text-sky-900',
  }
  return (
    <div role={kind === 'error' ? 'alert' : 'status'} className={`rounded-xl px-4 py-3 text-sm font-semibold ${styles[kind]}`}>
      {children}
    </div>
  )
}
