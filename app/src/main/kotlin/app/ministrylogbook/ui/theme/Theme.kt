package app.ministrylogbook.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.ministrylogbook.data.Design

class ExtendedColorScheme(warning: Color, onWarning: Color, outgoingTransfer: Color, onOutgoingTransfer: Color) {
    val warning by mutableStateOf(warning, structuralEqualityPolicy())
    val onWarning by mutableStateOf(onWarning, structuralEqualityPolicy())
    val outgoingTransfer by mutableStateOf(outgoingTransfer, structuralEqualityPolicy())
    val onOutgoingTransfer by mutableStateOf(onOutgoingTransfer, structuralEqualityPolicy())
}

val LocalExtendedColorScheme =
    compositionLocalOf {
        ExtendedColorScheme(
            warning = ThemeLightWarning,
            onWarning = ThemeLightOnWarning,
            outgoingTransfer = ThemeLightOutgoingTransfer,
            onOutgoingTransfer = ThemeLightOnOutgoingTransfer
        )
    }

val MaterialTheme.extendedColorScheme: ExtendedColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColorScheme.current

fun Context.lightColorPalette(useDynamicColors: Boolean = false): ColorScheme {
    if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return dynamicLightColorScheme(this)
    }
    return lightColorScheme(
        primary = md_theme_light_primary,
        onPrimary = md_theme_light_onPrimary,
        primaryContainer = md_theme_light_primaryContainer,
        onPrimaryContainer = md_theme_light_onPrimaryContainer,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        secondaryContainer = md_theme_light_secondaryContainer,
        onSecondaryContainer = md_theme_light_onSecondaryContainer,
        tertiary = md_theme_light_tertiary,
        onTertiary = md_theme_light_onTertiary,
        tertiaryContainer = md_theme_light_tertiaryContainer,
        onTertiaryContainer = md_theme_light_onTertiaryContainer,
        error = md_theme_light_error,
        errorContainer = md_theme_light_errorContainer,
        onError = md_theme_light_onError,
        onErrorContainer = md_theme_light_onErrorContainer,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
        surfaceVariant = md_theme_light_surfaceVariant,
        onSurfaceVariant = md_theme_light_onSurfaceVariant,
        outline = md_theme_light_outline,
        inverseOnSurface = md_theme_light_inverseOnSurface,
        inverseSurface = md_theme_light_inverseSurface,
        inversePrimary = md_theme_light_inversePrimary,
        outlineVariant = md_theme_light_onSurface.copy(0.05f)
    )
}

fun Context.darkColorPalette(useDynamicColors: Boolean = false): ColorScheme {
    if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return dynamicDarkColorScheme(this)
    }
    return darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        error = md_theme_dark_error,
        errorContainer = md_theme_dark_errorContainer,
        onError = md_theme_dark_onError,
        onErrorContainer = md_theme_dark_onErrorContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        outline = md_theme_dark_outline,
        inverseOnSurface = md_theme_dark_inverseOnSurface,
        inverseSurface = md_theme_dark_inverseSurface,
        inversePrimary = md_theme_dark_inversePrimary,
        outlineVariant = md_theme_dark_onSurface.copy(0.05f)
    )
}

@Composable
fun MinistryLogbookTheme(
    design: Design = Design.System,
    isDynamic: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = when (design) {
        Design.Light -> false
        Design.Dark -> true
        Design.System -> isSystemInDarkTheme
    }
    val colors =
        if (useDarkTheme) {
            context.darkColorPalette(isDynamic)
        } else {
            context.lightColorPalette(isDynamic)
        }

    val extendedColors = if (useDarkTheme) {
        ExtendedColorScheme(
            warning = ThemeDarkWarning,
            onWarning = ThemeDarkOnWarning,
            outgoingTransfer = ThemeDarkOutgoingTransfer,
            onOutgoingTransfer = ThemeDarkOnOutgoingTransfer
        )
    } else {
        ExtendedColorScheme(
            warning = ThemeLightWarning,
            onWarning = ThemeLightOnWarning,
            outgoingTransfer = ThemeLightOutgoingTransfer,
            onOutgoingTransfer = ThemeLightOnOutgoingTransfer
        )
    }

    CompositionLocalProvider(LocalExtendedColorScheme provides extendedColors) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}
