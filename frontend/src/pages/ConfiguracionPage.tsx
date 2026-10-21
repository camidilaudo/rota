import { useState, type FormEvent } from 'react'
import { ApiError } from '../api/client'
import { actualizarConfiguracionCategoria, crearCategoria, crearUsuario, getCategorias } from '../api/endpoints'
import { Button } from '../components/Button'
import { PageHeader } from '../components/PageHeader'
import { Segmented } from '../components/Segmented'
import { SelectField } from '../components/SelectField'
import { EmptyState, ErrorState, InlineAlert, LoadingList } from '../components/States'
import { TextField } from '../components/TextField'
import { formatNumber } from '../format'
import { errorMessage, useApi } from '../hooks/useApi'
import { ROL_LABEL } from '../navigation'
import type { Rol } from '../types/auth'
import type { Categoria, ModoNotificacion } from '../types/domain'

type Errores = Record<string, string>

const MODOS: { value: ModoNotificacion; label: string }[] = [
  { value: 'DIARIO', label: 'Diario' },
  { value: 'CADA_2_DIAS', label: 'Cada 2 días' },
  { value: 'SEMANAL', label: 'Semanal' },
  { value: 'PANTALLA', label: 'Solo en pantalla' },
]
const MODO_LABEL = Object.fromEntries(MODOS.map((m) => [m.value, m.label])) as Record<ModoNotificacion, string>

const dias = (n: number) => (n === 1 ? '1 día' : `${formatNumber(n)} días`)

function ModoSelect({ value, onChange }: { value: ModoNotificacion; onChange: (m: ModoNotificacion) => void }) {
  return (
    <SelectField label="Modo de aviso" value={value} onChange={(e) => onChange(e.target.value as ModoNotificacion)}>
      {MODOS.map((m) => (
        <option key={m.value} value={m.value}>
          {m.label}
        </option>
      ))}
    </SelectField>
  )
}

function EditarCategoria({ categoria, onGuardada, onCancelar }: { categoria: Categoria; onGuardada: () => void; onCancelar: () => void }) {
  const [critico, setCritico] = useState(String(categoria.diasUmbralCritico))
  const [modo, setModo] = useState<ModoNotificacion>(categoria.modoNotificacion)
  const [error, setError] = useState<string | null>(null)
  const [formError, setFormError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const n = Number(critico)
    if (!Number.isInteger(n) || n < 1) return setError('Ingresá al menos 1 día.')
    if (n > categoria.diasUmbralAlerta) return setError(`No puede superar el umbral de alerta (${dias(categoria.diasUmbralAlerta)}).`)
    setError(null)
    setFormError(null)
    setGuardando(true)
    try {
      await actualizarConfiguracionCategoria(categoria.id, { diasUmbral: n, modoNotificacion: modo })
      onGuardada()
    } catch (err) {
      setFormError(errorMessage(err))
      setGuardando(false)
    }
  }

  return (
    <form noValidate onSubmit={guardar} className="mt-4 space-y-4 border-t border-border pt-4">
      {formError && <InlineAlert kind="error">{formError}</InlineAlert>}
      <div className="grid gap-3 sm:grid-cols-2">
        <TextField
          label="Umbral crítico (días antes de vencer)"
          inputMode="numeric"
          value={critico}
          onChange={(e) => setCritico(e.target.value.replace(/\D/g, ''))}
          error={error ?? undefined}
        />
        <ModoSelect value={modo} onChange={setModo} />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <Button type="submit" loading={guardando}>
          {guardando ? 'Guardando' : 'Guardar'}
        </Button>
        <Button variant="secondary" onClick={onCancelar}>
          Cancelar
        </Button>
      </div>
    </form>
  )
}

