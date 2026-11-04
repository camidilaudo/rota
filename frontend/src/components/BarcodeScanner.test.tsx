import { StrictMode } from 'react'
import { render, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { BarcodeScanner } from './BarcodeScanner'

const zx = vi.hoisted(() => ({
  aperturas: 0,
  stop: vi.fn(),
}))

vi.mock('@zxing/browser', () => ({
  BrowserMultiFormatReader: class {
    decodeFromConstraints() {
      zx.aperturas += 1
      return Promise.resolve({ stop: zx.stop })
    }
  },
}))

beforeEach(() => {
  zx.aperturas = 0
  zx.stop.mockClear()
  Object.defineProperty(navigator, 'mediaDevices', { value: { getUserMedia: vi.fn() }, configurable: true })
})

describe('BarcodeScanner', () => {
  // En desarrollo, StrictMode monta, desmonta y vuelve a montar el componente. Si la cámara se abre
  // en el primer montaje, su limpieza corta el stream que usa el segundo y la pantalla queda negra.
  it('abre la cámara una sola vez aunque StrictMode monte el componente dos veces', async () => {
    render(
      <StrictMode>
        <BarcodeScanner onDetected={vi.fn()} onClose={vi.fn()} />
      </StrictMode>,
    )

    await waitFor(() => expect(zx.aperturas).toBe(1))
    await new Promise((resolve) => setTimeout(resolve, 20))
    expect(zx.aperturas).toBe(1)
    expect(zx.stop).not.toHaveBeenCalled()
  })
})
