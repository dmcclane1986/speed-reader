package com.speedreader.trainer.ui.screens.baseline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.UserRepository
import com.speedreader.trainer.domain.model.BaselineTestContent
import com.speedreader.trainer.domain.model.ComprehensionQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BaselineUiState(
    val passage: String = BaselineTestContent.passage,
    val wordCount: Int = BaselineTestContent.wordCount,
    val questions: List<ComprehensionQuestion> = BaselineTestContent.questions,
    val currentQuestionIndex: Int = 0,
    val userAnswers: MutableList<Int> = mutableListOf(),
    val elapsedTimeSeconds: Int = 0,
    val isReading: Boolean = false,
    val calculatedWpm: Int = 0,
    val comprehensionScore: Float = 0f,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class BaselineViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BaselineUiState())
    val uiState: StateFlow<BaselineUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startReading() {
        _uiState.value = _uiState.value.copy(isReading = true, elapsedTimeSeconds = 0)
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    elapsedTimeSeconds = _uiState.value.elapsedTimeSeconds + 1
                )
            }
        }
    }

    fun finishReading() {
        timerJob?.cancel()
        val elapsed = _uiState.value.elapsedTimeSeconds.coerceAtLeast(1)
        val wordCount = _uiState.value.wordCount
        val wpm = (wordCount.toFloat() / elapsed * 60).toInt()
        
        _uiState.value = _uiState.value.copy(
            isReading = false,
            calculatedWpm = wpm
        )
    }

    fun answerQuestion(answerIndex: Int) {
        val currentState = _uiState.value
        val newAnswers = currentState.userAnswers.toMutableList()
        
        // Update or add answer
        if (currentState.currentQuestionIndex < newAnswers.size) {
            newAnswers[currentState.currentQuestionIndex] = answerIndex
        } else {
            newAnswers.add(answerIndex)
        }
        
        _uiState.value = currentState.copy(userAnswers = newAnswers)
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1
            )
        }
    }

    fun previousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex - 1
            )
        }
    }

    fun calculateAndSaveResults() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // Calculate comprehension score
            var correctCount = 0
            currentState.questions.forEachIndexed { index, question ->
                if (index < currentState.userAnswers.size &&
                    currentState.userAnswers[index] == question.correctAnswerIndex) {
                    correctCount++
                }
            }
            val score = correctCount.toFloat() / currentState.questions.size

            _uiState.value = currentState.copy(
                comprehensionScore = score,
                isSaving = true
            )

            // Save to Firebase
            val result = userRepository.updateBaselineResults(
                wpm = currentState.calculatedWpm,
                comprehension = score
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = e.message
                    )
                }
            )
        }
    }

    fun getCurrentAnswer(): Int? {
        val currentState = _uiState.value
        return if (currentState.currentQuestionIndex < currentState.userAnswers.size) {
            currentState.userAnswers[currentState.currentQuestionIndex]
        } else {
            null
        }
    }

    fun isQuizComplete(): Boolean {
        val currentState = _uiState.value
        return currentState.userAnswers.size == currentState.questions.size
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

