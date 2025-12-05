package com.speedreader.trainer.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.UserRepository
import com.speedreader.trainer.domain.model.ReadingSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val sessions: List<ReadingSession> = emptyList(),
    val totalSessions: Int = 0,
    val totalReadingTime: Long = 0,
    val sessionsCompleted: Int = 0,
    val averageWpm: Int = 0,
    val averageComprehension: Float = 0f,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val sessionRepository: ReadingSessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            val sessions = sessionRepository.getRecentSessions(50)
            val totalSessions = sessionRepository.getAllSessionsCount()
            
            val avgWpm = if (sessions.isNotEmpty()) {
                sessions.map { it.wpmUsed }.average().toInt()
            } else 0
            
            val sessionsWithQuiz = sessions.filter { it.hasQuiz }
            val avgComprehension = if (sessionsWithQuiz.isNotEmpty()) {
                sessionsWithQuiz.map { it.comprehensionScore }.average().toFloat()
            } else 0f
            
            _uiState.value = _uiState.value.copy(
                sessions = sessions,
                totalSessions = totalSessions,
                totalReadingTime = profile?.totalReadingTimeSeconds ?: 0,
                sessionsCompleted = profile?.sessionsCompleted ?: 0,
                averageWpm = avgWpm,
                averageComprehension = avgComprehension,
                currentStreak = profile?.currentStreak ?: 0,
                isLoading = false
            )
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val result = sessionRepository.deleteSession(sessionId)
            if (result.isSuccess) {
                // Reload the data
                loadProgress()
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadProgress()
    }
}

