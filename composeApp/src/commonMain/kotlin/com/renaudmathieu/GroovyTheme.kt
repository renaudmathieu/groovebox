package com.renaudmathieu

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalResourceApi::class
)
@Composable
fun GroovyTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = lightScheme

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = LalezarTypography()
    ) {
        content()
    }
}
