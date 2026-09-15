package com.camglam.camera

/**
 * Resultado de la captura de foto. Fase 1 implementará `ImageCapture` de CameraX.
 */
data class CapturedPhoto(
    val jpegBytes: ByteArray,
    val width: Int,
    val height: Int,
    val orientationDegrees: Int,
)

/**
 * Abstracción de la cámara para que la UI no dependa de CameraX.
 * Fase 1: implementación real con `ProcessCameraProvider` (+ fakes en tests).
 */
interface CameraController {
    /** Enciende/apaga el flash. */
    var flashEnabled: Boolean

    /** Cambia entre cámara frontal y trasera. */
    fun switchFacing()

    /** Captura una foto y entrega los bytes JPEG. */
    suspend fun capture(): CapturedPhoto

    /** Libera recursos de la cámara. */
    fun release()
}