package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = LivingTealPrimary,
    secondary = LivingOrangeSecondary,
    tertiary = LivingSageTertiary,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color(0xFF003355), // High contrast dark text for active buttons
    onSecondary = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E6), // Light gray/silver primary text
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFFC4C6D0), // Medium-light gray
    outline = DarkSurfaceOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LivingTealPrimary,
    secondary = LivingOrangeSecondary,
    tertiary = LivingSageTertiary,
    background = LightBg,
    surface = LightSurface,
    onPrimary = Color(0xFF003355), // Consistent premium dark behavior
    onSecondary = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = LightSurfaceOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set custom brand colors by default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
