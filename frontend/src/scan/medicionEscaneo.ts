/**
 * NF-02: mediciones de rendimiento del escaneo, guardadas en este dispositivo.
 * Criterio: identificar el producto en menos de 2 s desde la lectura válida del código.
 */
export type ResultadoIdentificacion = 'encontrado' | 'no-existe' | 'error'

export interface MedicionEscaneo {
  fecha: string
  codigo: string
  resultado: ResultadoIdentificacion
  /** Desde la lectura válida del código hasta la respuesta de identificación. Es la métrica de NF-02. */
  lecturaAIdentificacionMs: number
  /** Desde que se abre la cámara hasta la identificación. Incluye permisos, enfoque y encuadre del usuario. */
  aperturaAIdentificacionMs: number
}

export interface ResumenMetrica {
  cantidad: number
  promedioMs: number | null
  maximoMs: number | null
}

export const UMBRAL_NF02_MS = 2000
const STORAGE_KEY = 'rota.nf02.mediciones'
const MAXIMO_GUARDADO = 200

export function leerMediciones(): MedicionEscaneo[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const parsed: unknown = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? (parsed as MedicionEscaneo[]) : []
  } catch {
    return []
  }
}

export function guardarMedicion(medicion: MedicionEscaneo): void {
  try {
    const todas = [...leerMediciones(), medicion].slice(-MAXIMO_GUARDADO)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(todas))
  } catch {
    // Sin storage disponible la medición se pierde, pero el escaneo sigue funcionando.
  }
}

export function borrarMediciones(): void {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    // Nada que borrar.
  }
}

function resumir(valores: number[]): ResumenMetrica {
  if (valores.length === 0) return { cantidad: 0, promedioMs: null, maximoMs: null }
  const suma = valores.reduce((a, b) => a + b, 0)
  return { cantidad: valores.length, promedioMs: Math.round(suma / valores.length), maximoMs: Math.round(Math.max(...valores)) }
}

/** Solo las identificaciones exitosas cuentan para el criterio de NF-02. */
export function resumenMediciones(mediciones: MedicionEscaneo[]) {
  const exitosas = mediciones.filter((m) => m.resultado === 'encontrado')
  return {
    lecturaAIdentificacion: resumir(exitosas.map((m) => m.lecturaAIdentificacionMs)),
    aperturaAIdentificacion: resumir(exitosas.map((m) => m.aperturaAIdentificacionMs)),
    cumpleNF02: exitosas.length > 0 && exitosas.every((m) => m.lecturaAIdentificacionMs < UMBRAL_NF02_MS),
  }
}

export function medicionesComoCsv(mediciones: MedicionEscaneo[]): string {
  const filas = mediciones.map((m) =>
    [m.fecha, m.codigo, m.resultado, Math.round(m.lecturaAIdentificacionMs), Math.round(m.aperturaAIdentificacionMs)].join(','),
  )
  return ['fecha,codigo,resultado,lectura_a_identificacion_ms,apertura_a_identificacion_ms', ...filas].join('\n')
}
