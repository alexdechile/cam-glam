# Criterios de Validación — CamGlam (MVP)

Cada criterio mapea a uno o más `FR`/`NFR`. Un criterio se considera cumplido solo cuando todos sus pasos pasan.

## Validaciones funcionales (Acceptance)

### V-001 — Flujo Cámara → Portada (FR-001, FR-002, FR-004, FR-005, FR-012)
**Pasos:**
1. Abrir la app → la vista previa de cámara trasera aparece < 2 s en un dispositivo de gama media (NFR-002).
2. Cambiar a frontal y disparar → la foto se captura y guarda en MediaStore como JPEG.
3. Rotar el dispositivo a landscape y volver → la preview continúa sin reiniciar (NFR-003).
4. Aplicar flash ON y pinchar para enfocar → ambos responden en ≤ 300 ms.

### V-002 — Overlay en vivo (FR-003, FR-005)
**Pasos:**
1. Seleccionar "TIME-style" → la preview muestra el marco rojo y el masthead encima del sujeto a ~35% de opacidad.
2. Cambiar a "GAP-style" sin disparar → el overlay cambia de inmediato (< 100 ms de recompone).

### V-003 — Post-proceso (FR-006, FR-007, FR-008)
**Pasos:**
1. Capturar → entrar al editor → realizar pinch-zoom y arrastre: la zona de recorte responde sin "saltos" y sin pixelarse (imagen muestreada a 2 etapas, NFR-004).
2. Editar el masthead a "ESTYLE" y el título a texto personalizado → se actualiza la vista previa en vivo.
3. Aplicar los 5 filtros → cada uno cambia el color del preview sin crash.

### V-004 — Pose (FR-009, FR-010, FR-011)
**Pasos:**
1. Sostener una pose "mano en cadera" frente a la cámara → aparece el chip "Recomendado: EditoriaI" (VOGUE/GAP) ≤ 1 s después de estabilizar la pose.
2. Apunta a un rostro frontal cercano → la guía de tercios + zona de masthead se muestra.
3. Bloquear la cámara (modo avión) y repetir → la detección sigue funcionando (C-001, bundled model).

### V-004b — Guía de pose con stick figure (FR-016, FR-017)
**Pasos:**
1. Seleccionar "TIME-style" → aparece un stick figure erguido (brazos al costado) sobre la preview, semitransparente y sin tapar el rostro.
2. Cambiar a "VOGUE-style" → el stick figure **anima** a la pose mano-en-cadera con transición suave (< 300 ms).
3. Emular la pose con el cuerdo: el stick figure pasa de **gris → ámbar → verde** según el score; al llegar a ≥ 75 % se emite haptic y se muestra "¡PORTADA!" (medir con el score expuesto en el log de debug).
4. El toggle de ocultar/ver el stick figure funciona sin reconfigurar la cámara.

### V-005 — Exportar y compartir (FR-012, FR-013, FR-014)
**Pasos:**
1. Exportar una portada → aparece en la Galería del sistema con orientación correcta y calidad ≥ 95 (NFR-008).
2. Tocar "Compartir" → abre el share sheet; tras compartir, la "Mis portadas" agrega el ítem.
3. Reabrir la app → "Mis portadas" muestra la lista persistida (Room).

### V-005b — Parodia visible y sello (FR-018, FR-019, C-003, C-004)
**Pasos:**
1. Primer uso → se muestra el modal "Homenaje/parodia — sin afiliación con VOGUE, TIME, NatGeo, GAP, H&M" antes de la cámara.
2. En la biblioteca y en el editor → el aviso de parodia es visible en pantalla (chip/línea discreta) cada vez que hay una plantilla de estética revista activa.
3. Activar "Añadir sello PARODIA" en export → la imagen guardada incluye el isotipo discreto "PARODIA"; desactivado → no aparece.
4. Revisión de assets: ningún masthead usa marca registrada y la app no ofrece importar logotipos de terceros.

### V-005c — Formato JPG/PNG (FR-020)
**Pasos:**
1. Exportar con formato **JPG** → archivo en galería con `mimeType image/jpeg`, extensión `.jpg`.
2. Exportar la misma portada con formato **PNG** → archivo `image/png` `.png`, dimensiones idénticas a la de JPG, sin artefactos de compresión en el texto del masthead (inspección visual/pixel a 200%).
3. Los dos archivos se guardan con calidad/orientación correcta; la app (miniaturas internas) sigue usando JPG aunque el export seleccionado sea PNG.

