import { Route, Routes } from 'react-router-dom'
import { RequireAuth, RequireRole } from './auth/guards'
import { AppLayout } from './layout/AppLayout'
import { SOLO_DUENO } from './navigation'
import { ConfiguracionPage } from './pages/ConfiguracionPage'
import { ConsultaPage } from './pages/ConsultaPage'
import { LoginPage } from './pages/LoginPage'
import { LotePage } from './pages/LotePage'
import { NotFoundPage } from './pages/NotFoundPage'
import { RecepcionPage } from './pages/RecepcionPage'
import { ReponerPage } from './pages/ReponerPage'
import { RutaDelDiaPage } from './pages/RutaDelDiaPage'
import { VentasPage } from './pages/VentasPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route index element={<RutaDelDiaPage />} />
          <Route path="ventas" element={<VentasPage />} />
          <Route path="lotes/:loteId" element={<LotePage />} />
          <Route path="recepcion" element={<RecepcionPage />} />
          <Route path="reponer" element={<ReponerPage />} />
          <Route path="consulta" element={<ConsultaPage />} />
          <Route element={<RequireRole roles={SOLO_DUENO} />}>
            <Route path="configuracion" element={<ConfiguracionPage />} />
          </Route>
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}
