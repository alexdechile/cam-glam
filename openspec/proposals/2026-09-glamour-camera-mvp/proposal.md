# Propuesta: CamGlam — Cámara de Glamour con Portadas de Revista (MVP)

> Estado: **proposal (no aprobada)**
> Autor: análisis de diseño para build inicial
> Fecha: 2026-09-15

## Resumen (Summary)

Esta propuesta describe el MVP de **CamGlam**: una app Android nativa cuya cámara detecta la pose del sujeto y le aplica, en vivo y en post-proceso, el look de una portada de revista. Inspirada en el meme de las "fotos de portada de revista" (una persona que, por su pose y estilo, parece estar en la portada de VOGUE, TIME, National Geographic, GAP o H&M).

**Diferenciación frente a apps existentes** (p. ej. "Magazine Covers for Photos", "Magazine Cover Maker"):
- Cámara en tiempo real con **overlay del layout de portada**: ves la composición ANTES de disparar.
- **Guía de pose con stick figure**: un personaje de palitos muestra la pose objetivo de cada plantilla ("mano en cadera" VOGUE, "frontal TIME"...). Al igualarla, la app te marca "¡PORTADA!" — convierte el meme en una mecánica de juego.
- **Recomendación de plantilla por detección de pose** con ML Kit: si tu pose es de modelo editorial → sugiere estilo VOGUE/GAP; pose vertical con mirada potente → TIME; escena amplia/dinámica → NatGeo.
- **Juego limpio con las marcas**: plantillas originales estilo parodia con **aviso de parodia visible** en pantalla y sello opcional "PARODIA" en la exportación.
- Diseño editorial premium, exportación en alta resolución directamente a galería y share sheet.

## Contexto (Context)

### Problema
Los usuarios quieren sentirse protagonistas de un editorial de revista a partir de su foto selfie o retrato. Las apps existentes son editores "estáticos" con frames precargados de baja calidad, sin CDC de la cámara, con publicidad intrusiva y sin personalización tipográfica.

### Oportunidad
- La cámara moderna (CameraX 1.6 + Compose) permite overlays en vivo sin artefactos.
- La detección de pose (ML Kit) permite un driver divertido: "Tu pose es de portada → usa este template".
- La **guía de pose con stick figure** convierte la app en un juego de "imitar la pose", reforzando el loop viral (selfie + pose emulada + portada compartida).
- El meme es viralizable → share sheet + guardado en galería es el loop core.

### Alcance (MVP)
- Captura de foto con cámara frontal/trasera, flash, zoom (pinch), tap-to-focus.
- Biblioteca de 5 familias de plantillas estilo revista (diseños originales parodia, ver legal).
- Overlay en vivo del layout de cada plantilla antes del disparo.
- **Stick figure de pose objetivo** + feedback de "match" de pose ("¡PORTADA!").
- Post-proceso: retoque de encuadre (pan/zoom), edición de textos (masthead/título/subtítulo), 5 filtros de "glamour".
- **Aviso de parodia visible** siempre que se use estética de revista + sello opcional "PARODIA" en export.
- Exportación a MediaStore + share sheet.
- Galería local de portadas creadas (sin red, offline-first).

### Fuera de alcance (MVP)
- Login/cuentas, nube/sincronización, compra in-app (se plantea en fases posteriores).
- Reconocimiento facial/segmentación de piel avanzada.
- Plantillas generadas por IA generativa.

## Stack tecnológico propuesto (Tech Stack)

| Capa | Elección | Justificación |
|---|---|---|
| Lenguaje | **Kotlin 2.x** | 100% Kotlin, interoperabilidad con CameraX/ML Kit/Compose. |
| UI | **Jetpack Compose + Material 3** | Paradigma declarativo; `CameraXViewfinder` (camera-compose) es composable nativo sin `AndroidView`. Ediciones de Compose en compileSdk 37 ya soportadas. |
| Cámara | **CameraX 1.6.2** (core, camera2, lifecycle, compose, extensions, mlkit-vision) | Lifecycle-aware, maneja rotación/foldables/coreo sin código propio de superficie. `ImageCapture` con RELATIVE_QUALITY_HIGH + JPEG para exportar a resolución nativa. |
| ML Pose | **Google ML Kit — Pose Detection** (accent/streaming) | Detección de 33 puntos con `MlKitAnalyzer` sobre `ImageAnalysis`. Corre offline con modelo de ~5MB bundled. |
| Imagen/filtros | Composición en **Compose `Canvas`/bitmaps** + matrices de color (no NDK/OpenGL en MVP) | Filtros de glamour vía `ColorMatrix`; el compositing de plantilla (masthead + franjas + marcos) se renderiza a `Bitmap` ARGB_8888 a resolución de captura. Simple, portable y suficiente para el MVP. |
| Carga de imágenes | **Coil 3** (coil-compose) | Para miniaturas en galería y previsualización de plantillas; soporta códecs modernos (AVIF/HEIF) que CameraX puede producir. |
| DI | **Hilt** | Estándar de Android; facilita inyectar `CameraController`/repositorios en ViewModels y testear con fakes. |
| Navegación | **Navigation Compose** | Flujo: Cámara → Editor → Resultado → Galería. |
| Persistencia | **MediaStore** (fotos) + **Room** (metadatos de portadas, plantillas favoritas) | Las portadas viven en galería del sistema; Room guarda metadatos para la pestaña "Mis portadas". |
| Build | **Gradle Kotlin DSL + version catalog** (`libs.versions.toml`) | Convención Android actual. AGP 8.x, compileSdk 35/36, minSdk 26. |
| Tests | **JUnit + JUnit5 ext + Truth + Compose UI Test** | Fakes sobre mocks (guía de Google para CameraX). |

