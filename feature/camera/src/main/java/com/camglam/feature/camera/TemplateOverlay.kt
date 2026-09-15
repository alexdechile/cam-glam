package com.camglam.feature.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.camglam.model.LayoutZoneKind
import com.camglam.model.TemplateFamily
import com.camglam.model.TemplateSpec

/**
 * Overlay en vivo del diseño de revista sobre la cámara (FR-003).
 * Espeja el mismo layout que [com.camglam.templateengine.TemplateEngine]
 * (scrims, marco, spine, reglas y textos por zonas) a la opacidad `opacity`.
 */
@Composable
fun TemplateOverlay(
    template: TemplateSpec,
    modifier: Modifier = Modifier,
    opacity: Float = 0.35f,
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawReadabilityScrims(template, w, h, opacity)
        drawFrame(template, w, h, opacity)
        drawSpine(template, w, h, opacity, textMeasurer)
        drawMastheadRule(template, w, h, opacity)

        drawZoneText(template, LayoutZoneKind.EYEBROW, template.tagline, w, h, textMeasurer, opacity)
        drawZoneText(template, LayoutZoneKind.TAG, template.tagText, w, h, textMeasurer, opacity)
        drawZoneText(template, LayoutZoneKind.MASTHEAD, template.masthead, w, h, textMeasurer, opacity)
        drawZoneText(template, LayoutZoneKind.TITLE, template.title, w, h, textMeasurer, opacity)
        drawZoneText(template, LayoutZoneKind.SUBTITLE, template.subtitle, w, h, textMeasurer, opacity)
        drawZoneText(template, LayoutZoneKind.FOOTER, template.issueLine, w, h, textMeasurer, opacity)
    }
}

private fun DrawScope.tintedColor(argb: Int, opacity: Float): Color =
    Color(argb).copy(alpha = opacity)

private fun DrawScope.drawReadabilityScrims(
    template: TemplateSpec,
    w: Float,
    h: Float,
    opacity: Float,
) {
    val topKinds = listOf(LayoutZoneKind.EYEBROW, LayoutZoneKind.TAG, LayoutZoneKind.MASTHEAD)
    val bottomKinds = listOf(LayoutZoneKind.TITLE, LayoutZoneKind.SUBTITLE, LayoutZoneKind.FOOTER)

    val topEnd = template.layoutZones
        .filter { it.kind in topKinds }
        .maxOfOrNull { it.y + it.height }
        ?.let { it * h } ?: return
    val bottomStart = template.layoutZones
        .filter { it.kind in bottomKinds }
        .minOfOrNull { it.y }
        ?.let { it * h } ?: return

    val black = Color.Black.copy(alpha = 0.45f * opacity)
    if (topEnd > 0f) {
        drawRect(
            brush = Brush.verticalGradient(listOf(black, Color.Transparent)),
            topLeft = Offset(0f, 0f),
            size = Size(w, topEnd),
        )
    }
    if (bottomStart < h) {
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, black)),
            topLeft = Offset(0f, bottomStart),
            size = Size(w, h - bottomStart),
        )
    }
}

private fun DrawScope.drawFrame(
    template: TemplateSpec,
    w: Float,
    h: Float,
    opacity: Float,
) {
    val color = template.frameColorArgb ?: return
    val stroke = (h * template.frameThickness).coerceAtLeast(6f)
    drawRect(
        color = tintedColor(color, opacity),
        style = Stroke(width = stroke),
        size = Size(w, h),
    )

    template.frameInnerArgb?.let { innerColor ->
        val thin = (h * 0.004f).coerceAtLeast(2f)
        val inset = stroke + (h * 0.006f).coerceAtLeast(4f)
        drawRect(
            color = tintedColor(innerColor, opacity),
            style = Stroke(width = thin),
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2, h - inset * 2),
        )
    }
}

private fun DrawScope.drawSpine(
    template: TemplateSpec,
    w: Float,
    h: Float,
    opacity: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    if (!template.leftSpine) return
    val accent = template.accentArgb ?: template.frameColorArgb ?: return
    val spineW = (w * 0.030f).coerceAtLeast(20f)
    drawRect(
        color = tintedColor(accent, opacity),
        topLeft = Offset(0f, 0f),
        size = Size(spineW, h),
    )

    val letter = template.spineText.ifBlank { template.masthead.firstOrNull()?.toString() ?: "" }
    if (letter.isNotEmpty()) {
        val master = template.layoutZones.firstOrNull { it.kind == LayoutZoneKind.MASTHEAD }
        val centerY = ((master?.y ?: 0.1f) + (master?.height ?: 0.12f) * 0.5f) * h
        val masterHeight = (master?.height ?: 0.12f) * h
        drawText(
            textMeasurer = textMeasurer,
            text = letter,
            topLeft = Offset(
                spineW / 2f - spineW * 0.25f,
                centerY - masterHeight * 0.3f,
            ),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (spineW * 0.62f).coerceAtMost(h * 0.05f).sp,
                color = Color.White.copy(alpha = opacity),
            ),
        )
    }
}

