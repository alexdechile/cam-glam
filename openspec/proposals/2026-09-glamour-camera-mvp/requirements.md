# Requisitos — CamGlam (MVP)

ID base: `CAM`

## Requisitos Funcionales (Functional Requirements)

### Cámara
- **FR-001 — Captura desde cámara real.** La app abre una vista previa de cámara (frontal/trasera) y captura fotos a máxima resolución del dispositivo (ImageCapture → JPEG).
- **FR-002 — Control de cámara básico.** Flash (on/off/auto), zoom por pinch, tap-to-focus, conmutación frontal/trasera. Sin cierre/cierre de sesión en rotación o paso a segundo plano (lifecycle-aware).
- **FR-003 — Overlay en vivo.** El layout de la plantilla seleccionada (masthead, títulos, franjas/marcos) se muestra sobre la vista previa a opacidad ajustable (~35% default) para encuadrar antes de disparar.

### Plantillas
- **FR-004 — Biblioteca de plantillas.** Mínimo **5 familias** de plantillas estilo revista (inspiradas en VOGUE, TIME, National Geographic, GAP, H&M). Diseños originales parodia (mastheads propios), definidos por `TemplateSpec` (zonas relativas 0..1, colores, tipografías).
- **FR-005 — Intercambio de plantilla en vivo.** El cambio de plantilla mientras se ve la cámara actualiza el overlay instantáneamente.

### Editores / Post-proceso
- **FR-006 — Encuadre post-captura.** Pan y zoom (gesture) sobre la foto capturada dentro de la zona de recorte definida por la plantilla; el redimensionado es no-destructivo hasta exportar.
- **FR-007 — Edición de textos.** Editar masthead, título y subtítulo por plantilla; opciones de estilo (cas itálica, kerning, alineación) preseleccionadas por familia.
- **FR-008 — Filtros de glamour.** Mínimo 5 filtros implementados con `ColorMatrix` (p.ej. "Editorial B&W", "Golden Hour", "Studio Cool", "Soft Glow", "Noir"). Vista previa en vivo en el editor.

### Pose e inteligencia
- **FR-009 — Detección de pose.** ML Kit Pose Detector (modelo bundled, offline) detecta el sujeto en el análisis en vivo.
- **FR-010 — Recomendación de plantilla por pose.** Heurística que sugiere familias (p.ej. mano en cadera → VOGUE/GAP; retrato frontal → TIME; encuadre amplio → NatGeo). La sugerencia es un chip "Recomendado" en la grid, nunca bloqueante.
- **FR-011 — Guías inteligentes.** El overlay muestra guías de encuadre (regla de tercios + zona de cara recomendada para el masthead) cuando la pose está "en portada".
- **FR-016 — Guía de pose con stick figure.** Cada plantilla tiene una **pose de referencia** representada por un personaje de palitos (stick figure) con la pose sugerida (p.ej. VOGUE: mano en cadera; TIME: erguido mirando frontal; NatGeo: postura amplia/de pie). El stick figure se dibuja sobre la vista previa indicando "emula esta pose" para que la foto salga "en portada".
- **FR-017 — Feedback de match de pose (gamificación).** El stick figure cambia de color/opacidad según la cercanía del usuario a la pose objetivo (score de similitud de ángulos de articulaciones). Al alcanzar el umbral (p.ej. ≥ 75 %) se emite haptic + indicación visual "¡PORTADA!" y se sugiere el disparo. El feedback nunca interrumpe el disparo manual.

### Parodia y derechos
- **FR-018 — Aviso de parodia visible en pantalla.** La biblioteca de plantillas y el editor muestran un descargo visible ("Plantillas de estilos parodia. No afiliado con las marcas originales") cada vez que se selecciona una plantilla estilo revista.
- **FR-019 — Sello PARODIA en exportación (opcional).** En el export, el usuario puede marcar la opción "Añadir sello PARODIA" que imprime un isotipo discreto ("PARODIA") sobre la portada final, dejando constancia del uso no comercial/homenaje.

### Exportación y galería
- **FR-012 — Exportar a galería.** La portada final se compone a resolución nativa de captura y se guarda en MediaStore (JPEG, con orientación correcta).
- **FR-020 — Selección de formato de exportación (JPG o PNG).** En el export, el usuario elige entre **JPG** (`Bitmap.compress`, calidad 95, compresión con pérdida) y **PNG** (pérdida cero, textos de masthead nítidos, ~2-4× más peso). Las miniaturas/galería interna siempre usan JPG para rendimiento; solo el archivo exportado respeta el formato elegido.
- **FR-013 — Compartir.** Share sheet a apps externas (Instagram Stories, WhatsApp, etc.) con la portada exportada.
- **FR-014 — Mis portadas.** Lista de portadas creadas (metadatos en Room, imagen desde MediaStore); re-exportar o eliminar.
- **FR-015 — Offline-first.** Todo el MVP funciona sin conexión a red.

## Requisitos No Funcionales (Non-Functional Requirements)

- **NFR-001 — Plataforma.** Solo Android. `minSdk 26`, `compileSdk 35+`/`targetSdk` vigente. Arquitectura ARM/x86 (emulador) y ABI real.
- **NFR-002 — Rendimiento.** Vista previa a 30fps en dispositivos de gama media; exportación de portada final < 2.5 s para una foto de 1080p; cold start < 1.5 s.
- **NFR-003 — Estabilidad.** Sin crashes en rotación, cambio de aplicación, paso a segundo plano, foldable/adaptive. `ProcessCameraProvider` con unbindAll en onClear.
- **NFR-004 — Memoria.** Sin OOM al componer bitmaps de alta resolución: trabajar con muestreo (`inSampleSize`) y `BitmapFactory.Options` de 2 etapas durante el editor; bitmap final se reescala solo a la resolución de exportación necesaria (máx. 4096px de lado largo, en el MVP).
- **NFR-005 — Privacidad.** Fotos solo localmente en el dispositivo; permisos mínimos (`CAMERA`, `READ_MEDIA_IMAGES`/`WRITE...` según API). Sin telemetría de contenido.
- **NFR-006 — Accesibilidad.** Estados de foco/activos/disabled en controles, contenido descriptivo en overlays de cámara, `semantics` merge en botones compuestos, text scale supported.
- **NFR-007 — Testing.** Cobertura unitaria del `TemplateEngine` (compositing + ColorMatrix) y de la heurística de pose ≥ 80%; pruebas de UI E2E del flujo Cámara → Exportar.
- **NFR-008 — Calidad visual.** Las portadas exportadas deben verse "editoriales": tipografía serif nítida, marcos sin anti-aliasing corrupto, sin pérdida visible de calidad frente al bitmap de captura (guardar JPEG calidad ≥ 95).

## Restricciones (Constraints)
- **C-001 — Offline.** ML Kit bundled (sin Google Play Services requerido para pose) — `pose-detection` (no `pose-detection-accurate` en MVP por tamaño).
- **C-002 — Sin redes/backend.** No hay suscripción ni cloud en el MVP.
- **C-003 — Parodia explícita y visible.** No se usan marcas registradas reales en plantillas distribuidas (mastheads originales). Toda pantalla con estética de revista muestra el aviso de parodia (FR-018). El usuario queda informado en el primer uso (modal inicial: "Homenaje/parodia — sin afiliación con VOGUE, TIME, NatGeo, GAP, H&M").
- **C-004 — Sin modificación de marcas ajenas.** La app no permite pegar logotipos/mastheads descargables de terceros ni simula como propios los nombres registrados.