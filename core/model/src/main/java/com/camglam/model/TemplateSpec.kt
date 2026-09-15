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
    EYEBROW,
    MASTHEAD,
    TAG,
    TITLE,
    SUBTITLE,
    FOOTER,
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
    val accentArgb: Int? = null,        // acento editorial (reglas, sticker, spine)
    val tagline: String = "",            // eyebrow pequeño espaciado
    val tagText: String = "",            // texto de la esquina (sticker/TAG)
    val issueLine: String = "",          // pie de portada en caps pequeñas
    val frameColorArgb: Int? = null,     // borde estilo TIME/NatGeo
    val frameThickness: Float = 0.025f,  // grosor relativo del marco
    val frameDoubleStyle: Boolean = false,
    val frameInnerArgb: Int? = null,     // hairline interior del marco doble
    val leftSpine: Boolean = false,      // tira vertical de acento en el borde izq.
    val spineText: String = "",          // inicial sobre la tira (TIME)
    val accentTagRect: Boolean = false,  // TAG como sticker de acento relleno
    val layoutZones: List<LayoutZone> = emptyList(),
)

/** Catálogo de plantillas estilo revista (parodias originales, sin marcas registradas). */
object TemplateCatalog {

    val VOGUE_STYLE = TemplateSpec(
        id = "vogue-style",
        family = TemplateFamily.VOGUE,
        masthead = "ESTYLE",
        mastheadArgb = 0xFFE4001B.toInt(),
        mastheadFontScale = 1.05f,
        title = "LUZ & GLAMOUR",
        subtitle = "LA TEMPORADA ES POSAR",
        accentArgb = 0xFFE4001B.toInt(),
        tagline = "BELLAS · RADIANTES · ÚNICAS",
        tagText = "POSA",
        issueLine = "ESTILO PARODIA · Nº 001 · 2026",
        frameColorArgb = 0xFFE4001B.toInt(),
        frameThickness = 0.010f,
        frameDoubleStyle = true,
        frameInnerArgb = 0xFFFFFFFF.toInt(),
        accentTagRect = true,
        layoutZones = listOf(
            LayoutZone(LayoutZoneKind.EYEBROW, 0.08f, 0.020f, 0.62f, 0.050f),
            LayoutZone(LayoutZoneKind.TAG, 0.80f, 0.020f, 0.17f, 0.050f),
            LayoutZone(LayoutZoneKind.MASTHEAD, 0.04f, 0.055f, 0.92f, 0.15f),
            LayoutZone(LayoutZoneKind.TITLE, 0.08f, 0.78f, 0.84f, 0.09f),
            LayoutZone(LayoutZoneKind.SUBTITLE, 0.08f, 0.885f, 0.84f, 0.05f),
            LayoutZone(LayoutZoneKind.FOOTER, 0.06f, 0.945f, 0.88f, 0.045f),
        ),
    )

    val TIME_STYLE = TemplateSpec(
        id = "time-style",
        family = TemplateFamily.TIME,
        masthead = "NOW",
        mastheadArgb = 0xFFFFFFFF.toInt(),
        mastheadOutlineArgb = 0xFF111111.toInt(),
        title = "LA PORTADA ERES TÚ",
        subtitle = "TU MOMENTO CUENTA",
        accentArgb = 0xFFE4001B.toInt(),
        tagline = "ESTABLECIDO EN 2026",
        issueLine = "Nº 042 · PARODIA · 2026",
        frameColorArgb = 0xFFE4001B.toInt(),
        frameThickness = 0.028f,
        frameDoubleStyle = true,
        frameInnerArgb = 0xFFFFFFFF.toInt(),
        leftSpine = true,
        spineText = "N",
        layoutZones = listOf(
            LayoutZone(LayoutZoneKind.EYEBROW, 0.07f, 0.030f, 0.70f, 0.050f),
            LayoutZone(LayoutZoneKind.MASTHEAD, 0.07f, 0.055f, 0.50f, 0.15f),
            LayoutZone(LayoutZoneKind.TITLE, 0.06f, 0.80f, 0.88f, 0.08f),
            LayoutZone(LayoutZoneKind.SUBTITLE, 0.07f, 0.90f, 0.86f, 0.045f),
            LayoutZone(LayoutZoneKind.FOOTER, 0.06f, 0.948f, 0.88f, 0.040f),
        ),
    )

    val NATGEO_STYLE = TemplateSpec(
        id = "natgeo-style",
        family = TemplateFamily.NATGEO,
        masthead = "REFLEX",
        mastheadArgb = 0xFFFFFFFF.toInt(),
        title = "RARA BELLEZA",
        subtitle = "ESTA TIERRA ES BELLA",
        accentArgb = 0xFFF7C81E.toInt(),
        tagline = "LA BELLEZA RARA",
        tagText = "TODO MUNDO ES BELLO",
        issueLine = "CAMGLAM · Nº 07 · PARODIA",
        frameColorArgb = 0xFFF7C81E.toInt(),
        frameThickness = 0.030f,
        frameDoubleStyle = true,
        frameInnerArgb = 0xFF111111.toInt(),
        layoutZones = listOf(
            LayoutZone(LayoutZoneKind.EYEBROW, 0.07f, 0.030f, 0.60f, 0.050f),
            LayoutZone(LayoutZoneKind.MASTHEAD, 0.07f, 0.055f, 0.55f, 0.14f),
            LayoutZone(LayoutZoneKind.TAG, 0.52f, 0.055f, 0.42f, 0.060f),
            LayoutZone(LayoutZoneKind.TITLE, 0.07f, 0.81f, 0.86f, 0.09f),
            LayoutZone(LayoutZoneKind.SUBTITLE, 0.07f, 0.905f, 0.86f, 0.040f),
            LayoutZone(LayoutZoneKind.FOOTER, 0.06f, 0.948f, 0.88f, 0.040f),
        ),
    )

    /** Las 3 plantillas del MVP. Cada fase suma más familias. */
    val all: List<TemplateSpec> = listOf(VOGUE_STYLE, TIME_STYLE, NATGEO_STYLE)

    fun byId(id: String): TemplateSpec? = all.firstOrNull { it.id == id }
}