private fun DrawScope.drawMastheadRule(
    template: TemplateSpec,
    w: Float,
    h: Float,
    opacity: Float,
) {
    val zone = template.layoutZones.firstOrNull { it.kind == LayoutZoneKind.MASTHEAD } ?: return
    val accent = template.accentArgb ?: template.frameColorArgb ?: return
    val ruleWidth = zone.width * w * (if (template.family == TemplateFamily.VOGUE) 0.92f else 0.5f)
    val ruleY = zone.y * h + zone.height * h * 1.08f

    val startX = if (template.family == TemplateFamily.VOGUE) {
        zone.x * w + zone.width * w / 2f - ruleWidth / 2f
    } else {
        zone.x * w
    }

    drawLine(
        color = tintedColor(accent, opacity),
        start = Offset(startX, ruleY),
        end = Offset(startX + ruleWidth, ruleY),
        strokeWidth = (h * 0.004f).coerceAtLeast(2f),
    )
}

private fun DrawScope.drawZoneText(
    template: TemplateSpec,
    kind: LayoutZoneKind,
    text: String,
    w: Float,
    h: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    opacity: Float,
) {
    if (text.isBlank()) return
    val zone = template.layoutZones.firstOrNull { it.kind == kind } ?: return
    val accent = template.accentArgb ?: template.frameColorArgb ?: 0xFFFFFFFF.toInt()

    val zoneWidth = zone.width * w
    val zoneHeight = zone.height * h
    val cx = zone.x * w + zoneWidth / 2f
    val cxRight = zone.x * w + zoneWidth
    val baseY = zone.y * h + zoneHeight * 0.80f

    when (kind) {
        LayoutZoneKind.EYEBROW -> {
            val style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (zoneHeight * 0.55f).sp,
                letterSpacing = 0.22f.sp,
                color = Color.White.copy(alpha = opacity),
            )
            drawText(textMeasurer, text, topLeft = Offset(zone.x * w, baseY - zoneHeight * 0.22f), style = style)
        }

        LayoutZoneKind.MASTHEAD -> {
            val style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = (zoneHeight * 0.72f * template.mastheadFontScale).sp,
                color = tintedColor(template.mastheadArgb, opacity),
            )
            if (template.family == TemplateFamily.VOGUE) {
                drawCentered(textMeasurer, text, style, cx, baseY)
            } else {
                drawText(textMeasurer, text, topLeft = Offset(zone.x * w, baseY - zoneHeight * 0.25f), style = style)
            }
        }

        LayoutZoneKind.TAG -> {
            if (template.accentTagRect) {
                val padV = zoneHeight * 0.12f
                val rect = Rect(
                    zone.x * w,
                    zone.y * h + padV,
                    cxRight,
                    zone.y * h + zoneHeight - padV,
                )
                drawRoundRect(
                    color = tintedColor(accent, opacity),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(zoneHeight * 0.28f),
                )
                val style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = (zoneHeight * 0.55f).sp,
                    color = Color.White.copy(alpha = opacity),
                )
                drawCentered(textMeasurer, text, style, cx, baseY - padV * 0.5f)
            } else {
                val style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = (zoneHeight * 0.60f).sp,
                    letterSpacing = 0.12f.sp,
                    color = tintedColor(accent, opacity),
                )
                val width = textMeasurer.measure(AnnotatedString(text), style).size.width
                drawText(
                    textMeasurer,
                    text,
                    topLeft = Offset(cxRight - width, baseY - zoneHeight * 0.25f),
                    style = style,
                )
            }
        }

        LayoutZoneKind.TITLE -> {
            val style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (zoneHeight * 0.66f).sp,
                color = Color.White.copy(alpha = opacity),
            )
            drawCentered(textMeasurer, text, style, cx, baseY)
        }

        LayoutZoneKind.SUBTITLE -> {
            val style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                fontSize = (zoneHeight * 0.55f).sp,
                letterSpacing = 0.14f.sp,
                color = Color.White.copy(alpha = opacity * 0.9f),
            )
            drawCentered(textMeasurer, text, style, cx, baseY)
        }

        LayoutZoneKind.FOOTER -> {
            val style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (zoneHeight * 0.62f).sp,
                letterSpacing = 0.20f.sp,
                color = Color.White.copy(alpha = opacity * 0.85f),
            )
            val width = textMeasurer.measure(AnnotatedString(text), style).size.width
            val bullet = zoneHeight * 0.35f
            val gap = zoneHeight * 0.18f
            drawRect(
                color = tintedColor(accent, opacity),
                topLeft = Offset(cx - width / 2f - gap - bullet, baseY - bullet),
                size = Size(bullet, bullet),
            )
            drawCentered(textMeasurer, text, style, cx, baseY)
        }
    }
}

private fun DrawScope.drawCentered(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    style: TextStyle,
    cx: Float,
    baselineY: Float,
) {
    val layout = textMeasurer.measure(AnnotatedString(text), style)
    drawText(
        textMeasurer,
        text,
        topLeft = Offset(cx - layout.size.width / 2f, baselineY - layout.size.height),
        style = style,
    )
}