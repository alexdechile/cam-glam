# CamGlam — OpenSpec

Repositorio de especificaciones del proyecto **CamGlam**, una cámara de glamour para Android que convierte fotos en portadas de revista (estilo VOGUE, TIME, National Geographic, GAP, H&M).

## Estructura

```
openspec/
├── README.md                     ← este archivo
├── proposals/                    ← propuestas en evaluación (no aprobadas)
│   └── 2026-09-glamour-camera-mvp/
│       ├── proposal.md           ← diseño técnico, stack y arquitectura
│       ├── requirements.md       ← requerimientos funcionales y no funcionales
│       └── validation-criteria.md← criterios de aceptación y plan de pruebas
└── specs/                        ← (futuro) specs consolidados de cambios aprobados
```

## Flujo

1. Las propuestas nuevas se documentan en `proposals/<id>/`.
2. Toda decisión de diseño se valida contra `validation-criteria.md` antes de implementarse.
3. Al aprobarse, el contenido se consolida en `specs/` como deltas por sistema (según las reglas del proyecto en `AGENTS.md`: consultar `MEMORY.md` y documentar mejoras de diseño en `DESIGN.md`).