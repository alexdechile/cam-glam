package com.camglam.feature.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.camglam.camera.CameraXController
import com.camglam.designsystem.theme.CamGlamTheme
import com.camglam.media.MediaStoreExporter
import com.camglam.media.OutputFormat
import com.camglam.model.TemplateCatalog
import com.camglam.model.TemplateSpec
import com.camglam.templateengine.TemplateEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun requiredPermissions(): Array<String> = buildList {
    add(Manifest.permission.CAMERA)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}.toTypedArray()

private fun hasPermissions(context: android.content.Context): Boolean =
    requiredPermissions().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

/**
 * Pantalla de cámara — Fase 1.
 * Previo: solicita permisos; luego preview en vivo (CameraXViewfinder),
 * overlay del layout de plantilla (FR-003) y captura que compone la portada.
 */
@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasPermissions(context)) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        granted = result.values.all { granted -> granted }
    }

    if (!granted) {
        PermissionRequestScreen(onRequest = { launcher.launch(requiredPermissions()) })
        return
    }

    CameraScreenContent(modifier)
}

@Composable
private fun PermissionRequestScreen(onRequest: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "CAMGLAM",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(16.dp))
            Text(
                text = "Necesitamos acceder a tu cámara para crear tu portada de revista.\n(Las fotos quedan solo en tu dispositivo).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(24.dp))
            Button(onClick = onRequest) {
                Text("Permitir cámara")
            }
        }
    }
}

@Composable
private fun CameraScreenContent(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val controller = remember { CameraXController(context) }
    val surfaceRequest by controller.surfaceRequest.collectAsStateWithLifecycle()
    val engine = remember { TemplateEngine() }

    var selectedTemplate by remember { mutableStateOf(TemplateCatalog.VOGUE_STYLE) }
    var format by rememberSaveable { mutableStateOf(OutputFormat.JPEG) }
    var isSaving by remember { mutableStateOf(false) }
    var lastPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(lifecycleOwner) {
        withContext(Dispatchers.Default) {
            controller.bindToCamera(context, lifecycleOwner)
        }
    }
    DisposableEffect(Unit) {
        onDispose { controller.release() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            surfaceRequest?.let { request ->
                CameraXViewfinder(
                    surfaceRequest = request,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            TemplateOverlay(
                template = selectedTemplate,
                modifier = Modifier.fillMaxSize(),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(onClick = { controller.flashEnabled = !controller.flashEnabled }) {
                    Icon(
                        imageVector = if (controller.flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = "Flash",
                        tint = Color.White,
                    )
                }
                IconButton(onClick = { controller.switchFacing() }) {
                    Icon(
                        imageVector = Icons.Filled.Cameraswitch,
                        contentDescription = "Cambiar de cámara",
                        tint = Color.White,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    TemplateCatalog.all.forEach { template ->
                        TemplateChip(
                            template = template,
                            selected = template.id == selectedTemplate.id,
                            onClick = { selectedTemplate = template },
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormatChip(
                            label = "JPG",
                            selected = format == OutputFormat.JPEG,
                            onClick = { format = OutputFormat.JPEG },
                        )
                        FormatChip(
                            label = "PNG",
                            selected = format == OutputFormat.PNG,
                            onClick = { format = OutputFormat.PNG },
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .border(2.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSaving) Color.Gray else MaterialTheme.colorScheme.primary,
                                CircleShape,
                            )
                            .clickable(enabled = !isSaving) {
                                scope.launch {
                                    isSaving = true
                                    try {
                                        val photo = controller.capture()
                                        val cover = engine.compose(selectedTemplate, photo.bitmap)
                                        val uri = MediaStoreExporter.save(context, cover, format)
                                        lastPhotoUri = uri
                                        snackbar.showSnackbar("Portada guardada en Galería")
                                    } catch (t: Throwable) {
                                        snackbar.showSnackbar(
                                            "Error al capturar: ${t.message ?: t.javaClass.simpleName}",
                                        )
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                    ) {
                    }

                    IconButton(
                        onClick = {
                            val uri = lastPhotoUri ?: return@IconButton
                            scope.launch {
                                try {
                                    val type = context.contentResolver.getType(uri)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, type)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (t: Throwable) {
                                    snackbar.showSnackbar("No se pudo abrir la foto")
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Ver foto en Galería",
                            tint = if (lastPhotoUri != null) Color.White else Color.White.copy(alpha = 0.35f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateChip(
    template: TemplateSpec,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color(0x88000000))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = template.family.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun FormatChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Color.White else Color(0x88000000))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.Black else Color.White,
        )
    }
}

@Preview(name = "Camera screen (dark)")
@Composable
private fun CameraScreenPreview() {
    CamGlamTheme {
        CameraScreen()
    }
}