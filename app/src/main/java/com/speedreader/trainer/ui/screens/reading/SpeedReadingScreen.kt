package com.speedreader.trainer.ui.screens.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.speedreader.trainer.ui.theme.ReadingBackground
import com.speedreader.trainer.ui.theme.ReadingBackgroundDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedReadingScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    onSessionComplete: (String, Boolean) -> Unit,
    viewModel: SpeedReadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    val backgroundColor = if (uiState.readingDarkModeEnabled) {
        ReadingBackgroundDark
    } else {
        ReadingBackground
    }

    val contentColor = if (uiState.readingDarkModeEnabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    // Continue Dialog
    if (uiState.showContinueDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.startFromBeginning() },
            title = { Text("Continue Reading?") },
            text = { 
                Text("You've previously read ${uiState.savedProgress} words (${(uiState.savedProgress * 100 / uiState.words.size.coerceAtLeast(1))}%). Would you like to continue where you left off?")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.continueFromSaved() }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.startFromBeginning() }) {
                    Text("Start Over")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.documentTitle, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.currentWordIndex > 0 || uiState.isFinished) {
                        TextButton(
                            onClick = {
                                val (sessionId, shouldShowQuiz) = viewModel.finishReading()
                                onSessionComplete(sessionId, shouldShowQuiz)
                            }
                        ) {
                            Text("Finish")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Progress
                LinearProgressIndicator(
                    progress = uiState.progress,
                    modifier = Modifier.fillMaxWidth()
                )

                // Word Display Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.currentChunk,
                        fontSize = uiState.fontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // WPM Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Speed",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = uiState.wpm.toFloat(),
                                onValueChange = { viewModel.setWpm(it.toInt()) },
                                valueRange = 100f..1000f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "${uiState.wpm} WPM",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Font Size Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Size",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Slider(
                                value = uiState.fontSize.toFloat(),
                                onValueChange = { viewModel.setFontSize(it.toInt()) },
                                valueRange = 24f..72f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "${uiState.fontSize}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Chunking Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Chunking",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = uiState.chunkingEnabled,
                                    onCheckedChange = { viewModel.setChunkingEnabled(it) }
                                )
                            }
                            
                            if (uiState.chunkingEnabled) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Words: ", style = MaterialTheme.typography.bodySmall)
                                    (2..5).forEach { size ->
                                        FilterChip(
                                            selected = uiState.chunkSize == size,
                                            onClick = { viewModel.setChunkSize(size) },
                                            label = { Text("$size") },
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Navigation and Play Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Go to sentence start
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { viewModel.goBackToSentenceStart() },
                                    enabled = !uiState.isPlaying
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Sentence start",
                                        tint = if (!uiState.isPlaying) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                                Text(
                                    text = "Sentence",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Go back one word/chunk
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { viewModel.goBackOneWord() },
                                    enabled = !uiState.isPlaying
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay,
                                        contentDescription = "Back one",
                                        tint = if (!uiState.isPlaying) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                                Text(
                                    text = "Back",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Play/Pause Button
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                FloatingActionButton(
                                    onClick = {
                                        if (uiState.isPlaying) viewModel.pause() else viewModel.play()
                                    },
                                    shape = CircleShape,
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            
                            // Skip forward one word/chunk
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(
                                    onClick = { viewModel.skipForwardOneWord() },
                                    enabled = !uiState.isPlaying
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forward,
                                        contentDescription = "Forward one",
                                        tint = if (!uiState.isPlaying) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                                Text(
                                    text = "Skip",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Progress info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Word ${uiState.currentWordIndex}/${uiState.words.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(uiState.progress * 100).toInt()}% complete",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

