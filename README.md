# CamGlam 🎬

Cámara de glamour para Android: transforma tus fotos en portadas de revista (estilo VOGUE, TIME, National Geographic, GAP, H&M) con **guía de pose** y **plantillas de estilo parodia**.

## Funcionalidades (MVP)
- Cámara en tiempo real con overlay del layout de la portada antes de disparar.
- **Pose Trainer**: stick figure con la pose objetivo; al igualarla, la app marca "¡PORTADA!".
- 5 familias de plantillas estilo revista (diseños originales parodia — sin marcas registradas, aviso de parodia visible).
- Editor: encuadre, textos (masthead/título/subtítulo) y 5 filtros de glamour.
- Export a galería + share sheet. Offline-first.

## Stack
Kotlin · Jetpack Compose (Material 3) · CameraX 1.6 · ML Kit Pose Detection · Coil 3 · Hilt · Room · Gradle version catalog

## Especificación
Propuesta completa en [openspec/proposals/2026-09-glamour-camera-mvp](openspec/proposals/2026-09-glamour-camera-mvp/).

## CI
[GitHub Actions](.github/workflows/android.yml) — compila con `gradle/actions/setup-gradle`, lint y unit tests en cada push.

> ⚠️ Aviso: este proyecto es un **homenaje/parodia**. No está afiliado con VOGUE, TIME, National Geographic, GAP ni H&M.