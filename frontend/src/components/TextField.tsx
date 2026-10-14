import { useId, type InputHTMLAttributes, type ReactNode } from 'react'

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
  hint?: string
  /** Botón o ícono dentro del campo, a la derecha. */
  trailing?: ReactNode
}

export function TextField({ label, error, hint, trailing, id, className = '', ...props }: TextFieldProps) {
  const autoId = useId()
  const inputId = id ?? autoId
  const describedBy = error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined

  return (
    <div className={className}>
      <label htmlFor={inputId} className="mb-1.5 block text-sm font-semibold text-foreground">
        {label}
      </label>
      <div className="relative">
        <input
          id={inputId}
          aria-invalid={error ? true : undefined}
          aria-describedby={describedBy}
          className={`block min-h-12 w-full min-w-0 rounded-xl border bg-card px-4 text-base text-foreground placeholder:text-muted-foreground/70 transition-[border-color,box-shadow] duration-150 focus:outline-none focus:ring-3 focus:ring-ring/25 ${
            error ? 'border-destructive focus:border-destructive' : 'border-border-strong focus:border-primary'
          } ${trailing ? 'pr-14' : ''}`}
          {...props}
        />
        {trailing && <div className="absolute inset-y-0 right-0 flex items-center pr-1">{trailing}</div>}
      </div>
      {error ? (
        <p id={`${inputId}-error`} className="mt-1.5 text-sm text-destructive">
          {error}
        </p>
      ) : hint ? (
        <p id={`${inputId}-hint`} className="mt-1.5 text-sm text-muted-foreground">
          {hint}
        </p>
      ) : null}
    </div>
  )
}
