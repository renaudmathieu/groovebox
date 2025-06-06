package com.renaudmathieu

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFFff8989)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFDBCB)
val onPrimaryContainerLight = Color(0xFF703716)
val secondaryLight = Color(0xFF6973da)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFFDBCB)
val onSecondaryContainerLight = Color(0xFF5C4032)
val tertiaryLight = Color(0xFF646031)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFECE4AA)
val onTertiaryContainerLight = Color(0xFF4C481C)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFFFF8F6)
val onBackgroundLight = Color(0xFF221A15)
val surfaceLight = Color(0xFFFFF8F6)
val onSurfaceLight = Color(0xFF221A15)
val surfaceVariantLight = Color(0xFFF4DED4)
val onSurfaceVariantLight = Color(0xFF52443D)
val outlineLight = Color(0xFF85736C)
val outlineVariantLight = Color(0xFFD7C2B9)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF382E2A)
val inverseOnSurfaceLight = Color(0xFFFFEDE6)
val inversePrimaryLight = Color(0xFFFFB691)
val surfaceDimLight = Color(0xFFE8D7CF)
val surfaceBrightLight = Color(0xFFFFF8F6)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFFF1EB)
val surfaceContainerLight = Color(0xFFFCEAE3)
val surfaceContainerHighLight = Color(0xFFF6E5DD)
val surfaceContainerHighestLight = Color(0xFFF0DFD8)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val lightScheme = expressiveLightColorScheme().copy(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)