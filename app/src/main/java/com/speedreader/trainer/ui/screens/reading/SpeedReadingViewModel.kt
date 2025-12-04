package com.speedreader.trainer.ui.screens.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.DocumentRepository
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpeedReadingUiState(
    val documentId: String = "",
    val documentTitle: String = "",
    val words: List<String> = emptyList(),
    val currentWordIndex: Int = 0,
    val currentWord: String = "",
    val totalWords: Int = 0,
    val wpm: Int = 250,
    val fontSize: Int = 48,
    val isReading: Boolean = false,
    val isLoading: Boolean = true,
    val isDarkMode: Boolean = false,
    val progress: Float = 0f,
    val estimatedMinutes: Int = 0,
    val elapsedSeconds: Int = 0,
    val sessionId: String? = null,
    val error: String? = null
)

@HiltViewModel
class SpeedReadingViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val sessionRepository: ReadingSessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedReadingUiState())
    val uiState: StateFlow<SpeedReadingUiState> = _uiState.asStateFlow()

    private var readingJob: Job? = null
    private var timerJob: Job? = null
    private var textContent: String = ""

    init {
        loadUserWpm()
    }

    private fun loadUserWpm() {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile()
            profile?.let {
                if (it.baselineWpm > 0) {
                    _uiState.value = _uiState.value.copy(wpm = it.baselineWpm)
                }
            }
        }
    }

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, documentId = documentId)
            
            val document = documentRepository.getDocument(documentId)
            
            if (document != null) {
                textContent = document.content
                val words = document.content
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                
                val estimatedMinutes = (words.size / _uiState.value.wpm.toFloat()).let {
                    if (it < 1) 1 else it.toInt()
                }

                _uiState.value = _uiState.value.copy(
                    documentTitle = document.title,
                    words = words,
                    totalWords = words.size,
                    currentWord = if (words.isNotEmpty()) words[0] else "",
                    estimatedMinutes = estimatedMinutes,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Document not found"
                )
            }
        }
    }

    fun startReading() {
        if (_uiState.value.words.isEmpty()) return
        
        _uiState.value = _uiState.value.copy(isReading = true)
        
        // Start timer
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    elapsedSeconds = _uiState.value.elapsedSeconds + 1
                )
            }
        }
        
        // Start word display
        readingJob = viewModelScope.launch {
            val delayMs = (60000.0 / _uiState.value.wpm).toLong()
            
            while (_uiState.value.currentWordIndex < _uiState.value.totalWords && _uiState.value.isReading) {
                delay(delayMs)
                
                val nextIndex = _uiState.value.currentWordIndex + 1
                if (nextIndex < _uiState.value.totalWords) {
                    _uiState.value = _uiState.value.copy(
                        currentWordIndex = nextIndex,
                        currentWord = _uiState.value.words[nextIndex],
                        progress = nextIndex.toFloat() / _uiState.value.totalWords
                    )
                } else {
                    // Finished reading
                    finishReading()
                    break
                }
            }
        }
    }

    fun pauseReading() {
        _uiState.value = _uiState.value.copy(isReading = false)
        readingJob?.cancel()
        timerJob?.cancel()
    }

    fun stopReading() {
        _uiState.value = _uiState.value.copy(isReading = false)
        readingJob?.cancel()
        timerJob?.cancel()
    }

    fun restart() {
        pauseReading()
        _uiState.value = _uiState.value.copy(
            currentWordIndex = 0,
            currentWord = if (_uiState.value.words.isNotEmpty()) _uiState.value.words[0] else "",
            progress = 0f,
            elapsedSeconds = 0
        )
    }

    fun skipBackward() {
        val newIndex = (_uiState.value.currentWordIndex - 10).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            currentWordIndex = newIndex,
            currentWord = _uiState.value.words.getOrElse(newIndex) { "" },
            progress = newIndex.toFloat() / _uiState.value.totalWords
        )
    }

    fun skipForward() {
        val newIndex = (_uiState.value.currentWordIndex + 10).coerceAtMost(_uiState.value.totalWords - 1)
        _uiState.value = _uiState.value.copy(
            currentWordIndex = newIndex,
            currentWord = _uiState.value.words.getOrElse(newIndex) { "" },
            progress = newIndex.toFloat() / _uiState.value.totalWords
        )
    }

    fun setWpm(newWpm: Int) {
        val wasReading = _uiState.value.isReading
        if (wasReading) {
            pauseReading()
        }
        
        val estimatedMinutes = (_uiState.value.totalWords / newWpm.toFloat()).let {
            if (it < 1) 1 else it.toInt()
        }
        
        _uiState.value = _uiState.value.copy(
            wpm = newWpm,
            estimatedMinutes = estimatedMinutes
        )
        
        if (wasReading) {
            startReading()
        }
    }

    fun setFontSize(size: Int) {
        _uiState.value = _uiState.value.copy(fontSize = size)
    }

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(isDarkMode = !_uiState.value.isDarkMode)
    }

    fun finishReading() {
        pauseReading()
        
        viewModelScope.launch {
            val state = _uiState.value
            
            // Create session
            val result = sessionRepository.createPendingSession(
                documentId = state.documentId,
                documentTitle = state.documentTitle,
                textContent = textContent,
                wpmUsed = state.wpm,
                wordsRead = state.currentWordIndex + 1,
                durationSeconds = state.elapsedSeconds
            )

            result.fold(
                onSuccess = { sessionId ->
                    _uiState.value = state.copy(sessionId = sessionId)
                },
                onFailure = { e ->
                    _uiState.value = state.copy(error = e.message)
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        readingJob?.cancel()
        timerJob?.cancel()
    }
}

