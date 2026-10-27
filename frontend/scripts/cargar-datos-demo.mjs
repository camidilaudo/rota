#!/usr/bin/env node
// Carga los datos de demostración del MVP a través de la API del backend (no toca la base directamente).
// Uso: npm run demo:datos  [-- --archivo otra-ruta.json]
// Variables opcionales: ROTA_BACKEND_URL (http://localhost:8080), ROTA_EMAIL y ROTA_PASSWORD (las del dueño de prueba).
//
// Reglas: solo agrega. Si un código de barras ya existe, ese producto se saltea entero (no se le agregan lotes,
// ventas ni bajas). No borra ni modifica datos existentes. La línea base de merma solo se carga si está vacía.
import { readFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const BACKEND = (process.env.ROTA_BACKEND_URL || 'http://localhost:8080').replace(/\/$/, '')
const EMAIL = process.env.ROTA_EMAIL || 'dueno@comercio.com'
const PASSWORD = process.env.ROTA_PASSWORD || '123456'
const LOTE_PARA_HISTORIAL_DIAS = 60

const argArchivo = process.argv.indexOf('--archivo')
const archivo =
  argArchivo > -1 ? path.resolve(process.argv[argArchivo + 1]) : path.join(path.dirname(fileURLToPath(import.meta.url)), 'datos-demo.json')

let token = ''

async function api(metodo, ruta, cuerpo) {
  const res = await fetch(BACKEND + ruta, {
    method: metodo,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
  })
  const texto = await res.text()
  const datos = texto ? JSON.parse(texto) : null
  if (!res.ok) {
    const err = new Error(`${metodo} ${ruta} -> ${res.status}: ${datos?.message ?? texto}`)
    err.status = res.status
    throw err
  }
  return datos
}

function fechaEnDias(dias) {
  const d = new Date()
  d.setDate(d.getDate() + dias)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function main() {
  const datos = JSON.parse(await readFile(archivo, 'utf8'))
  console.log(`Backend: ${BACKEND}\nDatos:   ${archivo}\n`)

  const sesion = await api('POST', '/api/auth/login', { email: EMAIL, password: PASSWORD })
  if (sesion.rol !== 'ROLE_DUENO') throw new Error('El usuario tiene que ser dueño para crear productos.')
  token = sesion.token
  const categorias = new Map((await api('GET', '/api/categorias')).map((c) => [c.nombre.toLowerCase(), c]))

  const resumen = { creados: [], salteados: [], errores: [] }

  for (const p of datos.productos) {
    try {
      try {
        await api('GET', `/api/productos/codigo/${encodeURIComponent(p.codigoBarra)}`)
        resumen.salteados.push(`${p.nombre} (${p.codigoBarra}): ya existe, no se modifica`)
        continue
      } catch (e) {
        if (e.status !== 404) throw e
      }

      const categoria = categorias.get(p.categoria.toLowerCase())
      if (!categoria) throw new Error(`no existe la categoría "${p.categoria}"`)

      const producto = await api('POST', '/api/productos', {
        codigoBarra: p.codigoBarra,
        nombre: p.nombre,
        precioVenta: p.precioVenta,
        costo: p.costo,
        categoriaId: categoria.id,
      })

      // Historial de ventas: un lote auxiliar que se vende entero y queda en 0 (no aparece en ruta ni en FEFO)
      if (p.ventasUltimos14Dias > 0) {
        await api('POST', '/api/lotes', {
          productoId: producto.id,
          cantidad: p.ventasUltimos14Dias,
          fechaVencimiento: fechaEnDias(LOTE_PARA_HISTORIAL_DIAS),
          ubicacion: 'DEPOSITO',
        })
        await api('POST', '/api/operaciones/ventas', { productoId: producto.id, cantidad: p.ventasUltimos14Dias })
      }

      const lotes = []
      for (const l of p.lotes ?? []) {
        lotes.push(
          await api('POST', '/api/lotes', {
            productoId: producto.id,
            cantidad: l.cantidad,
            fechaVencimiento: fechaEnDias(l.venceEnDias),
            ubicacion: l.ubicacion,
          }),
        )
      }

      for (const b of p.bajas ?? []) {
        await api('POST', '/api/operaciones/mermas', { loteId: lotes[b.lote].id, cantidad: b.cantidad, causa: b.causa, motivo: b.motivo })
      }

      resumen.creados.push(
        `${p.nombre} (${p.codigoBarra}): ${lotes.length} lote(s), ${p.ventasUltimos14Dias ?? 0} venta(s), ${(p.bajas ?? []).length} baja(s)`,
      )
    } catch (e) {
      resumen.errores.push(`${p.nombre} (${p.codigoBarra}): ${e.message}`)
    }
  }

  if (datos.lineaBaseMermaMensual != null) {
    const comercio = await api('GET', `/api/comercios/${sesion.comercioId}`)
    if (comercio.lineaBaseMermaMensual == null) {
      await api('PATCH', `/api/comercios/${sesion.comercioId}/linea-base-merma`, { lineaBaseMermaMensual: datos.lineaBaseMermaMensual })
      resumen.creados.push(`Línea base de merma mensual: $ ${datos.lineaBaseMermaMensual}`)
    } else {
      resumen.salteados.push(`Línea base de merma: ya estaba cargada ($ ${comercio.lineaBaseMermaMensual}), no se modifica`)
    }
  }

  const lista = (titulo, items) => items.length && console.log(`${titulo}\n${items.map((i) => `  - ${i}`).join('\n')}\n`)
  lista('Creado:', resumen.creados)
  lista('Salteado:', resumen.salteados)
  lista('Con error:', resumen.errores)
  if (resumen.errores.length) process.exitCode = 1
}

main().catch((e) => {
  console.error(`No se pudieron cargar los datos: ${e.message}`)
  if (e.cause?.code === 'ECONNREFUSED') console.error(`¿Está corriendo el backend en ${BACKEND}?`)
  process.exitCode = 1
})
