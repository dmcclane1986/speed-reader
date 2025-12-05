package com.speedreader.trainer.ui.screens.baseline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.UserRepository
import com.speedreader.trainer.domain.model.BaselineTestData
import com.speedreader.trainer.domain.model.ComprehensionQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BaselineUiState(
    val passage: String = BaselineTestData.test.passage,
    val wordCount: Int = BaselineTestData.test.wordCount,
    val questions: List<ComprehensionQuestion> = BaselineTestData.test.questions,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val readingStartTime: Long = 0,
    val readingEndTime: Long = 0,
    val isQuizComplete: Boolean = false,
    val calculatedWpm: Int = 0,
    val comprehensionScore: Float = 0f,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class BaselineViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BaselineUiState())
    val uiState: StateFlow<BaselineUiState> = _uiState.asStateFlow()

    fun startReading() {
        _uiState.value = _uiState.value.copy(readingStartTime = System.currentTimeMillis())
    }

    fun finishReading() {
        val endTime = System.currentTimeMillis()
        val startTime = _uiState.value.readingStartTime
        val durationMinutes = (endTime - startTime) / 60000.0
        val wpm = if (durationMinutes > 0) {
            (_uiState.value.wordCount / durationMinutes).toInt()
        } else {
            250 // Default if timing fails
        }
        
        Log.d("BaselineViewModel", "Reading finished: duration=${durationMinutes}min, wordCount=${_uiState.value.wordCount}, wpm=$wpm")
        
        _uiState.value = _uiState.value.copy(
            readingEndTime = endTime,
            calculatedWpm = wpm
        )
    }

    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        val newAnswers = _uiState.value.selectedAnswers + (questionIndex to answerIndex)
        _uiState.value = _uiState.value.copy(selectedAnswers = newAnswers)
    }

    fun nextQuestion() {
        val current = _uiState.value.currentQuestionIndex
        if (current < _uiState.value.questions.size - 1) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = current + 1)
        }
    }

    fun finishQuiz() {
        val answers = _uiState.value.selectedAnswers
        val questions = _uiState.value.questions
        
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
        
        Log.d("BaselineViewModel", "Quiz finished: correct=$correct, total=${questions.size}, score=$score, wpm=${_uiState.value.calculatedWpm}")
        
        _uiState.value = _uiState.value.copy(
            isQuizComplete = true,
            comprehensionScore = score
        )
        
        saveResults()
    }

    private fun saveResults() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            
            val wpm = _uiState.value.calculatedWpm
            val score = _uiState.value.comprehensionScore
            
            Log.d("BaselineViewModel", "Saving results: wpm=$wpm, score=$score")
            
            val result = userRepository.updateBaselineResults(wpm, score)
            
            result.fold(
                onSuccess = {
                    Log.d("BaselineViewModel", "Results saved successfully")
                    _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
                },
                onFailure = { error ->
                    Log.e("BaselineViewModel", "Failed to save results", error)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = error.message ?: "Failed to save results"
                    )
                }
            )
        }
    }
}

