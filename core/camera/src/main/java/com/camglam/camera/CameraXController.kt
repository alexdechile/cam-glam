package com.camglam.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación de [CameraController] con CameraX.
 *
 * - [Preview] alimenta un [androidx.camera.core.SurfaceRequest] consumido por
 *   `CameraXViewfinder` (camera-compose) en la capa de UI.
 * - [ImageCapture] captura JPEG en memoria; el bitmap se rota con el EXIF del
 *   dispositivo para quedar "vertical" siempre.
 */
class CameraXController(
    context: Context,
) : CameraController {

    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor()

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    override val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    override val isFrontFacing: Boolean
        get() = lensFacing == CameraSelector.LENS_FACING_FRONT

    override var flashEnabled: Boolean = false
        set(value) {
            field = value
            imageCapture?.flashMode = if (value) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        }

    override suspend fun bindToCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        // La espera del provider se hace fuera del main thread para no congelar
        // la UI en el primer arranque en frío (frío de cámara después del permiso).
        if (cameraProvider == null) {
            cameraProvider = ProcessCameraProvider.getInstance(context).await()
        }
        // bindToLifecycle debe ejecutarse en el main thread.
        withContext(Dispatchers.Main.immediate) {
            bindInternal()
        }
    }

    private fun bindInternal() {
        val provider = cameraProvider ?: return
        val owner = lifecycleOwner ?: return
        provider.unbindAll()

        val selector = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider { request -> _surfaceRequest.value = request }
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        provider.bindToLifecycle(owner, selector, preview, imageCapture)
        imageCapture?.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    override fun switchFacing() {
        lensFacing =
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
        bindInternal()
    }

    override suspend fun capture(): CapturedPhoto {
        val capture = imageCapture ?: throw IllegalStateException("Camera not bound")
        return suspendCancellableCoroutine { cont ->
            capture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val bitmap = image.toRotatedBitmap()
                            cont.resume(CapturedPhoto(bitmap, System.currentTimeMillis()))
                        } catch (t: Throwable) {
                            cont.resumeWithException(t)
                        } finally {
                            image.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWithException(exception)
                    }
                },
            )
        }
    }

    override fun release() {
        cameraProvider?.unbindAll()
        executor.shutdown()
    }
}

/** Convierte el [ImageProxy] (JPEG) en un [Bitmap] ya rotado para visualización vertical. */
internal fun ImageProxy.toRotatedBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalStateException("No se pudo decodificar el JPEG capturado")

    val rotation = imageInfo.rotationDegrees
    if (rotation == 0) return decoded

    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    val result = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    if (result != decoded) {
        decoded.recycle()
    }
    return result
}