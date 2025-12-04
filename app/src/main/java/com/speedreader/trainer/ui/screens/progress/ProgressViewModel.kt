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
    val baselineWpm: Int = 0,
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val recentSessions: List<ReadingSession> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionRepository: ReadingSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            val sessions = sessionRepository.getRecentSessions(10)

            _uiState.value = ProgressUiState(
                baselineWpm = profile?.baselineWpm ?: 0,
                totalSessions = profile?.sessionsCompleted ?: 0,
                totalMinutes = ((profile?.totalReadingTimeSeconds ?: 0) / 60).toInt(),
                currentStreak = profile?.currentStreak ?: 0,
                recentSessions = sessions,
                isLoading = false
            )
        }
    }
}

