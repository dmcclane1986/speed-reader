package com.speedreader.trainer.ui.screens.quiz

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.speedreader.trainer.ui.theme.*

@Composable
fun SessionResultsScreen(
    sessionId: String,
    viewModel: SessionResultsViewModel = hiltViewModel(),
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    // Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Teal900, Teal700)
                )
            )
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Success Icon
                val iconColor = when {
                    uiState.comprehensionScore >= 0.8f -> Success
                    uiState.comprehensionScore >= 0.5f -> Warning
                    else -> Error
                }
                
                Surface(
                    shape = CircleShape,
                    color = iconColor,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when {
                                uiState.comprehensionScore >= 0.8f -> Icons.Default.EmojiEvents
                                uiState.comprehensionScore >= 0.5f -> Icons.Default.ThumbUp
                                else -> Icons.Default.TrendingUp
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = when {
                        uiState.comprehensionScore >= 0.8f -> "Excellent!"
                        uiState.comprehensionScore >= 0.5f -> "Good Job!"
                        else -> "Keep Practicing!"
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = "Session Complete",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Results Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = "WPM Used",
                        value = "${uiState.wpmUsed}"
                    )
                    ResultStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Psychology,
                        label = "Comprehension",
                        value = "${(uiState.comprehensionScore * 100).toInt()}%"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TextFields,
                        label = "Words Read",
                        value = "${uiState.wordsRead}"
                    )
                    ResultStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = formatDuration(uiState.durationSeconds)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Feedback
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = getFeedback(uiState.comprehensionScore, uiState.wpmUsed),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"
}

private fun getFeedback(comprehension: Float, wpm: Int): String {
    return when {
        comprehension >= 0.8f && wpm >= 400 -> 
            "Outstanding! You're reading fast AND understanding well. Keep pushing your limits!"
        comprehension >= 0.8f -> 
            "Great comprehension! Try gradually increasing your speed to challenge yourself."
        comprehension >= 0.5f && wpm >= 300 -> 
            "Good balance of speed and comprehension. With more practice, both will improve."
        comprehension >= 0.5f -> 
            "Solid understanding. Try increasing your speed slightly in the next session."
        wpm >= 400 -> 
            "You're reading fast! Consider slowing down a bit to improve comprehension."
        else -> 
            "Keep practicing! Focus on understanding the content first, then gradually increase speed."
    }
}

