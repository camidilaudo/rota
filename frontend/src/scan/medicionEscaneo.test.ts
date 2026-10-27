import { describe, expect, it } from 'vitest'
import { medicionesComoCsv, resumenMediciones, type MedicionEscaneo } from './medicionEscaneo'

const m = (resultado: MedicionEscaneo['resultado'], lectura: number, apertura: number): MedicionEscaneo => ({
  fecha: '2026-10-27T20:00:00.000Z',
  codigo: '7790001000011',
  resultado,
  lecturaAIdentificacionMs: lectura,
  aperturaAIdentificacionMs: apertura,
})

describe('resumen NF-02', () => {
  it('calcula promedio y máximo solo con identificaciones exitosas', () => {
    const r = resumenMediciones([m('encontrado', 100, 3000), m('encontrado', 300, 5000), m('no-existe', 9000, 9000)])

    expect(r.lecturaAIdentificacion).toEqual({ cantidad: 2, promedioMs: 200, maximoMs: 300 })
    expect(r.aperturaAIdentificacion).toEqual({ cantidad: 2, promedioMs: 4000, maximoMs: 5000 })
    expect(r.cumpleNF02).toBe(true)
  })

  it('no cumple si alguna identificación llega a 2 segundos desde la lectura', () => {
    expect(resumenMediciones([m('encontrado', 150, 900), m('encontrado', 2000, 4000)]).cumpleNF02).toBe(false)
  })

  it('sin identificaciones exitosas no hay datos ni cumplimiento', () => {
    const r = resumenMediciones([m('error', 50, 60)])
    expect(r.lecturaAIdentificacion).toEqual({ cantidad: 0, promedioMs: null, maximoMs: null })
    expect(r.cumpleNF02).toBe(false)
  })

  it('exporta CSV con una fila por medición', () => {
    expect(medicionesComoCsv([m('encontrado', 123.4, 2500.6)]).split('\n')).toEqual([
      'fecha,codigo,resultado,lectura_a_identificacion_ms,apertura_a_identificacion_ms',
      '2026-10-27T20:00:00.000Z,7790001000011,encontrado,123,2501',
    ])
  })
})
