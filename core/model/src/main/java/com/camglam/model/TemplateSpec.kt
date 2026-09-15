package com.camglam.model

/**
 * Familias de plantillas estilo revista. Diseños originales de estilo parodia
 * (sin marcas registradas). Los mastheads propios se asignan en el catálogo.
 */
enum class TemplateFamily {
    VOGUE,
    TIME,
    NATGEO,
    GAP,
    HM,
}

/**
 * Zona del layout de una plantilla en coordenadas relativas 0..1
 * (normalizadas al área de la portada, sin importar resolución).
 */
data class LayoutZone(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

/**
 * Especificación inmutable de una plantilla de portada.
 * El motor de compositing ([Composer]) renderiza la plantilla sobre la foto.
 */
data class TemplateSpec(
    val id: String,
    val family: TemplateFamily,
    val masthead: String,
    val mastheadFontScale: Float = 1f,
    val title: String = "",
    val subtitle: String = "",
    val frameColorArgb: Int? = null,   // borde estilo TIME/NatGeo
    val layoutZones: List<LayoutZone> = emptyList(),
)