### Dependencias clave (esqueleto de `libs.versions.toml`)
```toml
[versions]
kotlin = "2.0.x"
agp = "8.7.x"
compose-bom = "2024.09.xx"
camerax = "1.6.2"            # hasta 1.7.0-alpha disponible
hilt = "2.5x"
mlkit-pose = "1.0.0"
coil = "3.0.x"

[libraries]
androidx-camera-core = { module = "androidx.camera:camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
androidx-camera-compose = { module = "androidx.camera:camera-compose", version.ref = "camerax" }
androidx-camera-extensions = { module = "androidx.camera:camera-extensions", version.ref = "camerax" }
androidx-camera-mlkit-vision = { module = "androidx.camera:camera-mlkit-vision", version.ref = "camerax" }
androidx-camera-view = { module = "androidx.camera:camera-view", version.ref = "camerax" }
mlkit-pose-detection = { module = "com.google.mlkit:pose-detection", version.ref = "mlkit-pose" }
```

## Arquitectura

Patrón **MVVM + Repository**, StateFlow, navegación por destino. Separación por módulos Gradle:

```
:app                          ← entry point, Activity, tema, navegación
:core:designsystem            ← tokens (color, tipografía, espaciado), tema claro/oscuro
:core:camera                  ← CameraController + ImageAnalysis + ImageCapture abstraídas (interfaz + impl CameraX)
:core:mlpose                  ← PoseAnalyzer (interfaz) + impl ML Kit
:core:template-engine         ← modelos de plantilla (TemplateSpec), motor de compositing, ColorMatrix filters
:feature:camera               ← UI de cámara + overlay en vivo + selección de plantilla
:feature:editor               ← UI de post-proceso (encuadre, textos, filtros)
:feature:gallery              ← "Mis portadas" (Room + MediaStore)
:core:model                   ← entidades de dominio (TemplateSpec, CoverProject, PoseResult)
```

### Flujo de datos (diagrama simplificado)

```
CameraX ImageAnalysis (YUV) ──> MlKitAnalyzer ──> PoseResult (33 keypoints)
                                     │
CameraX ImageCapture (JPEG) ────────> Bitmap nativo
                                     ▼
                          TemplateEngine.compose(template, bitmap, edits)
                                     ▼
                          Export a MediaStore + share sheet
```

### Modelo de dominio (borrador)

```kotlin
data class TemplateSpec(
    val id: String,
    val family: TemplateFamily,        // VOGUE, TIME, NATGEO, GAP, H&M (parodias)
    val masthead: String,              // texto del masthead
    val mastheadFontScale: Float,
    val frameColor: Color?             // borde (TIME rojo, NatGeo amarillo)
    val layoutZones: List<LayoutZone>, // coordenadas relativas 0..1 del layout
    val poseReference: PoseReference   // pose "objetivo" para el stick figure
)

enum class TemplateFamily { VOGUE, TIME, NATGEO, GAP, HM }

// Pose de referencia expresada como ángulos relativos entre articulaciones
data class PoseReference(
    val family: TemplateFamily,
    val stickFigure: StickFigureSpec,      // geometría del personaje de palitos a dibujar
    val targetAngles: Map<PoseLandmark, Float>, // ángulo objetivo por articulación (cadera, codo, cuello...)
    val thresholds: MatchThresholds        // pesos por grupo (torso/cabeza/brazos/piernas)
)

data class PoseResult(
    val landmarks: Map<PoseLandmark, PointF>,   // 33 puntos
    val poseScore: Double,                       // 0..1 similitud vs poseReference activa
    val suggestedFamilies: List<TemplateFamily> // heurística de matching
)
```

El stick figure se renderiza como **overlay vectorial** en Compose `Canvas` (líneas + círculos de articulaciones) escalado a las coordenadas relativas de la preview; el mismo `StickFigureSpec` se usa para el match (mismos ángulos objetivo), garantizando que "lo que ves es lo que puntúas".

### Heurística de matching pose → plantilla (MVP)
Cada familia declara su `poseReference` con ángulos objetivo:

