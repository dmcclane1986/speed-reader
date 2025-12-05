package com.speedreader.trainer.ui.screens.quiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpeedAdjustment(
    val currentWpm: Int,
    val recommendedWpm: Int,
    val message: String,
    val isIncrease: Boolean
)

data class SessionResultsUiState(
    val wordsRead: Int = 0,
    val wpmUsed: Int = 0,
    val comprehensionScore: Float = 0f,
    val durationSeconds: Int = 0,
    val hasQuiz: Boolean = true,
    val isLoading: Boolean = true,
    val speedAdjustment: SpeedAdjustment? = null,
    val isSpeedApplied: Boolean = false
)

@HiltViewModel
class SessionResultsViewModel @Inject constructor(
    private val sessionRepository: ReadingSessionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionResultsUiState())
    val uiState: StateFlow<SessionResultsUiState> = _uiState.asStateFlow()

    @Suppress("UNUSED_PARAMETER")
    fun loadResults(sessionId: String) {
        viewModelScope.launch {
            val sessions = sessionRepository.getRecentSessions(1)
            val session = sessions.firstOrNull()
            
            if (session != null) {
                val speedAdjustment = if (session.hasQuiz) {
                    calculateSpeedAdjustment(session.comprehensionScore, session.wpmUsed)
                } else null
                
                _uiState.value = _uiState.value.copy(
                    wordsRead = session.wordsRead,
                    wpmUsed = session.wpmUsed,
                    comprehensionScore = session.comprehensionScore,
                    durationSeconds = session.durationSeconds,
                    hasQuiz = session.hasQuiz,
                    speedAdjustment = speedAdjustment,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun calculateSpeedAdjustment(score: Float, currentWpm: Int): SpeedAdjustment {
        return when {
            score >= 90 -> {
                // Excellent! Increase speed by 10-25%
                val increase = (currentWpm * 0.15).toInt() // 15% middle ground
                val newWpm = currentWpm + increase
                SpeedAdjustment(
                    currentWpm = currentWpm,
                    recommendedWpm = newWpm,
                    message = "Excellent comprehension! You're reading comfortably. Let's increase your speed to challenge yourself.",
                    isIncrease = true
                )
            }
            score >= 70 -> {
                // Good zone - maintain
                SpeedAdjustment(
                    currentWpm = currentWpm,
                    recommendedWpm = currentWpm,
                    message = "Great job! You're in the ideal learning zone. Maintain this speed until comprehension improves further.",
                    isIncrease = false
                )
            }
            else -> {
                // Below 70% - decrease speed by 5-10%
                val decrease = (currentWpm * 0.08).toInt() // 8% middle ground
                val newWpm = (currentWpm - decrease).coerceAtLeast(100)
                SpeedAdjustment(
                    currentWpm = currentWpm,
                    recommendedWpm = newWpm,
                    message = "Your speed might be too high. Let's slow down a bit to improve comprehension.",
                    isIncrease = false
                )
            }
        }
    }

    fun applySpeedAdjustment() {
        val adjustment = _uiState.value.speedAdjustment ?: return
        
        viewModelScope.launch {
            Log.d("SpeedAdjustment", "Applying new WPM: ${adjustment.recommendedWpm}")
            settingsRepository.setDefaultWpm(adjustment.recommendedWpm)
            _uiState.value = _uiState.value.copy(isSpeedApplied = true)
        }
    }

    fun declineSpeedAdjustment() {
        _uiState.value = _uiState.value.copy(isSpeedApplied = true)
    }
}