function NuevaCategoria({ onCreada, onCancelar }: { onCreada: (nombre: string) => void; onCancelar: () => void }) {
  const [form, setForm] = useState({ nombre: '', alerta: '', critico: '', modo: 'DIARIO' as ModoNotificacion })
  const [errores, setErrores] = useState<Errores>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const alerta = Number(form.alerta)
    const critico = Number(form.critico)
    const errs: Errores = {}
    if (!form.nombre.trim()) errs.nombre = 'Ingresá el nombre.'
    if (!Number.isInteger(alerta) || alerta < 1) errs.diasUmbralAlerta = 'Ingresá al menos 1 día.'
    if (!Number.isInteger(critico) || critico < 1) errs.diasUmbralCritico = 'Ingresá al menos 1 día.'
    else if (!errs.diasUmbralAlerta && critico > alerta) errs.diasUmbralCritico = 'No puede superar el umbral de alerta.'
    setErrores(errs)
    setFormError(null)
    if (Object.keys(errs).length) return
    setGuardando(true)
    try {
      const creada = await crearCategoria({ nombre: form.nombre.trim(), diasUmbralAlerta: alerta, diasUmbralCritico: critico, modoNotificacion: form.modo })
      onCreada(creada.nombre)
    } catch (err) {
      if (err instanceof ApiError) setErrores(err.fieldErrors)
      setFormError(errorMessage(err))
      setGuardando(false)
    }
  }

  return (
    <form noValidate onSubmit={guardar} className="space-y-4 rounded-2xl border border-border bg-card p-4">
      <h3 className="font-heading text-lg font-semibold">Nueva categoría</h3>
      {formError && <InlineAlert kind="error">{formError}</InlineAlert>}
      <TextField label="Nombre" value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} error={errores.nombre} />
      <div className="grid grid-cols-2 gap-3">
        <TextField
          label="Alerta (días)"
          inputMode="numeric"
          value={form.alerta}
          onChange={(e) => setForm({ ...form, alerta: e.target.value.replace(/\D/g, '') })}
          error={errores.diasUmbralAlerta}
        />
        <TextField
          label="Crítico (días)"
          inputMode="numeric"
          value={form.critico}
          onChange={(e) => setForm({ ...form, critico: e.target.value.replace(/\D/g, '') })}
          error={errores.diasUmbralCritico}
        />
      </div>
      <ModoSelect value={form.modo} onChange={(modo) => setForm({ ...form, modo })} />
      <div className="grid grid-cols-2 gap-3">
        <Button type="submit" loading={guardando}>
          {guardando ? 'Creando' : 'Crear'}
        </Button>
        <Button variant="secondary" onClick={onCancelar}>
          Cancelar
        </Button>
      </div>
    </form>
  )
}

function Categorias() {
  const { data, error, loading, reload } = useApi((signal) => getCategorias(signal))
  const [editando, setEditando] = useState<number | null>(null)
  const [creando, setCreando] = useState(false)
  const [aviso, setAviso] = useState<string | null>(null)

  return (
    <section aria-labelledby="categorias-titulo" className="space-y-4">
      <div>
        <h2 id="categorias-titulo" className="text-xl font-bold">
          Categorías
        </h2>
        <p className="text-muted-foreground">Cuántos días antes de vencer un lote pasa a alerta y a crítico, y cómo se avisa.</p>
      </div>
      {aviso && <InlineAlert kind="success">{aviso}</InlineAlert>}
      {loading && !data ? (
        <LoadingList rows={3} label="Cargando categorías" />
      ) : error ? (
        <ErrorState message={error} onRetry={reload} />
      ) : data && data.length === 0 ? (
        <EmptyState title="Sin categorías">Creá la primera para empezar a controlar vencimientos.</EmptyState>
      ) : (
        <ul className="grid gap-3 lg:grid-cols-2">
          {data?.map((c) => (
            <li key={c.id} className="rounded-2xl border border-border bg-card p-4">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="font-heading text-lg font-semibold [overflow-wrap:anywhere]">
                    {c.nombre}
                    {c.esPiloto && (
                      <span className="ml-2 inline-block rounded-full bg-primary/15 px-2 py-0.5 align-middle text-xs font-bold text-primary-strong">Piloto</span>
                    )}
                  </p>
                  <p className="text-sm text-muted-foreground">
                    Alerta: {dias(c.diasUmbralAlerta)} · Crítico: {dias(c.diasUmbralCritico)}
                  </p>
                  <p className="text-sm text-muted-foreground">Aviso: {MODO_LABEL[c.modoNotificacion]}</p>
                </div>
                {editando !== c.id && (
                  <Button
                    variant="secondary"
                    className="shrink-0 px-4"
                    aria-label={`Editar ${c.nombre}`}
                    onClick={() => {
                      setEditando(c.id)
                      setAviso(null)
                    }}
                  >
                    Editar
                  </Button>
                )}
              </div>
              {editando === c.id && (
                <EditarCategoria
                  categoria={c}
                  onCancelar={() => setEditando(null)}
                  onGuardada={() => {
                    setEditando(null)
                    setAviso(`Guardamos los cambios de ${c.nombre}.`)
                    reload()
                  }}
                />
              )}
            </li>
          ))}
        </ul>
      )}
      {creando ? (
        <NuevaCategoria
          onCancelar={() => setCreando(false)}
          onCreada={(nombre) => {
            setCreando(false)
            setAviso(`Creamos la categoría ${nombre}.`)
            reload()
          }}
        />
      ) : (
        <Button variant="secondary" fullWidth onClick={() => setCreando(true)}>
          Agregar categoría
        </Button>
      )}
    </section>
  )
}

