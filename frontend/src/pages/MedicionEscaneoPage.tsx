import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '../components/Button'
import { PageHeader } from '../components/PageHeader'
import { EmptyState, InlineAlert } from '../components/States'
import { formatDateTime, formatNumber } from '../format'
import {
  UMBRAL_NF02_MS,
  borrarMediciones,
  leerMediciones,
  medicionesComoCsv,
  resumenMediciones,
  type ResultadoIdentificacion,
  type ResumenMetrica,
} from '../scan/medicionEscaneo'

const RESULTADO_LABEL: Record<ResultadoIdentificacion, string> = {
  encontrado: 'Identificado',
  'no-existe': 'Código no cargado',
  error: 'Error',
}

const ms = (valor: number | null) => (valor === null ? 'Sin datos' : `${formatNumber(Math.round(valor))} ms`)

function Metrica({ titulo, descripcion, resumen }: { titulo: string; descripcion: string; resumen: ResumenMetrica }) {
  return (
    <div className="rounded-2xl border border-border bg-card p-4">
      <p className="font-heading text-base font-semibold">{titulo}</p>
      <p className="text-sm text-muted-foreground">{descripcion}</p>
      <dl className="mt-3 grid grid-cols-2 gap-3">
        <div>
          <dt className="text-sm text-muted-foreground">Promedio</dt>
          <dd className="font-heading text-xl font-bold">{ms(resumen.promedioMs)}</dd>
        </div>
        <div>
          <dt className="text-sm text-muted-foreground">Máximo</dt>
          <dd className="font-heading text-xl font-bold">{ms(resumen.maximoMs)}</dd>
        </div>
      </dl>
    </div>
  )
}

/** NF-02: resultados de las pruebas de escaneo hechas en este dispositivo. */
export function MedicionEscaneoPage() {
  const [mediciones, setMediciones] = useState(leerMediciones)
  const [copiado, setCopiado] = useState(false)
  const resumen = resumenMediciones(mediciones)
  const exitosas = resumen.lecturaAIdentificacion.cantidad

  async function copiarCsv() {
    try {
      await navigator.clipboard.writeText(medicionesComoCsv(mediciones))
      setCopiado(true)
    } catch {
      setCopiado(false)
    }
  }

  return (
    <section className="max-w-3xl space-y-5">
      <PageHeader
        title="Medición del escaneo"
        description="NF-02: el producto se tiene que identificar en menos de 2 segundos desde que se lee el código."
      />

      {mediciones.length === 0 ? (
        <EmptyState title="Todavía no hay mediciones">
          Escaneá productos desde <Link to="/recepcion" className="font-semibold text-primary-strong underline">Recepción</Link>. Cada escaneo se registra
          acá.
        </EmptyState>
      ) : (
        <>
          {exitosas > 0 ? (
            <InlineAlert kind={resumen.cumpleNF02 ? 'success' : 'error'}>
              {resumen.cumpleNF02
                ? `Cumple NF-02: las ${formatNumber(exitosas)} identificaciones tardaron menos de ${formatNumber(UMBRAL_NF02_MS)} ms desde la lectura.`
                : `No cumple NF-02: al menos una identificación tardó ${formatNumber(UMBRAL_NF02_MS)} ms o más desde la lectura.`}
            </InlineAlert>
          ) : (
            <InlineAlert kind="info">Ningún escaneo identificó un producto todavía: el criterio se evalúa sobre identificaciones exitosas.</InlineAlert>
          )}

          <div className="grid gap-3 sm:grid-cols-2">
            <Metrica
              titulo="Lectura a identificación"
              descripcion="Desde la lectura válida del código hasta tener el producto. Es la métrica de NF-02."
              resumen={resumen.lecturaAIdentificacion}
            />
            <Metrica
              titulo="Apertura a identificación"
              descripcion="Desde que se abre la cámara. Incluye permisos y el tiempo de encuadrar: es complementaria."
              resumen={resumen.aperturaAIdentificacion}
            />
          </div>

          <div className="overflow-x-auto rounded-2xl border border-border bg-card">
            <table className="w-full min-w-[34rem] text-left text-sm">
              <thead className="text-muted-foreground">
                <tr className="border-b border-border">
                  <th className="px-4 py-2 font-semibold">#</th>
                  <th className="px-4 py-2 font-semibold">Fecha</th>
                  <th className="px-4 py-2 font-semibold">Código</th>
                  <th className="px-4 py-2 font-semibold">Resultado</th>
                  <th className="px-4 py-2 text-right font-semibold">Lectura a identificación</th>
                  <th className="px-4 py-2 text-right font-semibold">Apertura a identificación</th>
                </tr>
              </thead>
              <tbody>
                {mediciones.map((m, i) => (
                  <tr key={`${m.fecha}-${i}`} className="border-b border-border last:border-0">
                    <td className="px-4 py-2 text-muted-foreground">{i + 1}</td>
                    <td className="px-4 py-2">{formatDateTime(m.fecha.slice(0, 19))}</td>
                    <td className="px-4 py-2 font-mono">{m.codigo}</td>
                    <td className="px-4 py-2">{RESULTADO_LABEL[m.resultado]}</td>
                    <td className="px-4 py-2 text-right font-semibold">{ms(m.lecturaAIdentificacionMs)}</td>
                    <td className="px-4 py-2 text-right">{ms(m.aperturaAIdentificacionMs)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <Button variant="secondary" onClick={copiarCsv}>
              {copiado ? 'CSV copiado' : 'Copiar resultados (CSV)'}
            </Button>
            <Button
              variant="ghost"
              onClick={() => {
                borrarMediciones()
                setMediciones([])
              }}
            >
              Borrar mediciones
            </Button>
          </div>
        </>
      )}
    </section>
  )
}
