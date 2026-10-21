# Design system de Rotá

Generado con la skill **ui-ux-pro-max**:

```bash
python3 .claude/skills/ui-ux-pro-max/scripts/search.py "inventory expiry tracking small grocery store mobile app" --design-system -p "Rotá"
```

Stack: React + Tailwind CSS v4. Los tokens viven en `src/index.css` (bloque `@theme`) y se usan como clases de Tailwind (`bg-primary`, `text-muted-foreground`, etc.). En los componentes no se escriben colores hex sueltos.

## Estilo

**Flat Design**: 2D, minimalista, colores definidos, sin sombras ni degradados, líneas limpias, tipografía protagonista.

- Superficies separadas por bordes (`border-border`), no por sombras.
- Hover y press con cambio de color u opacidad, 150 a 200 ms.
- Íconos SVG propios con trazo de 2px (`src/components/Icon.tsx`). Nada de emojis como íconos.

El patrón de página que sugiere la skill ("Product Demo + Features") es para landings y no aplica: Rotá es una app de uso diario.

## Colores

| Rol | Hex | Token |
| --- | --- | --- |
| Primary | `#059669` | `--color-primary` |
| Primary fuerte (hover, texto activo) | `#047857` | `--color-primary-strong` |
| On Primary | `#000000` | `--color-on-primary` |
| Secondary | `#10B981` | `--color-secondary` |
| Accent / CTA | `#D97706` | `--color-accent` |
| On Accent | `#000000` | `--color-on-accent` |
| Background | `#ECFDF5` | `--color-background` |
| Foreground | `#0F172A` | `--color-foreground` |
| Card | `#FFFFFF` | `--color-card` |
| Muted | `#F0F8F6` | `--color-muted` |
| Muted Foreground | `#475569` | `--color-muted-foreground` |
| Border | `#E1F2ED` | `--color-border` |
| Border fuerte (inputs) | `#B7DDD1` | `--color-border-strong` |
| Destructive | `#DC2626` | `--color-destructive` |
| On Destructive | `#FFFFFF` | `--color-on-destructive` |
| Ring (foco) | `#059669` | `--color-ring` |

Notas de la skill: verde fresco + ámbar de alimentos.

El texto sobre `primary` va en negro (`on-primary`), como indica la skill: blanco sobre `#059669` no llega a 4.5:1.

Agregados propios: `primary-strong` y `border-strong`, porque el borde base (`#E1F2ED`) es demasiado suave para delimitar un input.

### Semáforo de riesgo (Fase 2)

El estado nunca se comunica solo con color: cada badge lleva el texto ("Verde", "Amarillo", "Rojo", "Vencido") y un punto de color.

## Tipografía

- Títulos: **Rubik** (500, 600, 700).
- Texto: **Nunito Sans** (400, 600, 700).
- Se cargan desde Google Fonts con `display=swap`.
- Base de 16px y line-height 1.5. Ningún input baja de 16px (si no, iOS hace zoom).

## Movimiento

Siguiendo las skills de Emil Kowalski:

- Curva de salida: `--ease-out: cubic-bezier(0.23, 1, 0.32, 1)`.
- Feedback de press: `scale(0.97)` en 150 ms. Nada pasa de 300 ms.
- Solo se animan `transform`, `opacity` y colores. Nunca `all`, nunca width/height.
- No se animan acciones que se repiten decenas de veces por día (cambiar de pestaña).
- `prefers-reduced-motion` desactiva animaciones y transiciones.

## Mobile

- Alto de pantalla con `100dvh`, no `100vh`.
- `viewport-fit=cover` + `env(safe-area-inset-*)` en el encabezado, la barra inferior y el login.
- Sin zoom deshabilitado (`user-scalable` queda como está).
- `touch-action: manipulation` en controles para evitar el delay del tap.
- `-webkit-tap-highlight-color: transparent`.
- Hover solo con puntero que lo soporta: en Tailwind v4 la variante `hover:` ya compila a `@media (hover: hover)`.
- Teclado correcto por campo: `inputmode`, `autocapitalize="none"`, `enterkeyhint`.

## Interacción y layout

- Áreas táctiles de 44px como mínimo (botones de 48px, pestañas de 56px).
- Celular: encabezado arriba y barra de pestañas abajo, al alcance del pulgar (máximo 5 ítems).
- Escritorio (desde 1024px): barra lateral fija de 256px.
- Una sola acción principal por pantalla.
- Foco visible: anillo de 3px en `--color-ring`.
- Link "Ir al contenido" para teclado.

## Checklist antes de entregar

- [ ] Sin emojis como íconos (SVG).
- [ ] `cursor-pointer` en todo lo clickeable.
- [ ] Hover con transiciones suaves (150 a 300 ms).
- [ ] Contraste de texto de 4.5:1 como mínimo.
- [ ] Foco visible para teclado.
- [ ] `prefers-reduced-motion` respetado.
- [ ] Responsive: 375px, 768px, 1024px y 1440px.
- [ ] Estados de carga, error y lista vacía.
- [ ] Textos largos (nombres de comercio, emails) truncados sin romper el layout.
