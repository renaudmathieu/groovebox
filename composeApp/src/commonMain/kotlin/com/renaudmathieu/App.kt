package com.renaudmathieu

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.logger.Level
import org.koin.dsl.KoinConfiguration

@Serializable
internal object Home

@Serializable
internal data class MusicPlayerRoute(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val duration: Long,
)

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
            GradientSurface {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Home
                ) {
                    composable<Home> {
                        HomeScreen(
                            onTrackSelected = { track ->
                                navController.navigate(
                                    MusicPlayerRoute(
                                        id = track.id,
                                        title = track.title,
                                        artist = track.artist,
                                        url = track.url,
                                        duration = track.duration,
                                    )
                                )
                            }
                        )
                    }
                    composable<MusicPlayerRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<MusicPlayerRoute>()
                        val track = Track(
                            id = route.id,
                            title = route.title,
                            artist = route.artist,
                            url = route.url,
                            duration = route.duration
                        )
                        MusicPlayer(onBack = { navController.navigateUp() }, initialTrack = track)
                    }
                }
            }
        }
    }
}
