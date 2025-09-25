package com.renaudmathieu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onTrackSelected: (Track) -> Unit,
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    GradientSurface {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    ),
                    title = {
                        Text(
                            text = "GrooveBox",
                        )
                    },
                    subtitle = {
                        Text(
                            text = "droidcon Berlin 2025",
                        )
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }

                    error != null -> {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tracks) { track ->
                                ListItem(
                                    modifier = Modifier
                                        .clickable { onTrackSelected(track) },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Black.copy(alpha = 0.0f)
                                    ),
                                    headlineContent = {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = track.artist,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingContent = {
                                        Button(onClick = { onTrackSelected(track) }) {
                                            Text("Play")
                                        }
                                    },

                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}
