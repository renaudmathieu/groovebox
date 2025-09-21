package com.renaudmathieu

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GrooveBox",
        state = rememberWindowState(size = DpSize(412.dp, 915.dp)),
    ) {
        App()
    }
}