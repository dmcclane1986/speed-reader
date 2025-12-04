package com.speedreader.trainer.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.UserRepository
import com.speedreader.trainer.domain.model.ComprehensionQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComprehensionQuizUiState(
    val sessionId: String = "",
    val questions: List<ComprehensionQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val userAnswers: MutableMap<Int, Int> = mutableMapOf(),
    val selectedAnswer: Int? = null,
    val isLoading: Boolean = true,
    val isComplete: Boolean = false,
    val canSubmit: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ComprehensionQuizViewModel @Inject constructor(
    private val sessionRepository: ReadingSessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComprehensionQuizUiState())
    val uiState: StateFlow<ComprehensionQuizUiState> = _uiState.asStateFlow()

    fun loadQuestions(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sessionId = sessionId,
                isLoading = true
            )

            val questions = sessionRepository.getPendingQuestions(sessionId)
            
            _uiState.value = _uiState.value.copy(
                questions = questions,
                isLoading = false,
                selectedAnswer = null
            )
        }
    }

    fun selectAnswer(index: Int) {
        val currentState = _uiState.value
        val newAnswers = currentState.userAnswers.toMutableMap()
        newAnswers[currentState.currentQuestionIndex] = index
        
        _uiState.value = currentState.copy(
            selectedAnswer = index,
            userAnswers = newAnswers,
            canSubmit = newAnswers.size == currentState.questions.size
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer = currentState.userAnswers[nextIndex]
            )
        }
    }

    fun previousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            val prevIndex = currentState.currentQuestionIndex - 1
            _uiState.value = currentState.copy(
                currentQuestionIndex = prevIndex,
                selectedAnswer = currentState.userAnswers[prevIndex]
            )
        }
    }

    fun submitQuiz() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // Calculate score
            var correctCount = 0
            currentState.questions.forEachIndexed { index, question ->
                val userAnswer = currentState.userAnswers[index]
                if (userAnswer == question.correctAnswerIndex) {
                    correctCount++
                }
            }
            
            val score = if (currentState.questions.isNotEmpty()) {
                correctCount.toFloat() / currentState.questions.size
            } else {
                0f
            }

            // Complete the session
            val result = sessionRepository.completeSession(currentState.sessionId, score)
            
            result.fold(
                onSuccess = { session ->
                    // Update user stats
                    userRepository.updateSessionStats(
                        durationSeconds = session.durationSeconds,
                        comprehensionScore = score
                    )
                    
                    _uiState.value = currentState.copy(isComplete = true)
                },
                onFailure = { e ->
                    _uiState.value = currentState.copy(error = e.message)
                }
            )
        }
    }
}

