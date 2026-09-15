# DESIGN.md — Decisiones de Diseño CamGlam

Este archivo documenta las decisiones de diseño antes de aplicar cambios visuales,
según el roadmap de auto-mejora (AGENTS.md).

## Fase 1.2 — Rediseño estético de las portadas ("frames fomes")

### Problema
El usuario reporta que las portadas generadas son "muy fomes": un marco de color +
texto plano (masthead/título) sobre la foto, sin jerarquía editorial.

### Decisión
Rediseñar el motor de compositing (`TemplateEngine`) y su overlay en vivo
(`TemplateOverlay`) para una dirección editorial por familia, manteniendo "lo que
ves en la cámara es lo que se exporta" (mismas zonas y valores de estilo).

### Principios UI/UX aplicados (skill ui-ux-design)
- **Legibilidad sobre fotografías (regla 10):** en vez de bandas planas,
  degradados verticales oscuros (top:`#000~45%`, bottom:`#000~40%`) solo detrás
  de las zonas de texto. La foto queda visible.
- **Jerarquía tipográfica (regla 2 y 4):** masthead serif gigante con tracking
  negativo; eyebrow en caps pequeñas espaciadas; título en serif bold; footer en
  caps diminutas atenuadas. Máximo 5 roles de texto por plantilla.
- **Color con significado (regla 5):** el acento se usa como ancla editorial
  (regla bajo masthead, sticker, spine TIME), no como adorno.
- **Contraste en modo oscuro (regla 6):** bordes sin blanco puro; uso de
  outlines para el masthead TIME sobre fotos claras.

### Dirección por familia
- **ESTYLE (estilo VOGUE):** masthead serif rojo centrado + regla roja, eyebrow
  "BELLAS · RADIANTES", doble hairline delgado del mismo rojo, sticker "POSA!"
  arriba derecha, footer en caps pequeñas. Sin marco pesado.
- **NOW (estilo TIME):** marco rojo grueso + spine izquierdo rojo con la inicial,
  masthead blanco serif con outline negro, eyebrow, regla roja, footer con año.
- **REFLEX (estilo NatGeo):** marco amarillo medio + hairline negro interior,
  masthead blanco sobre regla amarilla, título sobre banda inferior, footer en caps.

### Modelo (`TemplateSpec`)
- Nuevos `LayoutZoneKind`: `EYEBROW`, `TAG`, `FOOTER`.
- Nuevos campos opcionales: `accentArgb`, `tagline`, `issueLine`,
  `frameThickness`, `frameDoubleStyle`.
- Los píxels/scrims se derivan de las zonas, por lo que overlay y exportación
  se mantienen en sincronía.

### Pendiente / validación
- Verificar en dispositivo que el overlay en vivo (35% alpha) refleje el diseño.
- Ajustar `textScale` si un masthead se saliera de su zona en pantallas anchas.