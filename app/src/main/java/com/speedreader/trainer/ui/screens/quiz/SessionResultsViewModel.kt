package com.speedreader.trainer.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionResultsUiState(
    val wpmUsed: Int = 0,
    val wordsRead: Int = 0,
    val comprehensionScore: Float = 0f,
    val durationSeconds: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class SessionResultsViewModel @Inject constructor(
    private val sessionRepository: ReadingSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionResultsUiState())
    val uiState: StateFlow<SessionResultsUiState> = _uiState.asStateFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            val session = sessionRepository.getSession(sessionId)
            
            session?.let {
                _uiState.value = SessionResultsUiState(
                    wpmUsed = it.wpmUsed,
                    wordsRead = it.wordsRead,
                    comprehensionScore = it.comprehensionScore,
                    durationSeconds = it.durationSeconds,
                    isLoading = false
                )
            }
        }
    }
}

