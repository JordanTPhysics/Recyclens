package org.pathfinder.recyclens.ui.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

val LightColors = ColorPalette(
    primary = Color(0xFF4CAF50),  // Forest Green 🌲
    secondary = Color(0xFF8D6E63),  // Earthy Brown 🌿
    background = Color(0xFFF1F8E9),  // Light Greenish Tint 🌱
    foreground = Color(0xFFFFFFFF),  // White for contrast ☁️
    text = Color(0xFF2E7D32),  // Dark Green for readability 🍃
    border = Color(0xFFA1887F),  // Muted Brown for borders 🏔️
)

val DarkColors = ColorPalette(
    primary = Color(0xFF2E7D32),  // Dark Green 🌳
    secondary = Color(0xFF5D4037),  // Rich Soil Brown 🍂
    background = Color(0xFF1B5E20),  // Deep Forest Green 🌌
    foreground = Color(0xFF4E342E),  // Dark Brown 🌑
    text = Color(0xFFD7CCC8),  // Light Beige for contrast 📜
    border = Color(0xFF6D4C41),  // Soft Cocoa Brown 🌾
)


// Define a simple color palette data class
data class ColorPalette(
    val foreground: Color,
    val background: Color,
    val text: Color,
    val border: Color,
    val primary: Color,
    val secondary: Color,
    val warning: Color = Color(0xEED2023B),  // Yellow for warnings ⚠️
    val error: Color = Color(0xFFC62828),  // Dark Red for errors 💣
    val success: Color = Color(0xFF388E3C),  // Green for success 🎉
    val info: Color = Color(0xFF1976D2),  // Blue for info ℹ️

)

val LocalAppColors: ProvidableCompositionLocal<ColorPalette> = compositionLocalOf { LightColors }


@Composable
fun ComposeAppTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors

    Box(modifier = Modifier.background(colors.background)) {
        content()
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalAppColors provides colors) {
        content()
    }
}
