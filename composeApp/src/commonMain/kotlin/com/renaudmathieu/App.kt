package com.renaudmathieu

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.logger.Level
import org.koin.dsl.KoinConfiguration

@Serializable
internal object Home

@Serializable
internal object MusicPlayerRoute

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
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = Home) {
                composable<Home> {
                    HomeScreen(
                        onNavigateToMusicPlayer = { navController.navigate(MusicPlayerRoute) }
                    )
                }
                composable<MusicPlayerRoute> {
                    MusicPlayer(onBack = { navController.navigateUp() })
                }
            }
        }
    }
}
