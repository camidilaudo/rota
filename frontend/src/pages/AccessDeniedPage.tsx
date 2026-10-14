import { Link } from 'react-router-dom'
import { Icon } from '../components/Icon'

export function AccessDeniedPage() {
  return (
    <section className="mx-auto max-w-md py-10 text-center">
      <span className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-muted text-muted-foreground">
        <Icon name="lock" className="h-7 w-7" />
      </span>
      <h1 className="mt-4 text-2xl font-bold">No tenés acceso</h1>
      <p className="mt-2 text-muted-foreground">Esta sección es solo para el dueño del comercio.</p>
      <Link
        to="/"
        className="mt-6 inline-flex min-h-12 items-center justify-center rounded-xl bg-primary px-5 font-bold text-on-primary transition-colors duration-150 hover:bg-primary-strong"
      >
        Ir a la ruta del día
      </Link>
    </section>
  )
}
