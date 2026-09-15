package com.camglam.feature.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.camglam.designsystem.theme.CamGlamTheme

/**
 * Pantalla de cámara — Fase 0 (placeholder).
 * Fase 1 integrará CameraX `CameraXViewfinder` + overlay de plantilla en vivo.
 */
@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
        ) {
            Spacer(Modifier.size(24.dp))
            Text(
                text = "CAMGLAM",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(16.dp))
            Text(
                text = "La cámara llega en Fase 1.\nMientras tanto: pose, sonríe y visualiza tu portada.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(32.dp))
            // Disparo (placeholder) — botón circular rojo estilo editorial
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .padding(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}

@Preview(name = "Camera placeholder (dark)")
@Composable
private fun CameraScreenPreview() {
    CamGlamTheme {
        CameraScreen()
    }
}