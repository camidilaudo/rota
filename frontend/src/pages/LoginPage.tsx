import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { BrandMark } from '../components/Brand'
import { Button } from '../components/Button'
import { Icon } from '../components/Icon'
import { TextField } from '../components/TextField'

interface FieldErrors {
  email?: string
  password?: string
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function validate(email: string, password: string): FieldErrors {
  const errors: FieldErrors = {}
  if (!email.trim()) errors.email = 'Ingresá tu email.'
  else if (!EMAIL_PATTERN.test(email.trim())) errors.email = 'Revisá el formato del email.'
  if (!password) errors.password = 'Ingresá tu contraseña.'
  return errors
}

function loginErrorMessage(err: unknown): string {
  if (err instanceof ApiError && err.isNetworkError) return err.message
  if (err instanceof ApiError && err.status === 401) return 'Email o contraseña incorrectos.'
  // Sin DEF-01 el backend responde 500 ante credenciales inválidas.
  return 'No pudimos iniciar sesión. Revisá el email y la contraseña.'
}

export function LoginPage() {
  const { session, sessionExpired, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (session) return <Navigate to={from} replace />

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const errors = validate(email, password)
    setFieldErrors(errors)
    setFormError(null)
    if (errors.email || errors.password) return

    setSubmitting(true)
    try {
      await login(email.trim(), password)
      navigate(from, { replace: true })
    } catch (err) {
      setFormError(loginErrorMessage(err))
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-dvh flex-col justify-center px-4 pt-[calc(2rem+env(safe-area-inset-top))] pb-[calc(2rem+env(safe-area-inset-bottom))]">
      <main className="mx-auto w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center text-center">
          <BrandMark size="lg" />
          <h1 className="mt-4 text-3xl font-bold">Rotá</h1>
          <p className="mt-1 text-muted-foreground">Controlá lotes y vencimientos de tu comercio.</p>
        </div>

        <form
          noValidate
          onSubmit={handleSubmit}
          className="space-y-5 rounded-2xl border border-border bg-card p-5 sm:p-6"
          aria-describedby={formError || sessionExpired ? 'login-status' : undefined}
        >
          <div id="login-status" role="status" aria-live="polite">
            {formError ? (
              <p className="flex gap-2 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-800">
                <Icon name="alert" className="mt-0.5 h-4 w-4 shrink-0" />
                {formError}
              </p>
            ) : sessionExpired ? (
              <p className="rounded-xl bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-900">
                Tu sesión se cerró. Ingresá de nuevo para seguir.
              </p>
            ) : null}
          </div>

          <TextField
            label="Email"
            type="email"
            name="email"
            autoComplete="username"
            autoCapitalize="none"
            autoCorrect="off"
            spellCheck={false}
            inputMode="email"
            enterKeyHint="next"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            error={fieldErrors.email}
          />

          <TextField
            label="Contraseña"
            type={showPassword ? 'text' : 'password'}
            name="password"
            autoComplete="current-password"
            enterKeyHint="go"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={fieldErrors.password}
            trailing={
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                aria-pressed={showPassword}
                className="flex h-11 w-11 items-center justify-center rounded-lg text-muted-foreground transition-colors duration-150 hover:text-foreground"
              >
                <Icon name={showPassword ? 'eye-off' : 'eye'} className="h-5 w-5" />
              </button>
            }
          />

          <Button type="submit" fullWidth loading={submitting}>
            {submitting ? 'Ingresando' : 'Ingresar'}
          </Button>
        </form>
      </main>
    </div>
  )
}