| Señal de pose (ML Kit) | Familia sugerida | Stick figure (pose a imitar) |
|---|---|---|
| Mano en cadera / hombros relajados, cabeza inclinada (modelo editorial) | VOGUE / GAP | Brazo al costado + mano en cadera, cabeza ladeada 10–15° |
| Mirada frontal + cara ocupa ≥40% alto del frame + postura erguida | TIME | De pie erguido, brazos al costado, mirada al frente |
| Frame amplio, sujeto ≤50% del ancho, fondo con detalle | NATGEO | De pie con pies separados (apertura ancha), brazos abiertos |
| Posición central con ropa en bloque de color | H&M | De pie centrado, manos juntas adelante o brazos cruzados |
| Ninguna heurística fuerte | Por defecto VOGUE | Mano en cadera |

## Diseño de UI/UX (resumen)

> Detalle completo: ver `design-system.md` (tokens) — la impl material será un módulo `:core:designsystem`.

- **Paleta editorial "editorial-noir"**: fondo `#0D0D0D` (dark), texto `#F5F1EA` (ivory), acento **rojo VOGUE** `#E4001B` como primario, dorado `#C9A227` para highlights. Light mode: `#FFFFFF`/`#111111`.
- **Tipografía**: una familia serif display (Playfair Display) SOLO para mastheads/editorial (marca), una sans (sistema/Inter) para todo el chrome de UI. Escala ≤6 tamaños.
- **Cámara**: overlay de la plantilla seleccionada a ~35% de opacidad con guías de encuadre; bottom sheet de plantillas (caroussel de miniaturas); botón disparo integrado con animación de "flash".
- **Stick figure de pose (Pose Trainer)**: personaje de palitos (líneas glifo + nodos) dibujado con animación de transición suave entre poses de referencia. Tres estados de match: **gris** (lejos), **ámbar** (acercándose, ≥ 50 %) y **verde** ("¡PORTADA!", ≥ 75 %) con haptic + escala del indicador. Semitransparente para no tapar el sujeto; se puede ocultar con un toggle.
- **Aviso de parodia**: modal de primer uso ("Homenaje/parodia — sin afiliación con VOGUE, TIME, National Geographic, GAP, H&M"), chip persistente en la biblioteca, y línea discreta en el editor. Sello "PARODIA" opcional activable en el export.
- **Editor**: gesture de pan/zoom sobre la zona de recorte, campos de texto con chips de estilo (masthead, título, subtítulo), filtros deslizables.
- **Micro-interacciones**: confirmación de guardado con snackbar, animación de selección de plantilla (bounce), haptic en disparo.
- **Modo oscuro por defecto**, bordes suaves, sombras sutiles (reglas del skill de diseño).

## Legal / parodia (nota)

Las plantillas se diseñan como **parodias originales de estilo**: mastheads propios ("ESTYLE", "NOW", "REFLEX", "SHOP", "GLOW&GO") que evocan la estética de VOGUE/TIME/NatGeo/GAP/H&M **sin usar sus marcas ni logotipos**. Para mantenerlo explícito y defendible:

1. **Modal de primer uso** informa que la app es un homenaje/parodia sin afiliación con las marcas.
2. **Aviso visible** en la biblioteca y el editor cada vez que se aplica una estética de revista (FR-018).
3. **Sello opcional "PARODIA"** en la exportación para dejar constancia en la imagen compartida (FR-019).
4. **Sin contenido que simule marcas reales**: no se distribuyen mastheads reales, la app no permite importar/pegar logotipos de terceros (C-004), y los metadatos de la imagen guardada incluyen el descargo legal.
5. El usuario final puede editar textos libremente para uso personal; la responsabilidad de un uso comercial con marcas reales recae en el usuario.

## Migración y fases (plan)

| Fase | Alcance |
|---|---|
| **Fase 0 — Esqueleto** | Proyecto Gradle multi-módulo, tema, navegación, CI básico. |
| **Fase 1 — Cámara MVP** | CameraX + preview, captura, overlay en vivo de 3 plantillas, exportar JPEG a MediaStore. |
| **Fase 2 — Editor** | Pan/zoom, edición de textos, 5 filtros ColorMatrix, previsualización. |
| **Fase 3 — Pose** | ML Kit pose detection + stick figure de pose objetivo + match "¡PORTADA!" + recomendación de plantilla. |
| **Fase 4 — Galería y legal** | "Mis portadas" con Room, modal de parodia + aviso visible + sello PARODIA en export. |

## Alternativas consideradas (Alternatives)

1. **Fluutter/React Native**: cross-platform, pero integración de cámara + pose + filtros de alta resolución es más frágil y con más fricción de mantenimiento. Rechazado para este MVP (la diferenciación está en la cámara).
2. **Camera2 nativo (sin CameraX)**: control total pero mucho boilerplate y bugs por dispositivo. Rechazado (CameraX 1.6 ya cubre con SessionConfig/CameraPipe).
3. **Editorial SDKs de pago (p.ej. PhotoEditor SDK)**: aceleran pero agregan dependencia comercial y menos control tipográfico. Evaluar en fase avanzada si la edición escala.
4. **IA generativa para fondos/mastheads**: fuera de alcance del MVP; el compositing determinístico es más rápido y offline.