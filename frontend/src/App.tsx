import { Route, Routes } from 'react-router-dom'
import { RequireAuth, RequireRole } from './auth/guards'
import { AppLayout } from './layout/AppLayout'
import { SOLO_DUENO } from './navigation'
import { ConsultaPage } from './pages/ConsultaPage'
import { LoginPage } from './pages/LoginPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { PendingScreen } from './pages/PendingScreen'
import { RecepcionPage } from './pages/RecepcionPage'
import { ReponerPage } from './pages/ReponerPage'
import { RutaDelDiaPage } from './pages/RutaDelDiaPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route index element={<RutaDelDiaPage />} />
          <Route path="recepcion" element={<RecepcionPage />} />
          <Route path="reponer" element={<ReponerPage />} />
          <Route path="consulta" element={<ConsultaPage />} />
          <Route element={<RequireRole roles={SOLO_DUENO} />}>
            <Route
              path="configuracion"
              element={<PendingScreen title="Configuración" description="Categorías y usuarios del comercio." />}
            />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}
