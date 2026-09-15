package com.camglam.templateengine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import com.camglam.model.LayoutZoneKind
import com.camglam.model.TemplateFamily
import com.camglam.model.TemplateSpec

/**
 * Componte la portada final: fondo = la foto capturada; encima se renderiza el
 * diseño editorial de la plantilla (marco, masthead, eyebrow, sticker, pie)
 * usando coordenadas relativas. El mismo layout lo dibuja el overlay de Compose.
 *
 * Orden de dibujado: scrims de legibilidad por degradado -> marco -> spine ->
 * regla del masthead -> textos por zonas.
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

        drawReadabilityScrims(canvas, template, width, height)
        drawFrame(canvas, template, width, height)
        drawSpine(canvas, template, width, height)
        drawMastheadRule(canvas, template, width, height)

        drawZoneText(canvas, template, LayoutZoneKind.EYEBROW, template.tagline, width, height)
        drawZoneText(canvas, template, LayoutZoneKind.TAG, template.tagText, width, height)
        drawZoneText(canvas, template, LayoutZoneKind.MASTHEAD, template.masthead, width, height)
        drawZoneText(canvas, template, LayoutZoneKind.TITLE, template.title, width, height)
        drawZoneText(canvas, template, LayoutZoneKind.SUBTITLE, template.subtitle, width, height)
        drawZoneText(canvas, template, LayoutZoneKind.FOOTER, template.issueLine, width, height)

        if (includeParodyStamp) {
            drawParodyStamp(canvas, width, height)
        }

        return result
    }

    // ------------------------------------------------------------------
    // Scrims de legibilidad (regla 10: degradado, nunca banda plana)
    // ------------------------------------------------------------------
    private fun drawReadabilityScrims(canvas: Canvas, template: TemplateSpec, w: Int, h: Int) {
        val topKinds = listOf(LayoutZoneKind.EYEBROW, LayoutZoneKind.TAG, LayoutZoneKind.MASTHEAD)
        val bottomKinds = listOf(LayoutZoneKind.TITLE, LayoutZoneKind.SUBTITLE, LayoutZoneKind.FOOTER)

        val topEnd = template.layoutZones
            .filter { it.kind in topKinds }
            .maxOfOrNull { it.y + it.height }?.let { it * h } ?: return
        val bottomStart = template.layoutZones
            .filter { it.kind in bottomKinds }
            .minOfOrNull { it.y }?.let { it * h } ?: return

        if (topEnd > 0f) {
            val paint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, topEnd,
                    intArrayOf(0x73000000.toInt(), 0x00000000),
                    null,
                    Shader.TileMode.CLAMP,
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), topEnd, paint)
        }
        if (bottomStart < h) {
            val paint = Paint().apply {
                shader = LinearGradient(
                    0f, bottomStart, 0f, h.toFloat(),
                    intArrayOf(0x00000000, 0x73000000.toInt()),
                    null,
                    Shader.TileMode.CLAMP,
                )
            }
            canvas.drawRect(0f, bottomStart, w.toFloat(), h.toFloat(), paint)
        }
    }

    // ------------------------------------------------------------------
    // Marco perimetral
    // ------------------------------------------------------------------
    private fun drawFrame(canvas: Canvas, template: TemplateSpec, w: Int, h: Int) {
        val color = template.frameColorArgb ?: return
        val stroke = (h * template.frameThickness).coerceAtLeast(6f)
        val outer = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = stroke
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), outer)

        template.frameInnerArgb?.let { innerColor ->
            val thin = (h * 0.004f).coerceAtLeast(2f)
            val inset = stroke + (h * 0.006f).coerceAtLeast(4f)
            val inner = Paint().apply {
                color = innerColor
                style = Paint.Style.STROKE
                strokeWidth = thin
            }
            canvas.drawRect(inset, inset, w - inset, h - inset, inner)
        }
    }

    /** Tira vertical de acento en el borde izquierdo (look TIME). */
    private fun drawSpine(canvas: Canvas, template: TemplateSpec, w: Int, h: Int) {
        if (!template.leftSpine) return
        val accent = template.accentArgb ?: template.frameColorArgb ?: return
        val spineW = (w * 0.030f).coerceAtLeast(20f)

        val fill = Paint().apply { color = accent }
        canvas.drawRect(0f, 0f, spineW, h.toFloat(), fill)

        val letter = template.spineText.ifBlank { template.masthead.firstOrNull()?.toString() ?: "" }
        if (letter.isNotEmpty()) {
            val master = template.layoutZones.firstOrNull { it.kind == LayoutZoneKind.MASTHEAD }
            val centerY = ((master?.y ?: 0.1f) + (master?.height ?: 0.12f) * 0.5f) * h
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("serif", Typeface.BOLD)
                textSize = (spineW * 0.62f).coerceAtMost(h * 0.05f)
                color = 0xFFFFFFFF.toInt()
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(letter, spineW / 2f, centerY + paint.textSize * 0.35f, paint)
        }
    }

    /** Regla horizontal de acento bajo el masthead. */
    private fun drawMastheadRule(canvas: Canvas, template: TemplateSpec, w: Int, h: Int) {
        val zone = template.layoutZones.firstOrNull { it.kind == LayoutZoneKind.MASTHEAD } ?: return
        val accent = template.accentArgb ?: template.frameColorArgb ?: return
        val ruleH = (h * 0.004f).coerceAtLeast(2f)
        val ruleWidth = zone.width * w * (if (template.family == TemplateFamily.VOGUE) 0.92f else 0.5f)
        val ruleY = zone.y * h + zone.height * h * 1.08f

        val startX = if (template.family == TemplateFamily.VOGUE) {
            (zone.x * w) + zone.width * w / 2f - ruleWidth / 2f
        } else {
            zone.x * w
        }

        val paint = Paint().apply {
            color = accent
            strokeWidth = ruleH
        }
        canvas.drawLine(startX, ruleY, startX + ruleWidth, ruleY, paint)
    }

    // ------------------------------------------------------------------
    // Textos por zona (estilo por rol)
    // ------------------------------------------------------------------
    private fun drawZoneText(
        canvas: Canvas,
        template: TemplateSpec,
        kind: LayoutZoneKind,
        text: String,
        w: Int,
        h: Int,
    ) {
        if (text.isBlank()) return
        val zone = template.layoutZones.firstOrNull { it.kind == kind } ?: return
        val accent = template.accentArgb ?: template.frameColorArgb ?: 0xFFFFFFFF.toInt()

        val zoneWidth = zone.width * w
        val zoneHeight = zone.height * h
        val cx = (zone.x * w) + zoneWidth / 2f
        val cxRight = (zone.x * w) + zoneWidth
        val baseY = (zone.y * h) + zoneHeight * 0.80f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("serif", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        when (kind) {
            LayoutZoneKind.EYEBROW -> {
                paint.textSize = zoneHeight * 0.55f
                paint.letterSpacing = 0.22f
                paint.color = 0xFFFFFFFF.toInt()
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(text, zone.x * w, baseY, paint)
            }

            LayoutZoneKind.MASTHEAD -> {
                paint.textSize = zoneHeight * 0.72f * template.mastheadFontScale
                paint.color = template.mastheadArgb

                template.mastheadOutlineArgb?.let { outline ->
                    val outlinePaint = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = textSize * 0.05f
                        color = outline
                    }
                    canvas.drawText(text, cx, baseY, outlinePaint)
                }

                if (template.family == TemplateFamily.VOGUE) {
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(text, cx, baseY, paint)
                } else {
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(text, zone.x * w, baseY, paint)
                }
            }

            LayoutZoneKind.TAG -> {
                if (template.accentTagRect) {
                    val padV = zoneHeight * 0.12f
                    val rect = android.graphics.RectF(
                        zone.x * w,
                        zone.y * h + padV,
                        cxRight,
                        zone.y * h + zoneHeight - padV,
                    )
                    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
                    canvas.drawRoundRect(rect, zoneHeight * 0.28f, zoneHeight * 0.28f, fill)

                    paint.textSize = zoneHeight * 0.55f
                    paint.color = 0xFFFFFFFF.toInt()
                    canvas.drawText(text, cx, baseY - padV * 0.5f, paint)
                } else {
                    paint.textSize = zoneHeight * 0.60f
                    paint.letterSpacing = 0.12f
                    paint.color = accent
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText(text, cxRight, baseY, paint)
                }
            }

            LayoutZoneKind.TITLE -> {
                paint.textSize = zoneHeight * 0.66f
                paint.color = 0xFFFFFFFF.toInt()
                canvas.drawText(text, cx, baseY, paint)
            }

            LayoutZoneKind.SUBTITLE -> {
                paint.textSize = zoneHeight * 0.55f
                paint.letterSpacing = 0.14f
                paint.color = 0xE6FFFFFF.toInt()
                canvas.drawText(text, cx, baseY, paint)
            }

            LayoutZoneKind.FOOTER -> {
                paint.textSize = zoneHeight * 0.62f
                paint.letterSpacing = 0.20f
                paint.color = 0xD9FFFFFF.toInt()

                val textWidth = paint.measureText(text)
                val bullet = zoneHeight * 0.35f
                val gap = zoneHeight * 0.18f
                val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
                canvas.drawRect(
                    cx - textWidth / 2f - gap - bullet,
                    baseY - bullet,
                    cx - textWidth / 2f - gap,
                    baseY,
                    bulletPaint,
                )
                canvas.drawText(text, cx, baseY, paint)
            }
        }
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