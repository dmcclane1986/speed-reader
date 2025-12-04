package com.speedreader.trainer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.AuthRepository
import com.speedreader.trainer.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val email: String = "",
    val baselineWpm: Int = 0,
    val isSignedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            _uiState.value = SettingsUiState(
                email = profile?.email ?: "",
                baselineWpm = profile?.baselineWpm ?: 0
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = _uiState.value.copy(isSignedOut = true)
    }
}

