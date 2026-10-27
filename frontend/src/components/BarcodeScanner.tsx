import { useEffect, useRef, useState } from 'react'
import { BrowserMultiFormatReader, type IScannerControls } from '@zxing/browser'
import { BarcodeFormat, DecodeHintType } from '@zxing/library'
import { Button } from './Button'
import { Icon } from './Icon'

// Formatos de los códigos de los productos (EAN-13 en los datos de prueba) y los más comunes en comercios
const FORMATOS = [BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.UPC_A, BarcodeFormat.UPC_E, BarcodeFormat.CODE_128]
const AYUDA_DESPUES_DE_MS = 10000

interface BarcodeScannerProps {
  /** Se llama una sola vez, con el código y el instante de la lectura (performance.now()). */
  onDetected: (codigo: string, instanteLectura: number) => void
  onClose: () => void
}

function mensajeDeError(err: unknown): string {
  const name = err instanceof Error || err instanceof DOMException ? err.name : ''
  if (name === 'NotAllowedError' || name === 'SecurityError') {
    return 'No hay permiso para usar la cámara. Habilitalo en la configuración del navegador o tipeá el código.'
  }
  if (name === 'NotFoundError' || name === 'OverconstrainedError' || name === 'DevicesNotFoundError') {
    return 'No encontramos una cámara en este dispositivo. Tipeá el código.'
  }
  if (name === 'NotReadableError' || name === 'TrackStartError') {
    return 'La cámara está siendo usada por otra aplicación. Cerrala y probá de nuevo, o tipeá el código.'
  }
  return 'No pudimos abrir la cámara. Tipeá el código.'
}

function errorDeSoporte(): string | null {
  if (window.isSecureContext === false) {
    return 'La cámara solo funciona con conexión segura (HTTPS). Abrí Rotá con https:// o tipeá el código.'
  }
  if (!navigator.mediaDevices?.getUserMedia) return 'Este navegador no permite usar la cámara. Tipeá el código.'
  return null
}

export function BarcodeScanner({ onDetected, onClose }: BarcodeScannerProps) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const controlsRef = useRef<IScannerControls | null>(null)
  const detectadoRef = useRef(false)
  const onDetectedRef = useRef(onDetected)
  useEffect(() => {
    onDetectedRef.current = onDetected
  }, [onDetected])
  const [errorSoporte] = useState(errorDeSoporte)
  const [errorCamara, setErrorCamara] = useState<string | null>(null)
  const error = errorSoporte ?? errorCamara
  const [listo, setListo] = useState(false)
  const [mostrarAyuda, setMostrarAyuda] = useState(false)

  useEffect(() => {
    if (errorSoporte) return
    let cancelado = false

    const hints = new Map<DecodeHintType, unknown>([[DecodeHintType.POSSIBLE_FORMATS, FORMATOS]])
    const reader = new BrowserMultiFormatReader(hints, { delayBetweenScanAttempts: 100 })

    reader
      .decodeFromConstraints({ audio: false, video: { facingMode: { ideal: 'environment' } } }, videoRef.current!, (result, _err, controls) => {
        if (!result || detectadoRef.current) return
        // Una sola lectura: se ignora todo lo que llegue después y se apaga la cámara
        detectadoRef.current = true
        const instante = performance.now()
        controls.stop()
        onDetectedRef.current(result.getText(), instante)
      })
      .then((controls) => {
        controlsRef.current = controls
        if (cancelado || detectadoRef.current) controls.stop()
        else setListo(true)
      })
      .catch((err: unknown) => {
        if (!cancelado) setErrorCamara(mensajeDeError(err))
      })

    const ayuda = window.setTimeout(() => setMostrarAyuda(true), AYUDA_DESPUES_DE_MS)

    return () => {
      cancelado = true
      window.clearTimeout(ayuda)
      controlsRef.current?.stop()
      controlsRef.current = null
    }
  }, [errorSoporte])

  // Si la app pasa a segundo plano se cierra el escáner y se libera la cámara
  useEffect(() => {
    const alOcultar = () => {
      if (document.visibilityState === 'hidden') onClose()
    }
    document.addEventListener('visibilitychange', alOcultar)
    return () => document.removeEventListener('visibilitychange', alOcultar)
  }, [onClose])

  return (
    <div role="dialog" aria-modal="true" aria-label="Escanear código de barras" className="fixed inset-0 z-50 flex flex-col bg-black text-white">
      <div className="flex items-center justify-between gap-3 px-4 pt-[calc(0.75rem+env(safe-area-inset-top))] pb-3">
        <p className="font-heading text-lg font-semibold">Escaneá el código de barras</p>
        <button
          type="button"
          onClick={onClose}
          aria-label="Cerrar cámara"
          className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-white/15 transition-colors duration-150 hover:bg-white/25"
        >
          <Icon name="close" className="h-6 w-6" />
        </button>
      </div>

      <div className="relative min-h-0 flex-1 overflow-hidden">
        <video ref={videoRef} className="h-full w-full object-cover" playsInline muted autoPlay aria-hidden="true" />
        {!error && (
          <div aria-hidden="true" className="pointer-events-none absolute inset-0 flex items-center justify-center">
            <div className="h-36 w-[80%] max-w-sm rounded-2xl border-4 border-white/90 shadow-[0_0_0_9999px_rgba(0,0,0,0.45)]" />
          </div>
        )}
      </div>

      <div className="px-4 pt-4 pb-[calc(1rem+env(safe-area-inset-bottom))]" aria-live="polite">
        {error ? (
          <div role="alert" className="space-y-3">
            <p className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-800">{error}</p>
            <Button variant="secondary" fullWidth onClick={onClose}>
              Tipear el código
            </Button>
          </div>
        ) : (
          <p className="text-center text-sm text-white/85">
            {!listo
              ? 'Abriendo la cámara'
              : mostrarAyuda
                ? 'No se lee: acercá el código, buscá buena luz o cerrá y tipealo.'
                : 'Encuadrá el código dentro del recuadro.'}
          </p>
        )}
      </div>
    </div>
  )
}
