package com.camglam.feature.camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.camglam.model.LayoutZone
import com.camglam.model.LayoutZoneKind
import com.camglam.model.TemplateSpec

/**
 * Overlay en vivo del layout de revista sobre la cámara (FR-003).
 * Mismo sistema de coordenadas relativas que [com.camglam.templateengine.TemplateEngine],
 * por lo que "lo que ves en la cámara es lo que se exporta".
 * Opacidad ~35% por defecto para no tapar al sujeto.
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

        // Marco perimetral (TIME rojo, NatGeo amarillo)
        template.frameColorArgb?.let { frame ->
            val strokeWidth = (h * 0.025f).coerceAtLeast(8f)
            drawRect(
                color = Color(frame).copy(alpha = opacity),
                style = Stroke(width = strokeWidth),
                size = Size(w, h),
            )
            val inset = strokeWidth * 1.8f
            drawRect(
                color = Color(frame).copy(alpha = opacity),
                style = Stroke(width = strokeWidth * 0.25f),
                topLeft = Offset(inset, inset),
                size = Size(w - inset * 2, h - inset * 2),
            )
        }

        // Masthead
        template.layoutZones.firstOrNull { it.kind == LayoutZoneKind.MASTHEAD }?.let { zone ->
            drawZone(
                zone = zone,
                text = template.masthead.uppercase(),
                fontSize = size.height * zone.height * 0.7f * template.mastheadFontScale,
                color = Color(template.mastheadArgb).copy(alpha = opacity),
                w = w,
                h = h,
                textMeasurer = textMeasurer,
            )
        }

        // Título
        template.layoutZones.firstOrNull { it.kind == LayoutZoneKind.TITLE }?.let { zone ->
            drawZone(
                zone = zone,
                text = template.title.uppercase(),
                fontSize = size.height * zone.height * 0.5f,
                color = Color.White.copy(alpha = opacity),
                w = w,
                h = h,
                textMeasurer = textMeasurer,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawZone(
    zone: LayoutZone,
    text: String,
    fontSize: Float,
    color: Color,
    w: Float,
    h: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    if (text.isBlank()) return
    val topLeft = Offset(zone.x * w, zone.y * h)
    val style = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontSize = fontSize.sp,
        color = color,
        letterSpacing = (-1).sp,
    )
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        topLeft = topLeft,
        style = style,
    )
}