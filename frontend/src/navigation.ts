import type { Rol } from './types/auth'
import type { IconName } from './components/Icon'

export const TODOS_LOS_ROLES: readonly Rol[] = ['ROLE_DUENO', 'ROLE_REPOSITOR']
export const SOLO_DUENO: readonly Rol[] = ['ROLE_DUENO']

export interface NavItem {
  path: string
  label: string
  icon: IconName
  roles: readonly Rol[]
}

export const NAV_ITEMS: readonly NavItem[] = [
  { path: '/', label: 'Ruta del día', icon: 'route', roles: TODOS_LOS_ROLES },
  { path: '/ventas', label: 'Ventas', icon: 'cart', roles: TODOS_LOS_ROLES },
  { path: '/recepcion', label: 'Recepción', icon: 'package', roles: TODOS_LOS_ROLES },
  { path: '/reponer', label: 'Reponer', icon: 'swap', roles: TODOS_LOS_ROLES },
  { path: '/consulta', label: 'Consulta', icon: 'search', roles: TODOS_LOS_ROLES },
  { path: '/merma', label: 'Merma', icon: 'chart', roles: SOLO_DUENO },
  { path: '/configuracion', label: 'Configuración', icon: 'settings', roles: SOLO_DUENO },
]

export function navItemsFor(rol: Rol): NavItem[] {
  return NAV_ITEMS.filter((item) => item.roles.includes(rol))
}

export const ROL_LABEL: Record<Rol, string> = {
  ROLE_DUENO: 'Dueño',
  ROLE_REPOSITOR: 'Encargado',
}
