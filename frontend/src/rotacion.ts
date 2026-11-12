import type { AlertaRotacion } from './types/domain'

export interface AlertaAgrupada {
  alerta: AlertaRotacion
  lotesGondola: number
}

/** El backend emite una alerta por cada lote en góndola; acá se agrupan por lote de depósito. */
export function agruparAlertas(alertas: AlertaRotacion[]): AlertaAgrupada[] {
  const porLote = new Map<number, AlertaAgrupada>()
  for (const a of alertas) {
    const actual = porLote.get(a.loteDepositoId)
    if (actual) actual.lotesGondola += 1
    else porLote.set(a.loteDepositoId, { alerta: a, lotesGondola: 1 })
  }
  return [...porLote.values()]
}
