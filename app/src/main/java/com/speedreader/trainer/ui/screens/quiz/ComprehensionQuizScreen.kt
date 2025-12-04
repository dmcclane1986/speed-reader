package com.speedreader.trainer.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ComprehensionQuizScreen(
    sessionId: String,
    viewModel: ComprehensionQuizViewModel = hiltViewModel(),
    onQuizComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadQuestions(sessionId)
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onQuizComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Generating comprehension questions...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Progress Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comprehension Check",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { 
                            if (uiState.questions.isNotEmpty()) {
                                (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size
                            } else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Question Content
            val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
            
            currentQuestion?.let { question ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp)
                ) {
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    question.options.forEachIndexed { index, option ->
                        QuizAnswerOption(
                            text = option,
                            isSelected = uiState.selectedAnswer == index,
                            onClick = { viewModel.selectAnswer(index) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Navigation Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.previousQuestion() },
                        enabled = uiState.currentQuestionIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (uiState.currentQuestionIndex < uiState.questions.size - 1) {
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            enabled = uiState.selectedAnswer != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.submitQuiz() },
                            enabled = uiState.canSubmit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizAnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

