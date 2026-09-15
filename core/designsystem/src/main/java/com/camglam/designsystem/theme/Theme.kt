package com.camglam.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)

private val DarkColors = darkColorScheme(
    primary = EditorialNoir.EditorialRed,
    onPrimary = White,
    secondary = EditorialNoir.Gold,
    onSecondary = Black,
    background = EditorialNoir.Background,
    onBackground = EditorialNoir.InkWarm,
    surface = EditorialNoir.Surface,
    onSurface = EditorialNoir.InkWarm,
    surfaceVariant = EditorialNoir.SurfaceHigh,
    onSurfaceVariant = EditorialNoir.InkMuted,
    outline = EditorialNoir.Outline,
    error = EditorialNoir.Error,
)

private val LightColors = lightColorScheme(
    primary = EditorialNoirLight.EditorialRed,
    onPrimary = White,
    secondary = EditorialNoirLight.Gold,
    onSecondary = White,
    background = EditorialNoirLight.Background,
    onBackground = EditorialNoirLight.InkWarm,
    surface = EditorialNoirLight.Surface,
    onSurface = EditorialNoirLight.InkWarm,
    surfaceVariant = EditorialNoirLight.SurfaceHigh,
    onSurfaceVariant = EditorialNoirLight.InkMuted,
    outline = EditorialNoirLight.Outline,
    error = EditorialNoir.Error,
)

/**
 * Tema editorial-noir. El modo oscuro es el default de la app (estética glamour;
 * regla de modo oscuro del skill de diseño). darkTheme = true por defecto.
 */
@Composable
fun CamGlamTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = camGlamMaterialTypography(),
        content = content,
    )
}

@Composable
private fun camGlamMaterialTypography(): androidx.compose.material3.Typography =
    androidx.compose.material3.Typography().let { base ->
        base.copy(
            headlineLarge = CamGlamTypography.Editorial.Masthead,
            headlineMedium = CamGlamTypography.Editorial.Title,
            titleLarge = CamGlamTypography.Headline,
            bodyLarge = CamGlamTypography.Body,
            bodyMedium = CamGlamTypography.Body,
            labelLarge = CamGlamTypography.Label,
            labelMedium = CamGlamTypography.Caption,
        )
    }