const ROLES: { value: Rol; label: string }[] = [
  { value: 'ROLE_REPOSITOR', label: ROL_LABEL.ROLE_REPOSITOR },
  { value: 'ROLE_DUENO', label: ROL_LABEL.ROLE_DUENO },
]

function AltaUsuario() {
  const vacio = { nombre: '', email: '', password: '', rol: 'ROLE_REPOSITOR' as Rol }
  const [form, setForm] = useState(vacio)
  const [errores, setErrores] = useState<Errores>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [guardando, setGuardando] = useState(false)

  async function guardar(e: FormEvent) {
    e.preventDefault()
    const errs: Errores = {}
    if (!form.nombre.trim()) errs.nombre = 'Ingresá el nombre.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) errs.email = 'Ingresá un email válido.'
    if (form.password.length < 6) errs.password = 'Usá al menos 6 caracteres.'
    setErrores(errs)
    setFormError(null)
    setAviso(null)
    if (Object.keys(errs).length) return
    setGuardando(true)
    try {
      const u = await crearUsuario({ nombre: form.nombre.trim(), email: form.email.trim(), password: form.password, rol: form.rol })
      setAviso(`Creamos a ${u.nombre} (${ROL_LABEL[u.rol]}). Ya puede iniciar sesión con ${u.email}.`)
      setForm(vacio)
    } catch (err) {
      if (err instanceof ApiError) setErrores(err.fieldErrors)
      setFormError(errorMessage(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <section aria-labelledby="usuarios-titulo" className="space-y-4">
      <div>
        <h2 id="usuarios-titulo" className="text-xl font-bold">
          Usuarios
        </h2>
        <p className="text-muted-foreground">Da de alta a quien trabaja en el comercio.</p>
      </div>
      <form noValidate onSubmit={guardar} className="space-y-4 rounded-2xl border border-border bg-card p-4">
        {aviso && <InlineAlert kind="success">{aviso}</InlineAlert>}
        {formError && <InlineAlert kind="error">{formError}</InlineAlert>}
        <TextField label="Nombre" autoComplete="off" value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} error={errores.nombre} />
        <TextField
          label="Email"
          type="email"
          inputMode="email"
          autoComplete="off"
          autoCapitalize="none"
          spellCheck={false}
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          error={errores.email}
        />
        <TextField
          label="Contraseña inicial"
          type="password"
          autoComplete="new-password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          error={errores.password}
          hint="Mínimo 6 caracteres."
        />
        <Segmented label="Rol" options={ROLES} value={form.rol} onChange={(rol) => setForm({ ...form, rol })} />
        <Button type="submit" fullWidth loading={guardando}>
          {guardando ? 'Creando' : 'Crear usuario'}
        </Button>
      </form>
    </section>
  )
}

export function ConfiguracionPage() {
  return (
    <div className="space-y-10">
      <PageHeader title="Configuración" description="Categorías y usuarios del comercio." />
      <Categorias />
      <AltaUsuario />
    </div>
  )
}
