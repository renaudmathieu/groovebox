package com.renaudmathieu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import kotlin.math.max

val desertDuskBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF2D1A8),
        Color(0xFFB3633D),
        Color(0xFF4D365F)
    )
)
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(desertDuskBrush),
            content = content
        )

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        ),
                        center = Offset(
                            x = maxWidth.value / 2,
                            y = maxHeight.value / 2
                        ),
                        radius = max(
                            maxWidth.value,
                            maxHeight.value
                        ) * 1.5f,
                    ),
                    shape = RectangleShape,
                )
        )

    }
}