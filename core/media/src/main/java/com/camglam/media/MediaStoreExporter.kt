package com.camglam.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formato de exportación. JPG con pérdida (calidad 95); PNG sin pérdida. */
enum class OutputFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
}

/**
 * Guarda portadas en la galería (MediaStore) — offline, sin permisos de
 * escritura en API 29+. Para API 26-28 escribe dentro de Pictures/CamGlam.
 */
object MediaStoreExporter {

    const val ALBUM_DIR = "CamGlam"

    fun save(
        context: Context,
        bitmap: Bitmap,
        format: OutputFormat,
        quality: Int = 95,
    ): Uri {
        val resolver = context.contentResolver
        val displayName = "CamGlam_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.${format.extension}"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM_DIR")
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("No se pudo crear la entrada en MediaStore")

        try {
            // API 26-28 requieren DATA + no soportan RELATIVE_PATH
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                setLegacyDataPath(context, uri, displayName)
            }
            resolver.openOutputStream(uri).use { stream ->
                if (stream == null) throw IOException("No se pudo abrir el stream de salida")
                val compressFormat =
                    if (format == OutputFormat.JPEG) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG
                val ok = bitmap.compress(compressFormat, quality, stream)
                if (!ok) throw IOException("Fallo al codificar ${format.name}")
            }
        } catch (t: Throwable) {
            resolver.delete(uri, null, null)
            throw t
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        return uri
    }

    private fun setLegacyDataPath(context: Context, uri: Uri, displayName: String) {
        val publicPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val dataDir = java.io.File(publicPictures, ALBUM_DIR).apply { mkdirs() }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DATA, java.io.File(dataDir, displayName).absolutePath)
        }
        context.contentResolver.update(uri, values, null, null)
    }
}