import { useId, type ReactNode, type SelectHTMLAttributes } from 'react'

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  error?: string
  children: ReactNode
}

export function SelectField({ label, error, id, className = '', children, ...props }: SelectFieldProps) {
  const autoId = useId()
  const selectId = id ?? autoId
  return (
    <div className={className}>
      <label htmlFor={selectId} className="mb-1.5 block text-sm font-semibold text-foreground">
        {label}
      </label>
      <select
        id={selectId}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? `${selectId}-error` : undefined}
        className={`block min-h-12 w-full min-w-0 rounded-xl border bg-card px-4 text-base text-foreground transition-[border-color,box-shadow] duration-150 focus:outline-none focus:ring-3 focus:ring-ring/25 ${
          error ? 'border-destructive' : 'border-border-strong focus:border-primary'
        }`}
        {...props}
      >
        {children}
      </select>
      {error && (
        <p id={`${selectId}-error`} className="mt-1.5 text-sm text-destructive">
          {error}
        </p>
      )}
    </div>
  )
}
