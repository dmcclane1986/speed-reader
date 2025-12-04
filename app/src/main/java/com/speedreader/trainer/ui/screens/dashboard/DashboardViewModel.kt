package com.speedreader.trainer.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.DocumentRepository
import com.speedreader.trainer.data.repository.UserRepository
import com.speedreader.trainer.domain.model.UserDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val displayName: String = "",
    val baselineWpm: Int = 0,
    val baselineComprehension: Float = 0f,
    val sessionsCompleted: Int = 0,
    val currentStreak: Int = 0,
    val totalReadingTimeMinutes: Int = 0,
    val recentDocuments: List<UserDocument> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                userRepository.getUserProfileFlow(),
                documentRepository.getUserDocumentsFlow()
            ) { profile, documents ->
                DashboardUiState(
                    displayName = profile?.displayName ?: "",
                    baselineWpm = profile?.baselineWpm ?: 0,
                    baselineComprehension = profile?.baselineComprehension ?: 0f,
                    sessionsCompleted = profile?.sessionsCompleted ?: 0,
                    currentStreak = profile?.currentStreak ?: 0,
                    totalReadingTimeMinutes = ((profile?.totalReadingTimeSeconds ?: 0) / 60).toInt(),
                    recentDocuments = documents.take(5),
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

