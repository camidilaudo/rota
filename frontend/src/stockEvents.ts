import { useEffect, useState } from 'react'

/**
 * Aviso de que el stock cambió (lote registrado, traslado, venta, descuento o baja).
 * La campana de alertas vive en el layout y no se vuelve a montar al quedarse en la misma pantalla:
 * con este aviso vuelve a consultar las alertas de rotación sin esperar a que se navegue.
 */
export const STOCK_CAMBIO = 'rota:stock-cambio'

export function avisarCambioDeStock() {
  window.dispatchEvent(new Event(STOCK_CAMBIO))
}

/** Devuelve un número que sube cada vez que alguien avisa un cambio de stock. */
export function useCambiosDeStock(): number {
  const [version, setVersion] = useState(0)
  useEffect(() => {
    const alCambiar = () => setVersion((v) => v + 1)
    window.addEventListener(STOCK_CAMBIO, alCambiar)
    return () => window.removeEventListener(STOCK_CAMBIO, alCambiar)
  }, [])
  return version
}
