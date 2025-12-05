package com.speedreader.trainer.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<SplashAuthState>(SplashAuthState.Loading)
    val authState: StateFlow<SplashAuthState> = _authState.asStateFlow()

    fun checkAuthState() {
        viewModelScope.launch {
            val user = authRepository.currentUser
            if (user == null) {
                _authState.value = SplashAuthState.NotAuthenticated
            } else {
                val hasBaseline = authRepository.hasCompletedBaseline()
                _authState.value = if (hasBaseline) {
                    SplashAuthState.Authenticated
                } else {
                    SplashAuthState.NeedsBaseline
                }
            }
        }
    }
}

