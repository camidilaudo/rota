export function BrandMark({ size = 'md' }: { size?: 'md' | 'lg' }) {
  const box = size === 'lg' ? 'h-14 w-14 text-2xl rounded-2xl' : 'h-9 w-9 text-lg rounded-xl'
  return (
    <span
      aria-hidden="true"
      className={`inline-flex shrink-0 items-center justify-center bg-primary font-heading font-bold text-on-primary ${box}`}
    >
      R
    </span>
  )
}
