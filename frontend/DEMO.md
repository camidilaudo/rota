# Demo del MVP de Rotá (28/10)

Recorrido en vivo: **Recepción → Ruta del día → Venta → Descuento → Baja → Panel de merma**, con el backend real, en una sola PC (localhost) y, si se quiere mostrar el escaneo, un celular en el mismo wifi.

Duración estimada: 5 a 6 minutos.

## 1. Usuarios y roles

| Usuario | Contraseña | Rol | Qué hace en la demo |
| --- | --- | --- | --- |
| `repositor@comercio.com` | `123456` | Encargado (Juan Repositor) | Recepción con escaneo, Ruta del día, marcar revisado, Venta |
| `dueno@comercio.com` | `123456` | Dueño (Carlos Dueño) | Valor en riesgo, Descuento, Baja, Panel de merma |

El encargado nunca ve costos, márgenes ni valor en riesgo, y no tiene las pestañas Merma ni Configuración.

## 2. Levantar todo desde cero

Hace falta Docker Desktop, JDK 17 o superior y Node.js 20 o superior. Maven no hace falta: el backend trae el wrapper (`mvnw.cmd` en Windows). Abrí tres PowerShell en la raíz del repo.

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

### 3.1 Datos base (automáticos)

`DataInitializer` crea el comercio "Almacén Don Carlos", los dos usuarios, 3 categorías (Lácteos, Panadería, Fiambres y Embutidos), 4 productos y 5 lotes. Las fechas se calculan **desde el día en que arranca el backend con la base vacía**: por eso hay que restablecer el entorno el mismo día de la demo (sección 7).

### 3.2 Datos de demo (un comando)

Con el backend corriendo, en otra terminal:

```powershell
cd frontend
npm run demo:datos
```

Carga por la API, con las mismas reglas que la app:

| Producto | Código (EAN-13) | Lotes | Historial |
| --- | --- | --- | --- |
| Yogur Bebible Vainilla 1L | 7799999000018 | 12 u. en góndola (vence en 2 días), 24 u. en depósito (20 días) | 21 ventas |
| Queso Untable 300g | 7799999000025 | 6 u. en góndola (1 día), 18 u. en depósito (25 días) | 28 ventas, 1 baja por rotura |
| Medialunas x6 | 7799999000032 | 10 u. en góndola (1 día), 8 u. en depósito (3 días) | 42 ventas, 2 bajas por vencimiento |
| Jamón Cocido 200g | 7799999000049 | 15 u. en góndola (4 días) | 14 ventas, 2 bajas por daño |
| Leche Descremada 1L | 7799999000056 | 20 u. en góndola (10 días), 40 u. en depósito (30 días) | 70 ventas |

También carga la **línea base de merma mensual** ($ 150.000) si todavía no estaba cargada.

Es seguro correrlo más de una vez: si un código ya existe, saltea ese producto entero, no borra nada y no pisa la línea base si ya tiene valor. Cuando Mati y Paolo compartan sus datos, se pueden poner en `frontend/scripts/datos-demo.json` (mismo formato) o pasar otro archivo: `npm run demo:datos -- --archivo ruta\a\datos.json`.

Las ventas del historial se registran por la API en el momento de la carga (en un lote auxiliar que queda en 0 y no aparece en ninguna lista), así la velocidad de venta de los últimos 14 días es real.

### 3.3 Códigos para escanear

En `frontend/docs/demo/codigos/` están los códigos EAN-13 de la tabla, más uno que no existe (`7799999000063`) para mostrar la carga manual. Abrilos en la PC (o impresos) y escanealos con el celular.

> Los códigos de los 4 productos de `DataInitializer` (excepto el Pan, `7790002000018`) no son EAN-13 válidos: el dígito verificador no cierra, así que un EAN-13 impreso con esos números no se puede leer. Para escanear, usá los de la carpeta.

## 4. Guion paso a paso

Antes de empezar: entorno restablecido (sección 7), `npm run demo:datos` corrido y sesión cerrada en el navegador.

