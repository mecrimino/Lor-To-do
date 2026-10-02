package org.lortodo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.lortodo.domain.model.AccentColor
import org.lortodo.domain.model.AppTheme

private val LightColorScheme = lightColorScheme(
    primary = WarmYellowPrimary,
    onPrimary = WarmYellowOnPrimary,
    primaryContainer = WarmYellowPrimaryContainer,
    onPrimaryContainer = WarmYellowOnPrimaryContainer,
    secondary = WarmYellowSecondary,
    onSecondary = WarmYellowOnSecondary,
    secondaryContainer = WarmYellowSecondaryContainer,
    onSecondaryContainer = WarmYellowOnSecondaryContainer,
    background = WarmYellowBackgroundLight,
    surface = WarmYellowSurfaceLight,
    surfaceVariant = WarmYellowSurfaceVariant,
    onBackground = WarmYellowOnSurfaceLight,
    onSurface = WarmYellowOnSurfaceLight,
    onSurfaceVariant = WarmYellowOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmYellowDarkPrimary,
    onPrimary = WarmYellowDarkOnPrimary,
    primaryContainer = WarmYellowPrimaryContainer,
    onPrimaryContainer = WarmYellowOnPrimaryContainer,
    secondary = WarmYellowSecondary,
    onSecondary = WarmYellowOnSecondary,
    background = WarmYellowDarkBackground,
    surface = WarmYellowDarkSurface,
    surfaceVariant = WarmYellowDarkSurfaceVariant,
    onBackground = WarmYellowDarkOnSurface,
    onSurface = WarmYellowDarkOnSurface,
    onSurfaceVariant = WarmYellowOnSurfaceVariant
)

private val AmoledColorScheme = darkColorScheme(
    primary = WarmYellowDarkPrimary,
    onPrimary = WarmYellowDarkOnPrimary,
    primaryContainer = WarmYellowPrimaryContainer,
    onPrimaryContainer = WarmYellowOnPrimaryContainer,
    secondary = WarmYellowSecondary,
    onSecondary = WarmYellowOnSecondary,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFC7C2B8)
)

@Composable
fun LorTodoTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    accentColor: AccentColor = AccentColor.YELLOW,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.AMOLED -> true
        AppTheme.SYSTEM -> isSystemDark
    }

    val context = LocalContext.current
    val colorScheme = when {
        accentColor == AccentColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        appTheme == AppTheme.AMOLED -> AmoledColorScheme
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
