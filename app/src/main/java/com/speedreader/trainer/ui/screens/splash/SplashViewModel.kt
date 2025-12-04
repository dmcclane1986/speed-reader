package com.speedreader.trainer.ui.screens.splash

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

sealed class SplashAuthState {
    object Loading : SplashAuthState()
    object NotLoggedIn : SplashAuthState()
    object NeedsBaseline : SplashAuthState()
    object Ready : SplashAuthState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<SplashAuthState>(SplashAuthState.Loading)
    val authState: StateFlow<SplashAuthState> = _authState.asStateFlow()

    fun checkAuthState() {
        viewModelScope.launch {
            if (!authRepository.isLoggedIn) {
                _authState.value = SplashAuthState.NotLoggedIn
                return@launch
            }

            val profile = userRepository.getUserProfile()
            _authState.value = when {
                profile == null -> SplashAuthState.NotLoggedIn
                !profile.hasCompletedBaseline -> SplashAuthState.NeedsBaseline
                else -> SplashAuthState.Ready
            }
        }
    }
}

