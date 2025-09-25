package com.renaudmathieu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var selectedTrack by remember { mutableStateOf<Track?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                title = {
                    Text(
                        text = "GrooveBox",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
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

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                text = "Error: $error",
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        modifier = Modifier.fillMaxWidth()
                            .padding(
                                start = 32.dp,
                                end = 32.dp,
                                top = 24.dp
                            ),
                        text = "Tracks",
                        //color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 32.dp,
                                end = 32.dp,
                                bottom = 32.dp,
                                top = 8.dp
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tracks) { track ->
                                ListItem(
                                    modifier = Modifier
                                        .clickable { selectedTrack = track },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Black.copy(alpha = 0.0f)
                                    ),
                                    headlineContent = {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            //color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            modifier = Modifier,
                                            text = track.artist,
                                            style = MaterialTheme.typography.bodyMedium,
                                            //color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    },
                                    trailingContent = {
                                        FilledIconButton(
                                            onClick = { selectedTrack = track }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                            )
                                        }
                                    },

                                    )
                            }
                        }
                    }
                }
            }
            if (selectedTrack != null) {
                ModalBottomSheet(
                    shape = MaterialTheme.shapes.extraExtraLarge,
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = Color.Transparent,
                    onDismissRequest = { selectedTrack = null },
                    dragHandle = {}
                ) {
                    GradientSurface {
                        MusicPlayer(
                            modifier = Modifier.padding(16.dp),
                            onBack = { selectedTrack = null },
                            initialTrack = selectedTrack
                        )
                    }
                }
            }
        }
    }
}
