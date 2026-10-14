import { useId } from 'react'

interface Option<T extends string> {
  value: T
  label: string
}

export function Segmented<T extends string>({
  label,
  options,
  value,
  onChange,
}: {
  label: string
  options: Option<T>[]
  value: T
  onChange: (value: T) => void
}) {
  const id = useId()
  return (
    <fieldset>
      <legend id={id} className="mb-1.5 block text-sm font-semibold text-foreground">
        {label}
      </legend>
      <div className="grid gap-2" style={{ gridTemplateColumns: `repeat(${options.length}, minmax(0, 1fr))` }}>
        {options.map((opt) => {
          const selected = opt.value === value
          return (
            <button
              key={opt.value}
              type="button"
              aria-pressed={selected}
              onClick={() => onChange(opt.value)}
              className={`min-h-12 rounded-xl border px-3 text-base font-bold transition-[background-color,border-color,color,transform] duration-150 ease-out active:scale-[0.97] ${
                selected
                  ? 'border-primary bg-primary text-on-primary'
                  : 'border-border-strong bg-card text-foreground hover:bg-muted'
              }`}
            >
              {opt.label}
            </button>
          )
        })}
      </div>
    </fieldset>
  )
}
