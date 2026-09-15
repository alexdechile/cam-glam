package com.camglam.camera

import android.graphics.Bitmap
import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow

/**
 * Foto capturada ya orientada y lista para compositing.
 */
data class CapturedPhoto(
    val bitmap: Bitmap,
    val capturedAtMillis: Long,
)

/**
 * Abstracción de la cámara para que la UI no dependa de CameraX.
 * La implementación con `ProcessCameraProvider` vive en [CameraXController];
 * los tests usan fakes.
 */
interface CameraController {

    /** Flujo de superficies de preview que el composable debe renderizar. */
    val surfaceRequest: StateFlow<SurfaceRequest?>

    /** `true` si la cámara apunta al usuario (selfie). */
    val isFrontFacing: Boolean

    /** Enciende/apaga el flash (no afecta a la frontal). */
    var flashEnabled: Boolean

    /** Enlaza preview + captura al ciclo de vida y arranca la cámara. */
    suspend fun bindToCamera(context: android.content.Context, lifecycleOwner: LifecycleOwner)

    /** Conmuta entre cámara frontal y trasera. */
    fun switchFacing()

    /** Captura una foto JPEG a máxima calidad y la entrega orientada. */
    suspend fun capture(): CapturedPhoto

    /** Libera la cámara y recursos asociados. */
    fun release()
}