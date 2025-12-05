package com.speedreader.trainer.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.DocumentRepository
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.SettingsRepository
import com.speedreader.trainer.data.repository.UserRepository
import com.speedreader.trainer.domain.model.UserDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val displayName: String = "",
    val currentWpm: Int = 250,
    val latestComprehensionScore: Float? = null,
    val averageComprehensionScore: Float? = null,
    val totalReadingTime: Long = 0,
    val sessionsCompleted: Int = 0,
    val currentStreak: Int = 0,
    val recentDocuments: List<UserDocument> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository,
    private val sessionRepository: ReadingSessionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeDocuments()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            val currentWpm = settingsRepository.defaultWpmFlow.first()
            val latestScore = sessionRepository.getLatestComprehensionScore()
            val averageScore = sessionRepository.getAverageComprehensionScore()
            
            _uiState.value = _uiState.value.copy(
                displayName = profile?.displayName ?: "",
                currentWpm = currentWpm,
                latestComprehensionScore = latestScore,
                averageComprehensionScore = averageScore,
                totalReadingTime = profile?.totalReadingTimeSeconds ?: 0,
                sessionsCompleted = profile?.sessionsCompleted ?: 0,
                currentStreak = profile?.currentStreak ?: 0,
                isLoading = false
            )
        }
    }

    private fun observeDocuments() {
        viewModelScope.launch {
            documentRepository.getDocumentsFlow().collect { documents ->
                _uiState.value = _uiState.value.copy(
                    recentDocuments = documents.take(3)
                )
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadData()
    }
}

