#!/usr/bin/env node
// Carga los datos de prueba de la demo del MVP a través de la API del backend (no toca la base directamente).
// Uso: npm run demo:datos  [-- --archivo otra-ruta.json]
// Variables opcionales: ROTA_BACKEND_URL (http://localhost:8080), ROTA_EMAIL y ROTA_PASSWORD (las del dueño de prueba).
//
// Se corre una sola vez, sobre una base recién creada por DataInitializer:
// - Productos con "existente": true son los de la semilla. Se les agregan ventas y bajas, sin tocar sus lotes.
// - Productos nuevos se dan de alta con sus lotes. Si el código ya existe, se saltean.
// - Si ya hay bajas registradas, no carga nada: los datos ya estaban (para repetir, restablecer el entorno).
//
// Las ventas y las bajas usan un lote auxiliar que vence hoy y queda en 0 unidades. Así la venta (FEFO) no
// descuenta de los lotes de la demo y el lote auxiliar no aparece en la ruta ni en las alertas.
import { readFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const BACKEND = (process.env.ROTA_BACKEND_URL || 'http://localhost:8080').replace(/\/$/, '')
const EMAIL = process.env.ROTA_EMAIL || 'dueno@comercio.com'
const PASSWORD = process.env.ROTA_PASSWORD || '123456'

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
  let datos = null
  try {
    datos = texto ? JSON.parse(texto) : null
  } catch {
    datos = texto
  }
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

async function buscarProducto(codigo) {
  try {
    return await api('GET', `/api/productos/codigo/${encodeURIComponent(codigo)}`)
  } catch (e) {
    if (e.status === 404) return null
    throw e
  }
}

/** Lote auxiliar que vence hoy, en depósito, para registrar ventas o bajas sin tocar los lotes de la demo. */
function loteAuxiliar(productoId, cantidad) {
  return api('POST', '/api/lotes', { productoId, cantidad, fechaVencimiento: fechaEnDias(0), ubicacion: 'DEPOSITO' })
}

async function main() {
  const datos = JSON.parse(await readFile(archivo, 'utf8'))
  console.log(`Backend: ${BACKEND}\nDatos:   ${archivo}\n`)

  const sesion = await api('POST', '/api/auth/login', { email: EMAIL, password: PASSWORD })
  if (sesion.rol !== 'ROLE_DUENO') throw new Error('El usuario tiene que ser dueño para crear productos.')
  token = sesion.token

  const bajasPrevias = await api('GET', '/api/operaciones/mermas')
  if (Array.isArray(bajasPrevias) && bajasPrevias.length > 0) {
    console.log('Los datos de la demo ya estaban cargados (hay bajas registradas). No se cargó nada.')
    console.log('Para empezar de cero, restablecé el entorno (DEMO.md, sección 7).')
    return
  }

  const categorias = new Map((await api('GET', '/api/categorias')).map((c) => [c.nombre.toLowerCase(), c]))
  const resumen = { creados: [], salteados: [], errores: [] }

  for (const p of datos.productos) {
    try {
      let producto = await buscarProducto(p.codigoBarra)
      if (p.existente) {
        if (!producto) throw new Error('no está en la base: el backend tiene que arrancar con la base vacía para cargar la semilla')
      } else if (producto) {
        resumen.salteados.push(`${p.nombre} (${p.codigoBarra}): ya existe, no se modifica`)
        continue
      } else {
        const categoria = categorias.get(p.categoria.toLowerCase())
        if (!categoria) throw new Error(`no existe la categoría "${p.categoria}"`)
        producto = await api('POST', '/api/productos', {
          codigoBarra: p.codigoBarra,
          nombre: p.nombre,
          precioVenta: p.precioVenta,
          costo: p.costo,
          categoriaId: categoria.id,
        })
      }

      if (p.ventasUltimos14Dias > 0) {
        await loteAuxiliar(producto.id, p.ventasUltimos14Dias)
        await api('POST', '/api/operaciones/ventas', { productoId: producto.id, cantidad: p.ventasUltimos14Dias })
      }

      for (const b of p.bajas ?? []) {
        const aux = await loteAuxiliar(producto.id, b.cantidad)
        await api('POST', '/api/operaciones/mermas', { loteId: aux.id, cantidad: b.cantidad, causa: b.causa, motivo: b.motivo })
      }

      for (const l of p.lotes ?? []) {
        await api('POST', '/api/lotes', {
          productoId: producto.id,
          cantidad: l.cantidad,
          fechaVencimiento: fechaEnDias(l.venceEnDias),
          ubicacion: l.ubicacion,
        })
      }

      resumen.creados.push(
        `${p.nombre} (${p.codigoBarra}): ${(p.lotes ?? []).length} lote(s), ${p.ventasUltimos14Dias ?? 0} venta(s), ${(p.bajas ?? []).length} baja(s)`,
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
  lista('Cargado:', resumen.creados)
  lista('Salteado:', resumen.salteados)
  lista('Con error:', resumen.errores)
  if (resumen.errores.length) {
    console.log('Hubo errores: restablecé el entorno (DEMO.md, sección 7) y volvé a correr el comando.')
    process.exitCode = 1
  }
}

main().catch((e) => {
  console.error(`No se pudieron cargar los datos: ${e.message}`)
  if (e.cause?.code === 'ECONNREFUSED') console.error(`¿Está corriendo el backend en ${BACKEND}?`)
  process.exitCode = 1
})
