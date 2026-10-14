# Demo de Rotá

Guion para mostrar el MVP en vivo desde una sola máquina (localhost), con el backend real y los datos de prueba.

## 1. Levantar todo desde cero

Necesitás Docker, JDK 17, Maven y Node.js 20 o superior. Abrí tres terminales en la raíz del repo.

**Terminal 1: base de datos**

```bash
git checkout feature/TT-02-pantallas-sprint1
docker compose up -d
docker compose ps        # rota-db tiene que figurar "running"
```

**Terminal 2: backend** (http://localhost:8080)

```bash
cd backend
mvn spring-boot:run
```

Esperá el mensaje `Started RotaApplication`. Si la base estaba vacía, en el log aparece `Cargando datos de prueba (DataInitializer)...`.

**Terminal 3: frontend** (http://localhost:5173)

```bash
cd frontend
npm install
npm run dev
```

Abrí http://localhost:5173 en Chrome. Para mostrarlo como celular: DevTools (F12), vista de dispositivo (Ctrl+Shift+M) y elegí un ancho de 375px (iPhone SE o similar).

### Antes de empezar: datos limpios

Las fechas de los datos de prueba se calculan a partir del día en que arranca el backend con la base vacía. Para que el recorrido salga como está escrito, reiniciá los datos el mismo día de la demo (ver "Volver a cargar los datos de prueba" más abajo).

Con datos limpios, el comercio "Almacén Don Carlos" tiene:

| Producto | Código | Lote | Vence | Ubicación |
| --- | --- | --- | --- | --- |
| Leche Entera 1L | 7790001000011 | 25 unidades | mañana | Góndola |
| Leche Entera 1L | 7790001000011 | 50 unidades | en 25 días | Depósito |
| Yogur Frutilla 500g | 7790001000028 | 15 unidades | en 5 días | Depósito |
| Pan Lactal Blanco 500g | 7790002000018 | 10 unidades | en 2 días | Góndola |
| Queso Cremoso 1kg | 7790002000020 | 8 unidades | en 40 días | Góndola |

Usuarios: `repositor@comercio.com` y `dueno@comercio.com`, los dos con contraseña `123456`.

## 2. Recorrido de 3 minutos

### Paso 1. El encargado ve su ruta del día (30 s)

1. Entrar con `repositor@comercio.com` / `123456`.
2. Se abre **Ruta del día**: 1 lote a revisar, 0 vencidos.
3. Mostrar la tarjeta de **Leche Entera 1L**: semáforo **Rojo** (con texto, no solo color), en Góndola, vence mañana, 25 unidades.
4. Remarcar: el encargado no ve plata en ningún lado.

### Paso 2. Recibe un lote (40 s)

1. Pestaña **Recepción**.
2. Código de barras: `7790001000028` y "Buscar producto". Aparece **Yogur Frutilla 500g**.
3. Vence el: una fecha dentro de un mes. Cantidad: `12`. Ubicación: **Góndola**.
4. "Registrar lote". Aparece "Lote registrado" con el resumen.
5. Remarcar: son 4 datos y se carga en menos de 20 segundos.
6. Opcional: "Recibir otro lote", tipear `123` y mostrar que, si el código no existe, el encargado no puede crear productos (solo el dueño).

### Paso 3. Ve qué lote reponer y lo traslada (40 s)

1. Pestaña **Reponer**.
2. Aparece la **alerta de rotación**: el yogur del depósito vence antes que el que acabamos de poner en góndola.
3. "Reponer este producto". La tarjeta **Bajá este lote primero** muestra el lote del depósito (vence en 5 días, 15 unidades).
4. Dejar 15 unidades y tocar "Trasladar a góndola". Aparece "Listo: 15 unidades pasaron a góndola".
5. La alerta desaparece y en la lista FEFO los lotes quedan todos en Góndola, en orden de vencimiento.

### Paso 4. El dueño ve el valor en riesgo y la configuración (50 s)

1. Cerrar sesión (ícono arriba a la derecha) y entrar con `dueno@comercio.com` / `123456`.
2. **Ruta del día**: además de los contadores, el dueño ve el **valor en riesgo** total ($ 21.250,00) y por lote.
3. El menú tiene una pestaña más: **Config.**
4. En **Configuración**: editar **Lácteos**, cambiar el umbral crítico a 2 días y el aviso a Semanal, "Guardar". Volver a dejarlo en 3 días y Diario.
5. Bajar a **Usuarios** y dar de alta a un encargado nuevo (por ejemplo `ana@comercio.com`, contraseña `123456`).

### Paso 5. El encargado no ve costos (20 s)

1. Cerrar sesión y entrar otra vez como `repositor@comercio.com`.
2. **Ruta del día**: no aparece el valor en riesgo.
3. **Consulta**: buscar `leche`. Se ven los lotes con vencimiento y ubicación, sin precios ni costos.
4. Escribir `/configuracion` en la barra de direcciones: aparece "No tenés acceso".

## 3. Si algo falla en vivo

| Síntoma | Qué hacer |
| --- | --- |
| "No pudimos conectarnos con el servidor" | El backend está caído. En la terminal 2: Ctrl+C y `mvn spring-boot:run` otra vez. Esperá `Started RotaApplication` y reintentá. |
| El backend no arranca y menciona la base de datos | `docker compose ps`. Si `rota-db` no está corriendo: `docker compose up -d` y reiniciá el backend. |
| La pantalla queda en blanco o no responde | Recargá la página (F5). La sesión se mantiene. |
| "Tu sesión se cerró" | El token venció o se reinició la base. Volvé a entrar. |
| Los datos no coinciden con el guion (fechas corridas, lotes de más) | Volvé a cargar los datos de prueba (abajo). |
| El puerto 5173 u 8080 está ocupado | Cerrá la otra instancia o reiniciá la terminal correspondiente. |
| Algo no sale y no hay tiempo | Saltear ese paso y seguir: cada pantalla funciona sola. |

### Volver a cargar los datos de prueba

`DataInitializer` carga los datos solo cuando la base no tiene usuarios. Para empezar de cero:

```bash
# 1. Frenar el backend (Ctrl+C en la terminal 2)

# 2. Vaciar la base
docker compose exec rota-db psql -U rota_user -d rota_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# 3. Arrancar el backend otra vez: recrea las tablas y carga los datos
cd backend && mvn spring-boot:run
```

Plan B si lo anterior falla: `docker compose down -v` (borra el volumen de la base), `docker compose up -d` y arrancar el backend.

Después de reiniciar la base, cerrá sesión y volvé a entrar en el navegador para arrancar el recorrido desde el login.
