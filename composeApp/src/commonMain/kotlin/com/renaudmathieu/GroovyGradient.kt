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
 * A composable that applies a gradient background with the primary color #cb6232.
 * This should be placed between GroovyTheme and Scaffold in the App composable.
 */
@Composable
fun GroovyGradient(
    content: @Composable BoxScope.() -> Unit
) {
    val primaryColor = Color(0xFFCB6232) // The specified color #cb6232
    val gradientColors = listOf(
        primaryColor,
        primaryColor.copy(alpha = 0.8f),
        primaryColor.copy(alpha = 0.6f)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            ),
        content = content
    )
}