package com.speedreader.trainer.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.UserRepository
import com.speedreader.trainer.domain.model.ComprehensionQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComprehensionQuizUiState(
    val questions: List<ComprehensionQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val isAnswerChecked: Boolean = false,
    val showFeedback: Boolean = false,
    val answers: Map<Int, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val isComplete: Boolean = false,
    val score: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class ComprehensionQuizViewModel @Inject constructor(
    private val sessionRepository: ReadingSessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComprehensionQuizUiState())
    val uiState: StateFlow<ComprehensionQuizUiState> = _uiState.asStateFlow()

    private var sessionId: String = ""

    fun loadQuiz(sessId: String) {
        sessionId = sessId
        viewModelScope.launch {
            // First try to get questions immediately
            var questions = sessionRepository.getPendingQuestions()
            
            // If not ready, wait for them (with timeout)
            if (questions == null || questions.isEmpty()) {
                questions = sessionRepository.waitForQuestions(timeoutMs = 30000)
            }
            
            if (questions != null && questions.isNotEmpty()) {
                android.util.Log.d("ComprehensionQuiz", "Loaded ${questions.size} questions")
                _uiState.value = _uiState.value.copy(
                    questions = questions,
                    isLoading = false
                )
            } else {
                android.util.Log.e("ComprehensionQuiz", "Failed to load questions - questions were null or empty")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load questions. Please try again."
                )
            }
        }
    }

    fun selectAnswer(answerIndex: Int) {
        if (!_uiState.value.isAnswerChecked) {
            _uiState.value = _uiState.value.copy(selectedAnswer = answerIndex)
        }
    }

    fun checkAnswer() {
        val selectedAnswer = _uiState.value.selectedAnswer ?: return
        val currentIndex = _uiState.value.currentQuestionIndex
        
        val newAnswers = _uiState.value.answers + (currentIndex to selectedAnswer)
        _uiState.value = _uiState.value.copy(
            answers = newAnswers,
            isAnswerChecked = true,
            showFeedback = true
        )
    }

    fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val isLastQuestion = currentIndex >= _uiState.value.questions.size - 1
        
        if (isLastQuestion) {
            calculateAndSaveResults()
        } else {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = currentIndex + 1,
                selectedAnswer = null,
                isAnswerChecked = false,
                showFeedback = false
            )
        }
    }

    private fun calculateAndSaveResults() {
        val questions = _uiState.value.questions
        val answers = _uiState.value.answers
        
        var correct = 0
        questions.forEachIndexed { index, question ->
            if (answers[index] == question.correctAnswerIndex) {
                correct++
            }
        }
        
        val score = if (questions.isNotEmpty()) {
            (correct.toFloat() / questions.size) * 100
        } else {
            0f
        }
        
        viewModelScope.launch {
            sessionRepository.completeSession(score)
            userRepository.updateSessionStats(0, score)
            
            _uiState.value = _uiState.value.copy(
                isComplete = true,
                score = score
            )
        }
    }
}

