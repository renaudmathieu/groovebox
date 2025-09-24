package com.renaudmathieu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * GradientSurface: A reusable gradient background using the Desert Dusk palette.
 *
 * Colors:
 *  - 0xFFF2D1A8
 *  - 0xFFB3633D
 *  - 0xFF4D365F
 */
@Composable
fun GradientSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val desertDuskBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF2D1A8),
            Color(0xFFB3633D),
            Color(0xFF4D365F)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(desertDuskBrush),
        content = content
    )
}
