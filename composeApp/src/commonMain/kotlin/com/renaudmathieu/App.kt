package com.renaudmathieu

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.logger.Level
import org.koin.dsl.KoinConfiguration


@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    KoinExperimentalAPI::class
)
@Composable
@Preview
fun App() {
    KoinMultiplatformApplication(
        config = KoinConfiguration {
            modules(platformModule, appModule)
        },
        logLevel = Level.INFO,
    ) {
        GroovyTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            subtitleContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        title = {
                            Text(
                                text = "GrooveBox",
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        },
                        expandedHeight = 100.dp,
                        subtitle = {
                            Text(
                                text = "droidcon Berlin",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = RobotoFontFamily()
                            )
                        },
                    )
                }
            ) { innerPadding ->
                MusicPlayer(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(vertical = 24.dp)
                        .fillMaxSize()
                )
            }
        }
    }
}
