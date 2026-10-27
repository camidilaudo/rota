# Demo del MVP de Rotá (28/10)

Recorrido en vivo en 9 pasos: **Recepción con escaneo → Alerta FEFO y traslado → Ruta del día → Descuentos → Piso de margen y escenarios → Baja → Revisión → Panel de merma → Línea base**, con el backend real, en una sola PC (localhost) y un celular en el mismo wifi para el escaneo.

Duración estimada: 8 a 10 minutos. Los datos son los datos de prueba del equipo (Almacén Don Carlos) y con ellos se miden las metas del MVP.

## 1. Usuarios y roles

| Usuario | Contraseña | Rol | Qué hace en la demo |
| --- | --- | --- | --- |
| `repositor@comercio.com` | `123456` | Encargado (Juan Repositor) | Recepción con escaneo, traslado, Ruta del día, marcar revisado |
| `dueno@comercio.com` | `123456` | Dueño (Carlos Dueño) | Valor en riesgo, descuentos, escenarios, baja, panel de merma, línea base |

El encargado nunca ve costos, márgenes ni valor en riesgo, y no tiene las pestañas Merma ni Configuración.

## 2. Levantar todo desde cero

Hace falta Docker Desktop, **JDK 21** y Node.js 20 o superior. Con JDK 25 o 26, Lombok falla al compilar (`cannot find symbol`): si tenés más de un JDK, apuntá `JAVA_HOME` al 21 antes de arrancar el backend. Maven no hace falta: el backend trae el wrapper (`mvnw.cmd` en Windows). Abrí tres PowerShell en la raíz del repo.

**Terminal 1: base de datos**

```powershell
docker compose up -d
docker compose ps        # rota-db tiene que figurar "running" o "Up"
```