### Paso 1. Recepción con escaneo (encargado, 1 min)

1. Entrar con `repositor@comercio.com` / `123456`.
2. Pestaña **Recibir**, botón **Escanear con la cámara**. La primera vez el celular pide permiso: aceptar.
3. Apuntar a `yogur-bebible-7799999000018.png`.
4. **Se espera:** la cámara se cierra sola y aparece **Yogur Bebible Vainilla 1L · Lácteos · 7799999000018** con el foco en "Vence el".
5. Cargar vencimiento dentro de 15 días, cantidad `12`, ubicación **Depósito** y **Registrar lote**.
6. **Se espera:** "Lote registrado" con "12 unidades · vence el dd/mm/aaaa · Depósito".
7. Opcional: **Recibir otro lote**, escanear `codigo-no-cargado-7799999000063.png`: el encargado ve que solo el dueño puede dar de alta productos.

Sin celular: tipear `7799999000018` y **Buscar producto** (mismo resultado).

### Paso 2. Ruta del día y revisión (encargado, 1 min)

1. Pestaña **Ruta**.
2. **Se espera:** "A revisar: 9", "Vencidos: 0", lotes agrupados por ubicación con el semáforo con texto ("Rojo"), la velocidad de venta y **ningún monto en pesos**.
3. En **Queso Untable 300g** (Góndola, vence mañana) tocar **Marcar como revisado**.
4. **Se espera:** la tarjeta desaparece y "A revisar" baja a 8. Queda guardado quién y cuándo (se ve en el historial del lote, paso 4).
5. Para mostrar el control de duplicados: con el dueño ya logueado en otra pestaña o dispositivo, que intente revisar el mismo lote: aparece "Este lote ya fue revisado hoy por Juan Repositor a las HH:MM".

### Paso 3. Venta (encargado, 40 s)

1. Pestaña **Ventas**. Producto **Medialunas x6**, unidades `3`, **Registrar venta**.
2. **Se espera:** confirmación de la venta. Se descontó del lote que vence antes (FEFO): el de góndola pasa de 8 a 5 unidades (se ve en **Consulta** buscando "medialunas").
3. Cerrar sesión.

### Paso 4. Descuento (dueño, 1 min)

1. Entrar con `dueno@comercio.com` / `123456`.
2. **Se espera** en la Ruta: el **Valor en riesgo** total y por lote, y la pestaña **Merma** y **Config.** en el menú.
3. En **Yogur Bebible Vainilla 1L** (Góndola, vence en 2 días) tocar **Ver y actuar**.
4. **Se espera:** velocidad de venta (1,5 u. por día, tarda 8 días y vence en 2), el **descuento recomendado** limitado por el piso de margen del comercio (20%) y la comparación de escenarios con y sin descuento.
5. **Aplicar descuento al lote**. **Se espera:** "Descuento del 20% aplicado correctamente al lote".

### Paso 5. Baja (dueño, 40 s)

1. En el mismo lote, sección **Dar de baja unidades**: unidades `2`, causa **Daño**, detalle "Envase golpeado", **Dar de baja**.
2. **Se espera:** "Listo: se dieron de baja 2 unidades (daño)" y el historial del lote con la baja, el descuento y quién hizo cada cosa.
3. Para mostrar la revisión del paso 2 en el historial, abrir el lote del Queso Untable desde **Consulta**.

### Paso 6. Panel de merma (dueño, 1 min)

1. Pestaña **Merma**.
2. **Se espera:** pérdida a costo del mes (con los datos de demo y la baja del paso 5: **$ 14.500**), unidades y cantidad de bajas; el reparto **por categoría** y **por causa** (Daño, Vencimiento, Rotura) y **contra la línea base**: "Antes de Rotá" vs "Con Rotá" y el ahorro estimado.

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
2. Ir a **Recibir** y escanear un producto existente (por ejemplo, el yogur bebible). Cuando aparece el producto, tocar **Cambiar** y repetir. Hacer 10 escaneos, variando el producto.
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
