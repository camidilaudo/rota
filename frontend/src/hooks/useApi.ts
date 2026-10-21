import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from '../api/client'

export interface ApiState<T> {
  data: T | null
  error: string | null
  loading: boolean
  reload: () => void
}

/** Carga datos al montar y expone reintento. */
export function useApi<T>(fetcher: (signal: AbortSignal) => Promise<T>, deps: readonly unknown[] = []): ApiState<T> {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [tick, setTick] = useState(0)
  const fetcherRef = useRef(fetcher)
  fetcherRef.current = fetcher

  useEffect(() => {
    const controller = new AbortController()
    setLoading(true)
    setError(null)
    fetcherRef
      .current(controller.signal)
      .then((result) => {
        if (!controller.signal.aborted) setData(result)
      })
      .catch((err: unknown) => {
        if (controller.signal.aborted) return
        setError(err instanceof ApiError ? err.message : 'Algo salió mal. Intentá de nuevo.')
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tick, ...deps])

  const reload = useCallback(() => setTick((t) => t + 1), [])

  return { data, error, loading, reload }
}

export function errorMessage(err: unknown): string {
  return err instanceof ApiError ? err.message : 'Algo salió mal. Intentá de nuevo.'
}
