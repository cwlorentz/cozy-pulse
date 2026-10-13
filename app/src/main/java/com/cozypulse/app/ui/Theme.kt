package com.cozypulse.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Cream = Color(0xFFFFF8F0)
val Oat = Color(0xFFF6EADF)
val Peach = Color(0xFFF4B7A0)
val Rose = Color(0xFFD9707B)
val Lavender = Color(0xFFB9A7D6)
val Cocoa = Color(0xFF5B4740)
val SoftCocoa = Color(0xFF8C766D)

private val colors = lightColorScheme(
    primary = Rose,
    onPrimary = Cream,
    secondary = Lavender,
    onSecondary = Cocoa,
    tertiary = Peach,
    background = Cream,
    onBackground = Cocoa,
    surface = Oat,
    onSurface = Cocoa,
    surfaceVariant = Oat,
    onSurfaceVariant = SoftCocoa,
    outline = Peach,
)

private val serif = FontFamily.Serif

private val type = Typography(
    displaySmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.Medium, fontSize = 36.sp),
    headlineMedium = TextStyle(fontFamily = serif, fontWeight = FontWeight.Medium, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = serif, fontWeight = FontWeight.Medium, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
)

@Composable
fun CozyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = type, content = content)
}
