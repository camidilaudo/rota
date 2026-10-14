# Capturas de pantalla del MVP

Capturas del frontend de Rotá (rama `feature/TT-02-pantallas-sprint1`) para la presentación del MVP.

## Cómo se sacaron

- App corriendo en localhost: PostgreSQL, backend Spring Boot y frontend con `npm run dev`, como indica `frontend/README.md`.
- Datos reales de `DataInitializer`, cargados sobre una base vacía el 14/10/2026. Por eso las fechas que se ven son relativas a ese día. No se inventó, editó ni simuló ningún dato.
- El único dato agregado es el lote de Yogur Frutilla 500g que se guarda en la captura 05, cargado desde la pantalla de Recepción. Ese lote en góndola es el que genera la alerta de rotación de la captura 08.
- Chromium a 390x844 (celular) y a 1440x900 (escritorio), sin barras del navegador ni del sistema, con idioma es-AR. Los PNG están a densidad 2x (780x1688 y 2880x1800) para que se vean nítidos al proyectar.

## Capturas

| Archivo | Qué muestra | Rol | Historia de usuario |
| --- | --- | --- | --- |
| 01-login.png | Inicio de sesión, vacío | Sin sesión | TT-01 |
| 02-repositor-ruta-del-dia.png | Ruta del día: Leche Entera 1L en semáforo Rojo, vence mañana en góndola. Sin valor en riesgo | Repositor | HU-13 y HU-14 (ruta diaria y semáforo), HU-05 (oculta datos financieros) |
| 03-dueno-ruta-del-dia.png | Ruta del día con el valor en riesgo total y por lote ($ 21.250,00) | Dueño | HU-13 y HU-14, HU-05 |
| 04-recepcion-formulario.png | Recepción: código 7790001000028 encontrado (Yogur Frutilla 500g), con vencimiento, 12 unidades y Góndola, antes de guardar | Repositor | HU-06 (búsqueda por código), HU-07 y HU-08 (registro de lote) |
| 05-recepcion-confirmacion.png | Confirmación "Lote registrado" con el resumen del lote | Repositor | HU-07 y HU-08 |
| 06-recepcion-codigo-inexistente.png | Código 7790009999999 inexistente, con el formulario de carga manual del producto | Dueño | HU-06 (código inexistente), HU-02 y HU-03 (alta de producto) |
| 07-reponer-fefo.png | Reponer Leche Entera 1L: "Bajá este lote primero" (depósito, vence en 25 días) y la lista de lotes en orden FEFO | Repositor | HU-10 (FEFO), HU-12 (traslado a góndola) |
| 08-reponer-alerta.png | Alerta de rotación: el yogur del depósito vence antes que el de góndola | Repositor | HU-11 (alerta de rotación) |
| 09-consulta.png | Consulta rápida de "leche": stock y lotes activos con vencimiento y ubicación, sin precios | Repositor | HU-09 (consulta rápida), HU-05 |
| 10-configuracion-categorias.png | Configuración: categorías con umbral de alerta, umbral crítico y modo de aviso | Dueño | HU-01 (categoría piloto); umbral y modo de aviso por `PATCH /api/categorias/{id}/configuracion`, sin HU identificada en los tests |
| 11-repositor-sin-acceso.png | El repositor entra por URL a /configuracion y ve "No tenés acceso" | Repositor | TT-01 (rutas protegidas por rol), HU-05 |
| 12-menu-repositor.png | Menú inferior del repositor: Ruta, Recepción, Reponer y Consulta, sin Configuración (sobre la pantalla de Recepción vacía) | Repositor | TT-01 (menú según rol) |
| 13-escritorio-dueno.png | Ruta del día del dueño en escritorio, con la barra lateral y el valor en riesgo | Dueño | TT-01 (layout responsive), HU-13 y HU-14, HU-05 |

Las historias de usuario salen de los nombres de los tests de integración del backend (`backend/src/test/java/com/rota/integration`). TT-01 es la historia técnica del frontend base.
