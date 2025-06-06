package com.renaudmathieu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import groovebox.composeapp.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MusicPlayer(
    modifier: Modifier = Modifier,
    viewModel: MusicPlayerViewModel = koinViewModel()
) {

    val uiState by viewModel.musicPlayerUiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isPlaying) {
        while (uiState.isPlaying) {
            viewModel.updatePosition()
            delay(100)
        }
    }

    // Set up the track when the composable is first created
    DisposableEffect(Unit) {
        viewModel.setDataSource(uiState.track)
        onDispose {
            viewModel.release()
        }
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {

        Card(
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Image(
                modifier = Modifier.size(256.dp),
                painter = painterResource(Res.drawable.daft_punk),
                contentDescription = "Artwork"
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Make Love",
                style = MaterialTheme.typography.headlineLarge,
            )

            Text(
                text = "Daft Punk",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF2c3647),
                fontFamily = RobotoFontFamily()
            )
        }

        LinearWavyProgressIndicator(
            progress = {
                if (uiState.duration > 0) uiState.currentPosition.toFloat() / uiState.duration.toFloat() else 0f
            },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            FilledIconButton(
                modifier =
                    Modifier
                        .shadow(
                            elevation = 8.dp,
                            spotColor = MaterialTheme.colorScheme.secondary,
                            shape = MaterialTheme.shapes.extraExtraLarge,
                        )
                        .size(
                            IconButtonDefaults.mediumContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Narrow
                            )
                        ),
                onClick = {
                    viewModel.seekTo(0)
                },
                shapes = IconButtonDefaults.shapes(
                    shape = MaterialTheme.shapes.extraExtraLarge,
                    pressedShape = MaterialTheme.shapes.large,
                ),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.skip_previous),
                    contentDescription = "Previous"
                )
            }

            FilledIconButton(
                modifier =
                    Modifier
                        .shadow(
                            elevation = 8.dp,
                            spotColor = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.extraExtraLarge,
                        )
                        .size(
                            IconButtonDefaults.largeContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Uniform
                            )
                        ),
                onClick = {
                    viewModel.togglePlayPause()
                },
                shapes = IconButtonDefaults.shapes(
                    shape = MaterialTheme.shapes.extraExtraLarge,
                    pressedShape = MaterialTheme.shapes.large,
                ),
            ) {
                Icon(
                    painter = painterResource(if (uiState.isPlaying) Res.drawable.pause else Res.drawable.play_arrow),
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play"
                )
            }

            FilledIconButton(
                modifier =
                    Modifier
                        .shadow(
                            elevation = 8.dp,
                            spotColor = MaterialTheme.colorScheme.secondary,
                            shape = MaterialTheme.shapes.extraExtraLarge,
                        )
                        .size(
                            IconButtonDefaults.mediumContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Narrow
                            )
                        ),
                onClick = {
                    if (uiState.duration > 0) {
                        viewModel.seekTo(uiState.duration)
                    }
                },
                shapes = IconButtonDefaults.shapes(
                    shape = MaterialTheme.shapes.extraExtraLarge,
                    pressedShape = MaterialTheme.shapes.large,
                ),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.skip_next),
                    contentDescription = "Next"
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            FilledTonalIconButton(
                modifier =
                    Modifier
                        .size(
                            IconButtonDefaults.mediumContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Uniform
                            )
                        ),
                onClick = {
                    // Volume control is not consistently available across platforms
                    // This is a placeholder for volume down functionality
                    // In a real app, you might implement platform-specific volume control
                    println("Volume down button clicked")
                },
                shapes = IconButtonDefaults.shapes(
                    shape = MaterialTheme.shapes.large,
                    pressedShape = MaterialTheme.shapes.small,
                ),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.volume_down),
                    contentDescription = "Volume Down"
                )
            }

            FilledTonalIconButton(
                modifier =
                    Modifier
                        .size(
                            IconButtonDefaults.mediumContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Uniform
                            )
                        ),
                onClick = {
                    // Volume control is not consistently available across platforms
                    // This is a placeholder for volume up functionality
                    // In a real app, you might implement platform-specific volume control
                    println("Volume up button clicked")
                },
                shapes = IconButtonDefaults.shapes(
                    shape = MaterialTheme.shapes.large,
                    pressedShape = MaterialTheme.shapes.small,
                ),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.volume_up),
                    contentDescription = "Volume Up"
                )
            }
        }

    }
}
