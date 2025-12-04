package com.speedreader.trainer.ui.screens.baseline

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BaselineQuizScreen(
    viewModel: BaselineViewModel = hiltViewModel(),
    onQuizComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = uiState.questions.getOrNull(uiState.currentQuestionIndex)
    val selectedAnswer = viewModel.getCurrentAnswer()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                        text = "Comprehension Quiz",
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
                    progress = { (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size },
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
                    AnswerOption(
                        text = option,
                        isSelected = selectedAnswer == index,
                        onClick = { viewModel.answerQuestion(index) }
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
                        enabled = selectedAnswer != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.calculateAndSaveResults()
                            onQuizComplete()
                        },
                        enabled = viewModel.isQuizComplete(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
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

