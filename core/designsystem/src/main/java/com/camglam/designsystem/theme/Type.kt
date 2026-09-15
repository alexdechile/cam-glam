package com.camglam.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Una serif display solo para mastheads/editorial; una sans para el chrome de UI.
// Escala <= 6 tamaños (regla ui-ux-design).
object CamGlamTypography {

    // Masthead / portadas (serif display, tight leading)
    object Editorial {
        val Masthead = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Black,
            fontSize = 64.sp,
            letterSpacing = -2.sp,
            lineHeight = 72.sp,
        )
        val Title = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            letterSpacing = -1.sp,
            lineHeight = 38.sp,
        )
        val Subtitle = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )
    }

    // UI sans
    private val Sans = FontFamily.SansSerif
    val Headline = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = -0.5.sp,
        lineHeight = 28.sp,
    )
    val Body = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val Label = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val Caption = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
}