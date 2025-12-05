package com.speedreader.trainer.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SessionResultsScreen(
    sessionId: String,
    onContinue: () -> Unit,
    viewModel: SessionResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadResults(sessionId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.MenuBook,
                    title = "Words Read",
                    value = "${uiState.wordsRead}"
                )

                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Speed,
                    title = "Speed",
                    value = "${uiState.wpmUsed} WPM"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    title = "Duration",
                    value = formatDuration(uiState.durationSeconds)
                )

                if (uiState.hasQuiz) {
                    ResultStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Psychology,
                        title = "Comprehension",
                        value = "${uiState.comprehensionScore.toInt()}%",
                        valueColor = getScoreColor(uiState.comprehensionScore)
                    )
                } else {
                    ResultStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Info,
                        title = "Quiz",
                        value = "Skipped",
                        subtitle = "< 300 words"
                    )
                }
            }

            // Speed Adjustment Card
            uiState.speedAdjustment?.let { adjustment ->
                Spacer(modifier = Modifier.height(24.dp))
                SpeedAdjustmentCard(
                    adjustment = adjustment,
                    isApplied = uiState.isSpeedApplied,
                    onApply = { viewModel.applySpeedAdjustment() },
                    onNotNow = { viewModel.declineSpeedAdjustment() },
                    onContinue = onContinue
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.speedAdjustment == null || uiState.isSpeedApplied) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Continue to Dashboard")
                }
            }
        }
    }
}

@Composable
private fun ResultStatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpeedAdjustmentCard(
    adjustment: SpeedAdjustment,
    isApplied: Boolean,
    onApply: () -> Unit,
    onNotNow: () -> Unit,
    onContinue: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (adjustment.isIncrease)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else if (adjustment.recommendedWpm < adjustment.currentWpm)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (adjustment.isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingFlat,
                    contentDescription = null,
                    tint = if (adjustment.isIncrease) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Speed Recommendation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = adjustment.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (adjustment.currentWpm != adjustment.recommendedWpm) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Current",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${adjustment.currentWpm} WPM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Recommended",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "${adjustment.recommendedWpm} WPM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (adjustment.isIncrease) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!isApplied) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNotNow,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Not Now")
                        }
                        Button(
                            onClick = onApply,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (adjustment.isIncrease) "Go Faster!" else "Slow Down")
                        }
                    }
                } else {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"
}

private fun getScoreColor(score: Float): Color {
    return when {
        score >= 90 -> Color(0xFF4CAF50)
        score >= 70 -> Color(0xFFFFA000)
        else -> Color(0xFFE53935)
    }
}

