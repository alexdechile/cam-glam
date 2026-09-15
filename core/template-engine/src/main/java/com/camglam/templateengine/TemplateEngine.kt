package com.camglam.templateengine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.camglam.model.LayoutZoneKind
import com.camglam.model.TemplateSpec

/**
 * Componte la portada final: fondo = la foto capturada; encima se renderizan
 * el marco (TIME/NatGeo), el masthead serif y los textos (título/subtítulo)
 * usando coordenadas relativas de la plantilla.
 *
 * Los textos se dibujan con `android.graphics.*` (determinístico y testeable);
 * el mismo layout se usa en el overlay en vivo de Compose.
 */
class TemplateEngine {

    fun compose(
        template: TemplateSpec,
        source: Bitmap,
        includeParodyStamp: Boolean = false,
    ): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        canvas.drawBitmap(source, 0f, 0f, null)

        // Marco perimetral (TIME rojo, NatGeo amarillo)
        template.frameColorArgb?.let { frame ->
            val stroke = (height * 0.025f).coerceAtLeast(8f)
            val paint = Paint().apply {
                color = frame
                style = Paint.Style.STROKE
                strokeWidth = stroke
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            // Refuerza el marco con una segunda línea más fina (look editorial)
            val inner = Paint().apply {
                color = frame
                style = Paint.Style.STROKE
                strokeWidth = stroke * 0.25f
            }
            val inset = stroke * 1.8f
            canvas.drawRect(inset, inset, width - inset, height - inset, inner)
        }

        // Masthead (zona MASTHEAD) + textos de revista
        drawZoneText(
            canvas = canvas,
            template = template,
            kind = LayoutZoneKind.MASTHEAD,
            text = template.masthead.uppercase(),
            scale = template.mastheadFontScale,
        )
        drawZoneText(
            canvas = canvas,
            template = template,
            kind = LayoutZoneKind.TITLE,
            text = template.title.uppercase(),
            scale = 0.55f,
        )
        drawZoneText(
            canvas = canvas,
            template = template,
            kind = LayoutZoneKind.SUBTITLE,
            text = template.subtitle.uppercase(),
            scale = 0.45f,
        )

        if (includeParodyStamp) {
            drawParodyStamp(canvas, width, height)
        }

        return result
    }

    private fun drawZoneText(
        canvas: Canvas,
        template: TemplateSpec,
        kind: LayoutZoneKind,
        text: String,
        scale: Float,
    ) {
        if (text.isBlank()) return
        val zone = template.layoutZones.firstOrNull { it.kind == kind } ?: return

        val zoneLeft = zone.x * canvas.width
        val zoneTop = zone.y * canvas.height
        val zoneWidth = zone.width * canvas.width
        val zoneHeight = zone.height * canvas.height

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = zoneHeight * 0.72f * scale
            color = template.mastheadArgb
        }

        template.mastheadOutlineArgb?.let { outline ->
            val outlinePaint = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = textSize * 0.06f
                color = outline
                typeface = Typeface.create("serif", Typeface.BOLD)
            }
            canvas.drawText(text, zoneLeft + zoneWidth / 2f, zoneTop + zoneHeight * 0.75f, outlinePaint)
        }

        canvas.drawText(text, zoneLeft + zoneWidth / 2f, zoneTop + zoneHeight * 0.75f, paint)
    }

    private fun drawParodyStamp(canvas: Canvas, width: Int, height: Int) {
        val text = "PARODIA"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textSize = height * 0.035f
            color = 0x99FFFFFF.toInt()
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(text, width - width * 0.03f, height - height * 0.03f, paint)
    }
}