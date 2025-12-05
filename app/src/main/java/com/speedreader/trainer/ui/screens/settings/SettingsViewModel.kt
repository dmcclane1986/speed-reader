package com.speedreader.trainer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.AuthRepository
import com.speedreader.trainer.data.repository.SettingsRepository
import com.speedreader.trainer.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val displayName: String = "",
    val email: String = "",
    val baselineWpm: Int = 0,
    val darkModeEnabled: Boolean = false,
    val readingDarkModeEnabled: Boolean = false,
    val defaultWpm: Int = 250,
    val defaultFontSize: Int = 48,
    val chunkingEnabled: Boolean = false,
    val defaultChunkSize: Int = 2,
    val isSignedOut: Boolean = false,
    val isSavingName: Boolean = false,
    val nameSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        observeSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            _uiState.value = _uiState.value.copy(
                displayName = profile?.displayName ?: "",
                email = profile?.email ?: "",
                baselineWpm = profile?.baselineWpm ?: 0
            )
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.darkModeFlow.collect { darkMode ->
                _uiState.value = _uiState.value.copy(darkModeEnabled = darkMode)
            }
        }
        viewModelScope.launch {
            settingsRepository.readingDarkModeFlow.collect { darkMode ->
                _uiState.value = _uiState.value.copy(readingDarkModeEnabled = darkMode)
            }
        }
        viewModelScope.launch {
            settingsRepository.defaultWpmFlow.collect { wpm ->
                _uiState.value = _uiState.value.copy(defaultWpm = wpm)
            }
        }
        viewModelScope.launch {
            settingsRepository.defaultFontSizeFlow.collect { fontSize ->
                _uiState.value = _uiState.value.copy(defaultFontSize = fontSize)
            }
        }
        viewModelScope.launch {
            settingsRepository.chunkingEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(chunkingEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.defaultChunkSizeFlow.collect { chunkSize ->
                _uiState.value = _uiState.value.copy(defaultChunkSize = chunkSize)
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    fun setReadingDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReadingDarkMode(enabled)
        }
    }

    fun setDefaultWpm(wpm: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultWpm(wpm)
        }
    }

    fun setDefaultFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultFontSize(size)
        }
    }

    fun setChunkingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setChunkingEnabled(enabled)
        }
    }

    fun setDefaultChunkSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultChunkSize(size)
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingName = true, nameSaved = false)
            val result = userRepository.updateDisplayName(name)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        displayName = name,
                        isSavingName = false,
                        nameSaved = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isSavingName = false)
                }
            )
        }
    }

    fun clearNameSavedFlag() {
        _uiState.value = _uiState.value.copy(nameSaved = false)
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = _uiState.value.copy(isSignedOut = true)
    }
}

