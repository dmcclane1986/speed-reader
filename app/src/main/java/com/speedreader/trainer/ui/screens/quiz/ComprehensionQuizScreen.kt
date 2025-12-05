package com.speedreader.trainer.ui.screens.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ComprehensionQuizScreen(
    sessionId: String,
    onQuizComplete: (String) -> Unit,
    viewModel: ComprehensionQuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadQuiz(sessionId)
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onQuizComplete(sessionId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading quiz...")
                }
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.error ?: "Error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onQuizComplete(sessionId) }) {
                        Text("Continue")
                    }
                }
            }
        } else {
            val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
            val progress = (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size.coerceAtLeast(1)
            val isLastQuestion = uiState.currentQuestionIndex >= uiState.questions.size - 1

            Text(
                text = "Comprehension Quiz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            currentQuestion?.let { question ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                question.options.forEachIndexed { index, option ->
                    QuizAnswerOption(
                        option = option,
                        index = index,
                        isSelected = uiState.selectedAnswer == index,
                        isCorrect = question.correctAnswerIndex == index,
                        isChecked = uiState.isAnswerChecked,
                        onSelect = { viewModel.selectAnswer(index) }
                    )
                }

                // Feedback message
                if (uiState.showFeedback) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val isCorrect = uiState.selectedAnswer == question.correctAnswerIndex
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isCorrect) "Correct!" else "Incorrect. The correct answer is highlighted above.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (uiState.isAnswerChecked) {
                        viewModel.nextQuestion()
                    } else {
                        viewModel.checkAnswer()
                    }
                },
                enabled = uiState.selectedAnswer != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    when {
                        uiState.isAnswerChecked && isLastQuestion -> "Finish Quiz"
                        uiState.isAnswerChecked -> "Next Question"
                        else -> "Check Answer"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizAnswerOption(
    option: String,
    @Suppress("UNUSED_PARAMETER") index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isChecked: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = when {
        isChecked && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        isChecked && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isChecked && isCorrect -> Color(0xFF4CAF50)
        isChecked && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                role = Role.RadioButton,
                enabled = !isChecked
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                enabled = !isChecked
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isChecked && isCorrect) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Correct",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

