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

/** Rol de una zona del layout de portada. */
enum class LayoutZoneKind {
    MASTHEAD,
    TITLE,
    SUBTITLE,
    IMAGE,
}

/**
 * Zona del layout de una plantilla en coordenadas relativas 0..1
 * (normalizadas al área de la portada, sin importar resolución).
 */
data class LayoutZone(
    val kind: LayoutZoneKind,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

/**
 * Especificación inmutable de una plantilla de portada.
 * El motor de compositing renderiza la plantilla sobre la foto en coordenadas
 * relativas, por lo que se ve igual en el overlay de la cámara y en la imagen final.
 */
data class TemplateSpec(
    val id: String,
    val family: TemplateFamily,
    val masthead: String,
    val mastheadArgb: Int,
    val mastheadOutlineArgb: Int? = null,
    val mastheadFontScale: Float = 1f,
    val title: String = "",
    val subtitle: String = "",
    val frameColorArgb: Int? = null,   // borde estilo TIME/NatGeo
    val layoutZones: List<LayoutZone> = emptyList(),
)

/** Catálogo de plantillas estilo revista (parodias originales, sin marcas registradas). */
object TemplateCatalog {

    val VOGUE_STYLE = TemplateSpec(
        id = "vogue-style",
        family = TemplateFamily.VOGUE,
        masthead = "ESTYLE",
        mastheadArgb = 0xFFE4001B.toInt(),
        mastheadFontScale = 1.1f,
        title = "LUZ & GLAMOUR",
        subtitle = "ESTA TEMPORADA SE POSE",
        layoutZones = listOf(
            LayoutZone(LayoutZoneKind.MASTHEAD, 0.05f, 0.02f, 0.90f, 0.12f),
            LayoutZone(LayoutZoneKind.TITLE, 0.08f, 0.80f, 0.84f, 0.08f),
            LayoutZone(LayoutZoneKind.SUBTITLE, 0.08f, 0.89f, 0.84f, 0.05f),
        ),
    )

    val TIME_STYLE = TemplateSpec(
        id = "time-style",
        family = TemplateFamily.TIME,
        masthead = "NOW",
        mastheadArgb = 0xFFFFFFFF.toInt(),
        mastheadOutlineArgb = 0xFF111111.toInt(),
        title = "LA PORTADA ERES TÚ",
        frameColorArgb = 0xFFE4001B.toInt(),
        layoutZones = listOf(
            LayoutZone(LayoutZoneKind.MASTHEAD, 0.06f, 0.03f, 0.60f, 0.10f),
            LayoutZone(LayoutZoneKind.TITLE, 0.06f, 0.82f, 0.88f, 0.08f),
        ),
    )

    val NATGEO_STYLE = TemplateSpec(
        id = "natgeo-style",
        family = TemplateFamily.NATGEO,
        masthead = "REFLEX",
        mastheadArgb = 0xFFF7C81E.toInt(),
        mastheadOutlineArgb = 0xFF111111.toInt(),
        title = "RARA BELLEZA",
        frameColorArgb = 0xFFF7C81E.toInt(),
        layoutZones = listOf(
            LayoutZone(LayoutZoneKind.MASTHEAD, 0.06f, 0.03f, 0.62f, 0.10f),
            LayoutZone(LayoutZoneKind.TITLE, 0.06f, 0.84f, 0.88f, 0.08f),
        ),
    )

    /** Las 3 plantillas del MVP. Cada fase suma más familias. */
    val all: List<TemplateSpec> = listOf(VOGUE_STYLE, TIME_STYLE, NATGEO_STYLE)

    fun byId(id: String): TemplateSpec? = all.firstOrNull { it.id == id }
}