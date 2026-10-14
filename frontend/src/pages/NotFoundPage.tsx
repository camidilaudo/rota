import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="mx-auto max-w-md py-10 text-center">
      <h1 className="text-2xl font-bold">No encontramos esta pantalla</h1>
      <p className="mt-2 text-muted-foreground">Puede que el enlace esté mal escrito.</p>
      <Link
        to="/"
        className="mt-6 inline-flex min-h-12 items-center justify-center rounded-xl bg-primary px-5 font-bold text-on-primary transition-colors duration-150 hover:bg-primary-strong"
      >
        Ir a la ruta del día
      </Link>
    </section>
  )
}