**Terminal 2: backend** (http://localhost:8080)

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Esperá `Started RotaApplication`. Con la base vacía, el log muestra `Cargando datos de prueba (DataInitializer)...`.

**Terminal 3: frontend**

```powershell
cd frontend
npm install
npm run dev:https        # para usar la cámara desde el celular (ver sección 6)
# o: npm run dev         # solo PC, sin cámara en el celular
```

Abrí la dirección que muestra Vite (`https://localhost:5173`). El navegador avisa que el certificado no es de confianza: elegí **Avanzado → Continuar**. Para verlo como celular en la PC: F12 y Ctrl+Shift+M, ancho 375 px.

## 3. Datos previos

### 3.1 Semilla (automática)

Con la base vacía, `DataInitializer` crea el comercio "Almacén Don Carlos" (piso de margen 20%), los dos usuarios, 3 categorías y 4 productos con 5 lotes. Las fechas se calculan **desde el día en que arranca el backend con la base vacía**: por eso hay que restablecer el entorno el mismo día de la demo (sección 7).

| Categoría | Umbral de alerta | Umbral crítico |
| --- | --- | --- |
| Lácteos | 7 días | 3 días |
| Panadería | 3 días | 1 día |
| Fiambres y Embutidos | 10 días | 5 días |

### 3.2 Datos de prueba (un comando)

Con el backend corriendo, en otra terminal:

```powershell
cd frontend
npm run demo:datos
```

Completa la semilla por la API, con las mismas reglas que la app: las ventas de los últimos 14 días (sin esto la velocidad de venta de HU-15 da cero), las bajas del mes y la manteca. Se corre una sola vez por base: si ya hay bajas registradas, no carga nada. Si algo falla a mitad de camino, restablecé el entorno y volvé a correrlo.

| Producto | Código (EAN-13) | Costo | Precio | Ventas en 14 días | Lotes al empezar |
| --- | --- | --- | --- | --- | --- |
| Leche Entera 1L | 7790001000019 | 850 | 1.200 | 84 (6 por día) | 25 u. en góndola (vence mañana) y 50 u. en depósito (25 días) |
| Yogur Frutilla 500g | 7790001000026 | 600 | 950 | 14 (1 por día) | 15 u. en depósito (5 días) |
| Pan Lactal Blanco 500g | 7790002000018 | 1.100 | 1.800 | 42 (3 por día) | 10 u. en góndola (2 días) |
| Queso Cremoso 1kg | 7790002000025 | 3.500 | 5.200 | 2 | 8 u. en góndola (40 días) |
| Manteca 200g | 7790001000033 | 1.400 | 2.100 | 7 | 4 u. en góndola (vence hoy) |

Bajas del mes que carga el comando:

| Producto | Unidades | Causa | Pérdida a costo |
| --- | --- | --- | --- |
| Leche Entera 1L | 10 | Vencimiento | $ 8.500 |
| Yogur Frutilla 500g | 6 | Vencimiento | $ 3.600 |
| Pan Lactal Blanco 500g | 8 | Vencimiento | $ 8.800 |
| Queso Cremoso 1kg | 2 | Daño | $ 7.000 |
| Leche Entera 1L | 2 | Rotura | $ 1.700 |
| **Total** | | | **$ 29.600** |

Las ventas y las bajas se registran sobre lotes auxiliares que vencen hoy y quedan en 0: no descuentan de los lotes de la tabla y no aparecen en la ruta. La línea base de merma **no** se carga acá: se carga en vivo en el paso 9.

### 3.3 Códigos para escanear

En `frontend/docs/demo/codigos/` están los EAN-13 de los 5 productos, más uno que no existe (`7799999000063`) para mostrar qué pasa con un producto no cargado. Abrilos en la PC con brillo alto o imprimilos.

## 4. Guion paso a paso

Antes de empezar: entorno restablecido (sección 7), `npm run demo:datos` corrido, sesión cerrada en el navegador y un cronómetro a mano (las metas se miden en vivo).

### Paso 1. Recepción con escaneo (encargado; HU-06, HU-07, HU-08 y HU-27)

1. Entrar con `repositor@comercio.com` / `123456` y abrir **Recibir**.
2. Cargar el remito, cronometrando cada lote (meta: menos de 20 segundos por lote):

| Escanear | Vence | Cantidad | Ubicación |
| --- | --- | --- | --- |
| `yogur-frutilla-7790001000026.png` | en 30 días | 12 | Góndola |
| `leche-entera-7790001000019.png` | en 20 días | 30 | Depósito |
| `pan-lactal-7790002000018.png` | en 4 días | 10 | Góndola |

3. **Se espera:** al leer el código la cámara se cierra sola, aparece el producto y el foco pasa a "Vence el". Cada lote termina con "Lote registrado".
4. Opcional: escanear `codigo-no-cargado-7799999000063.png`: el encargado ve que solo el dueño puede dar de alta productos.

Sin celular: tipear el código y **Buscar producto**.

### Paso 2. Alerta FEFO y traslado (encargado; HU-10, HU-11 y HU-12)

1. **Se espera:** al cargar el yogur en góndola, el aviso de que hay un lote más viejo del mismo producto en el depósito (15 u., vence en 5 días).
2. Trasladar ese lote del depósito a la góndola.

### Paso 3. Ruta del día (encargado; HU-13, HU-14 y HU-15)

1. Pestaña **Ruta**. Cronometrar el recorrido (meta: menos de 10 minutos).
2. **Se espera:** 5 lotes en rojo, con el semáforo con texto, la velocidad de venta y **ningún monto en pesos**:
   - Leche Entera, 25 u.: vence mañana.
   - Yogur Frutilla, 15 u.: cubre 15 días de venta y le quedan 5 (riesgo por velocidad).
   - Pan Lactal, 10 u.: cubre 3,3 días y le quedan 2.
   - Queso Cremoso, 8 u.: cubre 56 días y le quedan 40 (riesgo con fecha lejana, el caso principal de HU-15).
   - Manteca, 4 u.: vence hoy.
3. Cerrar sesión.

### Paso 4. Descuentos sugeridos (dueño; HU-17 y HU-22)

1. Entrar con `dueno@comercio.com` / `123456`. **Se espera:** el **Valor en riesgo** total y por lote, y las pestañas **Merma** y **Config.**
2. En la leche, el yogur y el pan: **Ver y actuar**, revisar el descuento sugerido y **Aplicar descuento al lote**.
3. **Se espera:** cada descuento queda en el historial del lote con tipo, usuario, fecha y motivo.

### Paso 5. Piso de margen y escenarios (dueño; HU-19 y HU-18)

1. En el queso cremoso, **Ver y actuar**.
2. **Se espera:** el descuento sugerido queda topeado por el piso de margen del comercio (20%) y la comparación de escenarios muestra, con y sin descuento, unidades vendidas, descartadas y margen resultante.

### Paso 6. Baja con causa (dueño; HU-23)

1. En la manteca, sección **Dar de baja unidades**: 4 unidades, causa **Vencimiento**, **Dar de baja**.
2. **Se espera:** "Listo: se dieron de baja 4 unidades (vencimiento)", el stock en 0 y la baja en el historial.

### Paso 7. Marcar como revisado (HU-16)

1. En la ruta, **Marcar como revisado** en un lote. **Se espera:** sale de la lista pendiente del día y queda guardado quién y a qué hora.
2. Para mostrar el control de duplicados: intentar revisarlo de nuevo desde otra pestaña. **Se espera:** "Este lote ya fue revisado hoy por … a las HH:MM".

### Paso 8. Panel de merma (dueño; HU-25)

1. Pestaña **Merma**.
2. **Se espera** (bajas de los datos de prueba más la manteca del paso 6):

| | Pérdida a costo |
| --- | --- |
| **Total del mes** | **$ 35.200** |
| Lácteos | $ 19.400 |
| Panadería | $ 8.800 |
| Fiambres y Embutidos | $ 7.000 |
| Vencimiento | $ 26.500 |
| Daño | $ 7.000 |
| Rotura | $ 1.700 |

### Paso 9. Línea base (dueño; HU-26)

1. **Config.**: cargar la línea base de merma mensual en $ 60.000.
2. Volver a **Merma**. **Se espera:** la línea base prorrateada a los días del mes que pasaron (el 28/10: 28 de 30 días, $ 56.000) contra la merma real ($ 35.200): ahorro de $ 20.800, un 37% menos. Si la demo es otro día, el prorrateo cambia.

### Cierre. El encargado no ve costos (20 s)

1. Cerrar sesión y entrar como encargado.
2. **Se espera:** ni valor en riesgo, ni márgenes en el detalle del lote, ni pestaña Merma. Escribiendo `/merma` en la barra: "No tenés acceso".

## 5. Escaneo desde el celular (HU-27)

La cámara del navegador solo funciona con **HTTPS** (o en `localhost`). Por eso, para el celular:

1. PC y celular en el **mismo wifi**. En la PC: `npm run dev:https`. Vite muestra la dirección "Network", por ejemplo `https://192.168.0.15:5173`.
2. Si Windows pregunta por el firewall para Node.js, permitir en redes privadas.
3. En el celular abrir esa dirección **con https://**. Aparece un aviso de certificado: **Chrome (Android):** "Avanzado → Continuar a 192.168… (no seguro)". **Safari (iPhone):** "Mostrar detalles → visitar este sitio web".
4. Entrar, ir a **Recibir → Escanear con la cámara** y aceptar el permiso.

Si se abre por `http://`, la app lo dice: "La cámara solo funciona con conexión segura (HTTPS)". La carga tipeando el código sigue funcionando siempre.

**Plan B si el certificado da problemas: túnel HTTPS.** En la PC, con `npm run dev` corriendo:

```powershell
winget install --id Cloudflare.cloudflared      # una sola vez
cloudflared tunnel --url http://localhost:5173
```

Abrir en el celular la dirección `https://….trycloudflare.com` que muestra. Tiene certificado válido, no hay aviso. (Con ngrok: `ngrok http 5173`.) Vite ya acepta esos dominios.

## 6. Medición del escaneo (NF-02)

Criterio: identificar el producto en **menos de 2 segundos desde la lectura válida del código**. No incluye el tiempo que tarda la persona en enfocar.

Cada escaneo hecho desde Recepción se mide y queda guardado en ese dispositivo:

- **Lectura a identificación** (la métrica de NF-02): desde que se lee el código hasta que llega la respuesta del producto.
- **Apertura a identificación** (complementaria): desde que se toca "Escanear con la cámara". Incluye permisos, enfoque y encuadre.

Para hacer las pruebas en el celular:

1. Con el entorno de la sección 5, en el celular entrar a `https://<ip-de-la-pc>:5173/medicion-escaneo` y tocar **Borrar mediciones**.
2. Ir a **Recibir** y escanear un producto existente (por ejemplo, la leche). Cuando aparece el producto, tocar **Cambiar** y repetir. Hacer 10 escaneos, variando el producto.
3. Volver a `/medicion-escaneo`: muestra cada medición en ms, el **promedio** y el **máximo** de cada métrica, y si cumple o no NF-02. Solo cuentan las identificaciones exitosas.
4. **Copiar resultados (CSV)** para pegarlos en el informe.

Los códigos tipeados a mano no se miden.

## 7. Restablecer el entorno (para repetir la demo)

Esto borra **los datos de la base de desarrollo local** y los vuelve a cargar. No lo hagas sobre una base con datos reales.

```powershell
# 1. Frenar el backend (Ctrl+C en la terminal 2)
# 2. Vaciar la base
docker compose exec rota-db psql -U rota_user -d rota_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
# 3. Arrancar el backend (recrea tablas y carga DataInitializer)
cd backend; .\mvnw.cmd spring-boot:run
# 4. En otra terminal, con el backend arriba, cargar los datos de demo
cd frontend; npm run demo:datos
```

Plan B si el paso 2 falla: `docker compose down -v` (borra el volumen), `docker compose up -d` y seguir desde el paso 3.

En el celular, para empezar con las mediciones en cero: `/medicion-escaneo` → **Borrar mediciones**.

## 8. Si algo falla en vivo

| Síntoma | Qué hacer |
| --- | --- |
| "No pudimos conectarnos con el servidor" | El backend está caído: Ctrl+C en la terminal 2 y `.\mvnw.cmd spring-boot:run`. Esperar `Started RotaApplication`. |
| El backend no arranca y menciona la base | `docker compose ps`. Si `rota-db` no está corriendo: abrir Docker Desktop, `docker compose up -d`, reiniciar el backend. |
| "Marcar como revisado" da error 500 en una base vieja | Pasa solo en bases creadas antes de esta versión (ver nota técnica abajo). Restablecer el entorno o correr el `ALTER TABLE` de la nota. |
| La cámara dice que necesita HTTPS | Abrir con `https://` (sección 5) o usar el túnel. Mientras tanto, tipear el código. |
| "No hay permiso para usar la cámara" | En el celular: candado de la barra de direcciones → Permisos → Cámara → Permitir. Recargar. |
| La cámara abre pero no lee | Más luz, código más cerca y derecho, pantalla de la PC con brillo alto. Si no, tipear el código. |
| "Este lote ya fue revisado hoy por…" | Es el comportamiento esperado (HU-16): un lote se revisa una vez por día. Elegir otro lote. |
| Los números no coinciden con este guion | Las fechas y montos dependen del día de carga y de lo hecho antes: restablecer el entorno (sección 7). |
| La pantalla queda en blanco | F5. La sesión se mantiene. |
| No hay tiempo | Saltear el paso: cada pantalla funciona sola. |

**Nota técnica, bases creadas antes de esta versión.** Hibernate creó en PostgreSQL una restricción `CHECK` con los tipos de acción que existían (`DESCUENTO`, `TRASLADO`, `BAJA`) y `ddl-auto=update` no la actualiza. Para agregar `REVISION` sin borrar datos, una sola vez:

```powershell
docker compose exec rota-db psql -U rota_user -d rota_db -c "ALTER TABLE historial_acciones DROP CONSTRAINT IF EXISTS historial_acciones_tipo_accion_check;"
```

Una base nueva o restablecida no lo necesita.