## NFR / validaciones de calidad

### V-006 — Rendimiento (NFR-002)
- Preview 30fps sostenidos (medible con `frameMetrics` aggregated via `FrameTimelineMetrics`: moda ≤ 33 ms).
- Export 1080p < 2.5 s en Pixel 6 o equivalente (cronometrado manual).
- Cold start < 1.5 s.

### V-007 — Estabilidad (NFR-003)
- Bucle de 50 capturas seguidas sin crash ni pérdida de sesión de cámara.
- Girar 20 veces alternando orientaciones durante preview sin "surface destroyed" recap.
- Probar en: emulador API 26, dispositivo gama media (API 29+), foldable o split-screen (si está disponible en lab).

### V-008 — Memoria (NFR-004)
- Con `adb shell dumpsys meminfo` tras 50 exportaciones: PSS estable, sin OOM.
- Existe test de "bitmap de 48 MP (resolución full) → muestreo → export ≤ 4096px” sin `OutOfMemoryError`.

### V-009 — Accesibilidad (NFR-006)
- `talkback` lee el botón de disparo como una acción única (semantics merge).
- Todos los controles tienen estados activo/pressed/disabled visibles (inspección manual).
- Escala de texto al 200% no rompe el editor (Smoke test).

### V-010 — Offline (C-001, C-002)
- En avión: captura, pose, edición, export y share funcionan 100%.

### V-011 — Legal (C-003, C-004)
- Revisión de assets: ningún masthead usa marca registrada; nombres són parodias ("ESTYLE", "NOW", "REFLEX", "SHOP", "GLOW&GO").
- Los metadatos de las imágenes exportadas incluyen el descargo de parodia (XMP/IPTC "Disclaimer").

## Plan de pruebas (Test Plan)

### Unitarias
| Archivo | Casos |
|---|---|
| `TemplateEngineTest` | Compositing correcto de zonas relativas → píxeles; recálculo tras pan/zoom; texto con kerning; sello PARODIA; calidad JPEG ≥ 95. |
| `ColorMatrixFilterTest` | Cada filtro produce transformación esperada (matriz aplicada vs snapshot conocido). |
| `PoseMatcherTest` | Heurística pose→familia con fixtures de 33 landmarks (mano cadera→VOGUE; retrato frontal→TIME; encuadre ancho→NatGeo). |
| `PoseMatchScoreTest` | Score de similitud vs `poseReference` (ángulos de cadera/codo/cuello): umbrales gris/ámbar/verde (>=50/>=75), tolerancias y falsos positivos. |
| `CoverProjectStoreTest` (Room) | Persistencia/borrado/re-export de proyectos. |

> Usar **fakes**, no mocks (estándar Google para CameraX): `FakeCameraController`, `FakePoseAnalyzer`, `FakeTemplateEngine`, `FakeMatchScore`. Los canvas del stick figure se prueban con VectorDrawable/snapshot (pixel compare en `Robolectric` o instrumentado).

### De instrumentación / UI
| Suite | Destino |
|---|---|
| `CameraFlowE2E` | Preview → captura → editor → export → galería (CompromiseTestRule). |
| `OverlaySwitchTest` | Cambio de plantilla en vivo actualiza overlay. |
| `EditorGestureTest` | pan/zoom y edición de texto. |

### Matriz de dispositivos (smoke mínimo)
| Dispositivo | API | Verificación |
|---|---|---|
| Emulador Pixel | 26 | Flujo completo sin cámara física (foto sintética). |
| Pixel 6 / equivalente | 33-35 | Preview 30fps, export < 2.5 s. |
| Gama media (Redmi/Samsung A) | 29-30 | Sin crashes en rotación, memoria estable. |

## Definición de Done (DoD)
- Todos los criterios V-001…V-011 (incl. V-004b, V-005b) pasan, o tienen un "skip" documentado y aprobado.
- Cobertura unitaria del `TemplateEngine` + heurística de pose + `PoseMatchScore` ≥ 80% (NFR-007).
- Build configurado con `libs.versions.toml`, lint sin errores, `connectedCheck` verde en emulador API 26.
- Propuesta aprobada → contenido consolidado en `openspec/specs/`.