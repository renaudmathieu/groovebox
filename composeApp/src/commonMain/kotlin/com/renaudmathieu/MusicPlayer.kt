package com.renaudmathieu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import groovebox.composeapp.generated.resources.Res
import groovebox.composeapp.generated.resources.daft_punk
import groovebox.composeapp.generated.resources.pause
import groovebox.composeapp.generated.resources.play_arrow
import groovebox.composeapp.generated.resources.skip_next
import groovebox.composeapp.generated.resources.skip_previous
import groovebox.composeapp.generated.resources.volume_down
import groovebox.composeapp.generated.resources.volume_up
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayer(
    modifier: Modifier = Modifier,
    viewModel: MusicPlayerViewModel = koinViewModel(),
    onBack: () -> Unit,
    initialTrack: Track? = null,
) {

    val uiState by viewModel.musicPlayerUiState.collectAsStateWithLifecycle()

    if (initialTrack == null) {
        // Collect initial track from ViewModel and set data source when available
        val autoTrack by viewModel.initialTrack.collectAsStateWithLifecycle()
        LaunchedEffect(autoTrack) {
            autoTrack?.let { viewModel.setDataSource(it) }
        }
    } else {
        LaunchedEffect(initialTrack) {
            viewModel.setDataSource(initialTrack)
        }
    }

    LaunchedEffect(uiState.isPlaying) {
        while (uiState.isPlaying) {
            viewModel.updatePosition()
            delay(100)
        }
    }

    // Manage lifecycle: release player on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GrooveBox",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(vertical = 24.dp)
                .fillMaxSize(),
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
                    text = uiState.track.title,
                    style = MaterialTheme.typography.headlineLarge,
                )

                Text(
                    text = uiState.track.artist,
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
}
