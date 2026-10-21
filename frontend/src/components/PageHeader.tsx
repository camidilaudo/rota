export function PageHeader({ title, description }: { title: string; description?: string }) {
  return (
    <div className="mb-5 lg:mb-8">
      <h1 className="text-2xl font-bold text-foreground lg:text-3xl">{title}</h1>
      {description && <p className="mt-1 text-base text-muted-foreground">{description}</p>}
    </div>
  )
}
