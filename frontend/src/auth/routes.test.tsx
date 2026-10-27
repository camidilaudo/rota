import { screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { renderApp } from '../test/renderApp'

function navLabels(): string[] {
  const navs = screen.getAllByRole('navigation', { name: 'Principal', hidden: true })
  return within(navs[0]).getAllByRole('link', { hidden: true }).map((a) => a.textContent ?? '')
}

describe('rutas protegidas', () => {
  it('sin sesión, cualquier pantalla manda al login', () => {
    renderApp('/reponer')
    expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument()
  })

  it('el repositor no puede entrar a Configuración escribiendo la URL', () => {
    renderApp('/configuracion', 'ROLE_REPOSITOR')
    expect(screen.getByRole('heading', { name: 'No tenés acceso' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Configuración' })).not.toBeInTheDocument()
  })

  it('el dueño sí entra a Configuración', () => {
    renderApp('/configuracion', 'ROLE_DUENO')
    expect(screen.getByRole('heading', { name: 'Configuración' })).toBeInTheDocument()
  })

  it('una URL desconocida muestra la pantalla de no encontrado', () => {
    renderApp('/no-existe', 'ROLE_REPOSITOR')
    expect(screen.getByRole('heading', { name: 'No encontramos esta pantalla' })).toBeInTheDocument()
  })
})

describe('menú según rol', () => {
  it('el repositor no ve Configuración', () => {
    renderApp('/', 'ROLE_REPOSITOR')
    expect(navLabels()).toEqual(['Ruta del día', 'Ventas', 'Recepción', 'Reponer', 'Consulta'])
  })

  it('el dueño ve Configuración', () => {
    renderApp('/', 'ROLE_DUENO')
    expect(navLabels()).toEqual(['Ruta del día', 'Ventas', 'Recepción', 'Reponer', 'Consulta', 'Configuración'])
  })
})
