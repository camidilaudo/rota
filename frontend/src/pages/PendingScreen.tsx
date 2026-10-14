import { PageHeader } from '../components/PageHeader'

/** Lugar reservado para las pantallas que se construyen en la fase siguiente. */
export function PendingScreen({ title, description }: { title: string; description: string }) {
  return (
    <section>
      <PageHeader title={title} description={description} />
      <div className="rounded-2xl border border-dashed border-border-strong bg-card px-5 py-10 text-center">
        <p className="font-heading text-lg font-semibold">En construcción</p>
        <p className="mt-1 text-muted-foreground">Esta pantalla llega en la próxima etapa del frontend.</p>
      </div>
    </section>
  )
}
