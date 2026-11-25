package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BerryPrimary,
    onPrimary = BerryOnPrimary,
    primaryContainer = BerryContainer,
    onPrimaryContainer = BerryOnContainer,
    secondary = CaramelSecondary,
    onSecondary = BerryOnPrimary,
    secondaryContainer = CaramelContainer,
    onSecondaryContainer = CaramelOnContainer,
    tertiary = PistachioTertiary,
    onTertiary = BerryOnPrimary,
    tertiaryContainer = PistachioContainer,
    onTertiaryContainer = PistachioOnContainer,
    background = CreamBackground,
    onBackground = CocoaText,
    surface = ChantillySurface,
    onSurface = CocoaText,
    surfaceVariant = ChantillyVariant,
    onSurfaceVariant = CocoaMutedText,
    outline = CocoaOutline,
    outlineVariant = CocoaOutline.copy(alpha = 0.55f)
)

private val DarkColorScheme = darkColorScheme(
    primary = BerryPrimaryDark,
    onPrimary = BerryOnPrimaryDark,
    primaryContainer = BerryContainerDark,
    onPrimaryContainer = BerryOnContainerDark,
    secondary = CaramelSecondaryDark,
    onSecondary = CaramelOnContainer,
    secondaryContainer = CaramelContainerDark,
    onSecondaryContainer = CaramelOnContainerDark,
    tertiary = PistachioTertiaryDark,
    onTertiary = PistachioOnContainer,
    tertiaryContainer = PistachioContainerDark,
    onTertiaryContainer = PistachioOnContainerDark,
    background = CocoaDarkBackground,
    onBackground = CreamTextDark,
    surface = EspressoSurface,
    onSurface = CreamTextDark,
    surfaceVariant = EspressoVariant,
    onSurfaceVariant = CreamMutedDark,
    outline = EspressoOutline,
    outlineVariant = EspressoOutline.copy(alpha = 0.6f)
)

val PrecificaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PrecificaDoceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrecificaTypography,
        shapes = PrecificaShapes,
        content = content
    )
}
