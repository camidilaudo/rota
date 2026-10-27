import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { BrandMark } from '../components/Brand'
import { Icon } from '../components/Icon'
import { ROL_LABEL, navItemsFor, type NavItem } from '../navigation'

const SHORT_LABEL: Record<string, string> = {
  '/': 'Ruta',
  '/recepcion': 'Recibir',
  '/configuracion': 'Config.',
}

function SidebarLink({ item }: { item: NavItem }) {
  return (
    <NavLink
      to={item.path}
      end={item.path === '/'}
      className={({ isActive }) =>
        `flex min-h-12 items-center gap-3 rounded-xl px-3 text-base font-semibold transition-colors duration-150 ${
          isActive ? 'bg-primary/12 text-primary-strong' : 'text-muted-foreground hover:bg-muted hover:text-foreground'
        }`
      }
    >
      <Icon name={item.icon} className="h-5 w-5 shrink-0" />
      <span className="truncate">{item.label}</span>
    </NavLink>
  )
}

function TabLink({ item }: { item: NavItem }) {
  const short = SHORT_LABEL[item.path]
  return (
    <NavLink
      to={item.path}
      end={item.path === '/'}
      aria-label={short ? item.label : undefined}
      className={({ isActive }) =>
        `flex min-h-14 min-w-0 flex-1 flex-col items-center justify-center gap-0.5 px-0.5 text-xs font-bold tracking-tight transition-colors duration-150 ${
          isActive ? 'text-primary-strong' : 'text-muted-foreground'
        }`
      }
    >
      {({ isActive }) => (
        <>
          <span
            className={`flex h-8 w-14 items-center justify-center rounded-full transition-colors duration-150 ${
              isActive ? 'bg-primary/15' : ''
            }`}
          >
            <Icon name={item.icon} className="h-5 w-5" />
          </span>
          <span className="max-w-full truncate">{short ?? item.label}</span>
        </>
      )}
    </NavLink>
  )
}

export function AppLayout() {
  const { session, logout } = useAuth()
  if (!session) return null

  const items = navItemsFor(session.rol)
  const comercio = session.comercioNombre ?? 'Tu comercio'

  return (
    <div className="min-h-dvh lg:flex">
      <a
        href="#contenido"
        className="sr-only z-50 rounded-lg bg-card px-4 py-3 font-semibold focus:not-sr-only focus:fixed focus:left-3 focus:top-3"
      >
        Ir al contenido
      </a>

      {/* Escritorio: barra lateral */}
      <aside className="sticky top-0 hidden h-dvh w-64 shrink-0 flex-col border-r border-border bg-card lg:flex">
        <div className="flex items-center gap-3 px-5 py-5">
          <BrandMark />
          <div className="min-w-0">
            <p className="font-heading text-lg font-bold leading-tight">Rotá</p>
            <p className="truncate text-sm text-muted-foreground" title={comercio}>
              {comercio}
            </p>
          </div>
        </div>
        <nav aria-label="Principal" className="flex-1 space-y-1 overflow-y-auto px-3">
          {items.map((item) => (
            <SidebarLink key={item.path} item={item} />
          ))}
        </nav>
        <div className="border-t border-border p-4">
          <p className="truncate text-sm font-semibold" title={session.email}>
            {session.email}
          </p>
          <p className="text-sm text-muted-foreground">{ROL_LABEL[session.rol]}</p>
          <button
            type="button"
            onClick={logout}
            className="mt-3 flex min-h-11 w-full items-center justify-center gap-2 rounded-xl border border-border-strong text-sm font-bold text-foreground transition-colors duration-150 hover:bg-muted"
          >
            <Icon name="logout" className="h-4 w-4" />
            Cerrar sesión
          </button>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        {/* Celular: encabezado */}
        <header className="sticky top-0 z-20 border-b border-border bg-card/95 pt-[env(safe-area-inset-top)] backdrop-blur lg:hidden">
          <div className="flex min-h-14 items-center gap-3 pl-4 pr-1">
            <BrandMark />
            <div className="min-w-0 flex-1">
              <p className="truncate font-heading text-base font-bold leading-tight" title={comercio}>
                {comercio}
              </p>
              <p className="truncate text-xs text-muted-foreground">
                {ROL_LABEL[session.rol]} · {session.email}
              </p>
            </div>
            <button
              type="button"
              onClick={logout}
              aria-label="Cerrar sesión"
              className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl text-muted-foreground transition-colors duration-150 hover:bg-muted active:bg-muted"
            >
              <Icon name="logout" className="h-6 w-6" />
            </button>
          </div>
        </header>

        <main
          id="contenido"
          className="mx-auto w-full max-w-5xl flex-1 px-4 pt-5 pb-[calc(6rem+env(safe-area-inset-bottom))] sm:px-6 lg:px-10 lg:pt-8 lg:pb-10"
        >
          <Outlet />
        </main>

        {/* Celular: barra inferior al alcance del pulgar */}
        <nav
          aria-label="Principal"
          className="fixed inset-x-0 bottom-0 z-20 border-t border-border bg-card pb-[env(safe-area-inset-bottom)] lg:hidden"
        >
          <div className="mx-auto flex max-w-lg">
            {items.map((item) => (
              <TabLink key={item.path} item={item} />
            ))}
          </div>
        </nav>
      </div>
    </div>
  )
}
