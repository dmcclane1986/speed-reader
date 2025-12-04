package com.speedreader.trainer.ui.screens.auth

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

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val hasCompletedBaseline: Boolean) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.signInWithEmail(email, password)
            
            result.fold(
                onSuccess = {
                    val profile = userRepository.getUserProfile()
                    _uiState.value = AuthUiState.Success(
                        hasCompletedBaseline = profile?.hasCompletedBaseline == true
                    )
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(
                        e.message ?: "Sign in failed"
                    )
                }
            )
        }
    }

    fun registerWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.registerWithEmail(email, password, displayName)
            
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState.Success(hasCompletedBaseline = false)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(
                        e.message ?: "Registration failed"
                    )
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.signInWithGoogle(idToken)
            
            result.fold(
                onSuccess = {
                    val profile = userRepository.getUserProfile()
                    _uiState.value = AuthUiState.Success(
                        hasCompletedBaseline = profile?.hasCompletedBaseline == true
                    )
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState.Error(
                        e.message ?: "Google sign in failed"
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

