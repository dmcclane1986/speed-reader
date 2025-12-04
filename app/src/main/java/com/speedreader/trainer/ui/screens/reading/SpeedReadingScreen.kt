package com.speedreader.trainer.ui.screens.reading

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.speedreader.trainer.ui.theme.ReadingBackground
import com.speedreader.trainer.ui.theme.ReadingBackgroundDark
import com.speedreader.trainer.ui.theme.RsvpWordStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedReadingScreen(
    documentId: String,
    viewModel: SpeedReadingViewModel = hiltViewModel(),
    onSessionComplete: (sessionId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(uiState.sessionId) {
        uiState.sessionId?.let { sessionId ->
            onSessionComplete(sessionId)
        }
    }

    val backgroundColor = if (uiState.isDarkMode) ReadingBackgroundDark else ReadingBackground
    val textColor = if (uiState.isDarkMode) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.documentTitle,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopReading()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle dark mode"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Main reading area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (!uiState.isReading && uiState.currentWordIndex == 0) {
                    // Start screen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Ready to read",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${uiState.totalWords} words • ${uiState.estimatedMinutes} min at ${uiState.wpm} WPM",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.startReading() },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Reading")
                        }
                    }
                } else {
                    // RSVP word display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        // Word counter
                        Text(
                            text = "${uiState.currentWordIndex + 1} / ${uiState.totalWords}",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // Current word with focus point
                        AnimatedContent(
                            targetState = uiState.currentWord,
                            transitionSpec = {
                                fadeIn(animationSpec = snap()) togetherWith
                                fadeOut(animationSpec = snap())
                            },
                            label = "word_animation"
                        ) { word ->
                            Text(
                                text = word,
                                style = RsvpWordStyle.copy(
                                    fontSize = uiState.fontSize.sp,
                                    color = textColor
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // Speed indicator
                        Text(
                            text = "${uiState.wpm} WPM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // WPM Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Speed",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Slider(
                            value = uiState.wpm.toFloat(),
                            onValueChange = { viewModel.setWpm(it.toInt()) },
                            valueRange = 100f..1000f,
                            steps = 17,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.wpm}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(48.dp)
                        )
                    }

                    // Font size slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Size",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Slider(
                            value = uiState.fontSize.toFloat(),
                            onValueChange = { viewModel.setFontSize(it.toInt()) },
                            valueRange = 32f..80f,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.fontSize}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Playback controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Restart button
                        IconButton(
                            onClick = { viewModel.restart() },
                            enabled = uiState.currentWordIndex > 0
                        ) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = "Restart",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Rewind 10 words
                        IconButton(
                            onClick = { viewModel.skipBackward() },
                            enabled = uiState.currentWordIndex > 0
                        ) {
                            Icon(
                                Icons.Default.Replay10,
                                contentDescription = "Back 10",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Play/Pause button
                        FilledIconButton(
                            onClick = { 
                                if (uiState.isReading) viewModel.pauseReading() 
                                else viewModel.startReading() 
                            },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                if (uiState.isReading) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isReading) "Pause" else "Play",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Forward 10 words
                        IconButton(
                            onClick = { viewModel.skipForward() },
                            enabled = uiState.currentWordIndex < uiState.totalWords - 1
                        ) {
                            Icon(
                                Icons.Default.Forward10,
                                contentDescription = "Forward 10",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Finish button
                        IconButton(
                            onClick = { viewModel.finishReading() },
                            enabled = uiState.currentWordIndex > 0
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Finish",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

