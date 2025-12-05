package com.speedreader.trainer.ui.screens.reading

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.DocumentRepository
import com.speedreader.trainer.data.repository.ReadingSessionRepository
import com.speedreader.trainer.data.repository.SettingsRepository
import com.speedreader.trainer.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SpeedReadingUiState(
    val documentTitle: String = "",
    val words: List<String> = emptyList(),
    val currentWordIndex: Int = 0,
    val wpm: Int = 250,
    val fontSize: Int = 48,
    val chunkSize: Int = 1,
    val chunkingEnabled: Boolean = false,
    val isPlaying: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val sessionId: String? = null,
    val readingDarkModeEnabled: Boolean = false,
    val sessionStartIndex: Int = 0,
    val shouldShowQuiz: Boolean = true,
    val showContinueDialog: Boolean = false,
    val savedProgress: Int = 0
) {
    val progress: Float
        get() = if (words.isNotEmpty()) currentWordIndex.toFloat() / words.size else 0f
    
    val currentChunk: String
        get() {
            if (words.isEmpty()) return ""
            val effectiveChunkSize = if (chunkingEnabled) chunkSize else 1
            val endIndex = minOf(currentWordIndex + effectiveChunkSize, words.size)
            return words.subList(currentWordIndex, endIndex).joinToString(" ")
        }

    val wordsReadInSession: Int
        get() = currentWordIndex - sessionStartIndex

    companion object {
        const val MIN_WORDS_FOR_QUIZ = 300
    }
}

@HiltViewModel
class SpeedReadingViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val sessionRepository: ReadingSessionRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedReadingUiState())
    val uiState: StateFlow<SpeedReadingUiState> = _uiState.asStateFlow()

    private var readingJob: Job? = null
    private var documentId: String = ""
    private var startTime: Long = 0

    fun loadDocument(docId: String) {
        documentId = docId
        viewModelScope.launch {
            loadDefaultSettings()
            
            val document = documentRepository.getDocument(docId)
            if (document != null) {
                val words = document.content
                    .split("\\s+".toRegex())
                    .filter { it.isNotBlank() }
                
                _uiState.value = _uiState.value.copy(
                    documentTitle = document.title,
                    words = words,
                    isLoading = false,
                    savedProgress = document.lastReadWordIndex,
                    showContinueDialog = document.hasProgress
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Document not found"
                )
            }
        }
    }

    private suspend fun loadDefaultSettings() {
        val profile = userRepository.getUserProfile()
        val baselineWpm = profile?.baselineWpm ?: 250
        
        val savedWpm = settingsRepository.defaultWpmFlow.first()
        val savedFontSize = settingsRepository.defaultFontSizeFlow.first()
        val savedChunkSize = settingsRepository.defaultChunkSizeFlow.first()
        val chunkingEnabled = settingsRepository.chunkingEnabledFlow.first()
        val readingDarkMode = settingsRepository.readingDarkModeFlow.first()
        
        // Use saved WPM if user has adjusted it (not default 250), otherwise use baseline
        val effectiveWpm = if (savedWpm != 250) savedWpm else baselineWpm
        
        Log.d("SpeedReading", "Loading settings: savedWpm=$savedWpm, baselineWpm=$baselineWpm, effectiveWpm=$effectiveWpm")
        
        _uiState.value = _uiState.value.copy(
            wpm = effectiveWpm,
            fontSize = savedFontSize,
            chunkSize = savedChunkSize,
            chunkingEnabled = chunkingEnabled,
            readingDarkModeEnabled = readingDarkMode
        )
    }

    fun continueFromSaved() {
        _uiState.value = _uiState.value.copy(
            currentWordIndex = _uiState.value.savedProgress,
            sessionStartIndex = _uiState.value.savedProgress,
            showContinueDialog = false
        )
    }

    fun startFromBeginning() {
        viewModelScope.launch {
            documentRepository.resetReadingProgress(documentId)
            _uiState.value = _uiState.value.copy(
                currentWordIndex = 0,
                sessionStartIndex = 0,
                showContinueDialog = false
            )
        }
    }

    fun play() {
        if (_uiState.value.isFinished) return
        
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }
        
        _uiState.value = _uiState.value.copy(isPlaying = true)
        
        readingJob = viewModelScope.launch {
            while (_uiState.value.isPlaying && _uiState.value.currentWordIndex < _uiState.value.words.size) {
                val delayMs = (60000.0 / _uiState.value.wpm).toLong()
                delay(delayMs)
                
                val effectiveChunkSize = if (_uiState.value.chunkingEnabled) _uiState.value.chunkSize else 1
                val newIndex = _uiState.value.currentWordIndex + effectiveChunkSize
                
                if (newIndex >= _uiState.value.words.size) {
                    _uiState.value = _uiState.value.copy(
                        currentWordIndex = _uiState.value.words.size,
                        isPlaying = false,
                        isFinished = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(currentWordIndex = newIndex)
                }
            }
        }
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
        readingJob?.cancel()
        
        // Save progress
        viewModelScope.launch {
            documentRepository.updateReadingProgress(documentId, _uiState.value.currentWordIndex)
        }
    }

    fun setWpm(wpm: Int) {
        val newWpm = wpm.coerceIn(100, 1000)
        _uiState.value = _uiState.value.copy(wpm = newWpm)
        // Save to settings
        viewModelScope.launch {
            settingsRepository.setDefaultWpm(newWpm)
        }
    }

    fun setFontSize(size: Int) {
        val newSize = size.coerceIn(24, 72)
        _uiState.value = _uiState.value.copy(fontSize = newSize)
        // Save to settings
        viewModelScope.launch {
            settingsRepository.setDefaultFontSize(newSize)
        }
    }

    fun setChunkSize(size: Int) {
        val newSize = size.coerceIn(1, 5)
        _uiState.value = _uiState.value.copy(chunkSize = newSize)
        // Save to settings
        viewModelScope.launch {
            settingsRepository.setDefaultChunkSize(newSize)
        }
    }

    fun setChunkingEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(chunkingEnabled = enabled)
        // Save to settings
        viewModelScope.launch {
            settingsRepository.setChunkingEnabled(enabled)
        }
    }

    fun goBackOneWord() {
        val effectiveChunkSize = if (_uiState.value.chunkingEnabled) _uiState.value.chunkSize else 1
        val newIndex = (_uiState.value.currentWordIndex - effectiveChunkSize).coerceAtLeast(_uiState.value.sessionStartIndex)
        _uiState.value = _uiState.value.copy(currentWordIndex = newIndex)
    }

    fun goBackToSentenceStart() {
        val words = _uiState.value.words
        val currentIndex = _uiState.value.currentWordIndex
        val sessionStart = _uiState.value.sessionStartIndex
        
        if (currentIndex <= sessionStart || words.isEmpty()) return
        
        // Look backwards from current position to find sentence start
        // A sentence typically ends with . ! or ? followed by a word starting with uppercase
        var sentenceStart = currentIndex
        
        for (i in (currentIndex - 1) downTo sessionStart) {
            val word = words.getOrNull(i) ?: continue
            // Check if this word ends a sentence (ends with . ! or ?)
            if (word.endsWith(".") || word.endsWith("!") || word.endsWith("?")) {
                // The next word (i + 1) is the start of the current sentence
                sentenceStart = i + 1
                break
            }
            sentenceStart = i
        }
        
        // If we're already at sentence start, go to previous sentence
        if (sentenceStart == currentIndex && currentIndex > sessionStart) {
            for (i in (currentIndex - 2) downTo sessionStart) {
                val word = words.getOrNull(i) ?: continue
                if (word.endsWith(".") || word.endsWith("!") || word.endsWith("?")) {
                    sentenceStart = i + 1
                    break
                }
                sentenceStart = i
            }
        }
        
        _uiState.value = _uiState.value.copy(currentWordIndex = sentenceStart.coerceAtLeast(sessionStart))
    }

    fun skipForwardOneWord() {
        val effectiveChunkSize = if (_uiState.value.chunkingEnabled) _uiState.value.chunkSize else 1
        val newIndex = (_uiState.value.currentWordIndex + effectiveChunkSize).coerceAtMost(_uiState.value.words.size - 1)
        _uiState.value = _uiState.value.copy(currentWordIndex = newIndex)
    }

    fun finishReading(): Pair<String, Boolean> {
        pause()
        
        val endTime = System.currentTimeMillis()
        // If never played (startTime is 0), duration is 0
        val durationSeconds = if (startTime == 0L) 0 else ((endTime - startTime) / 1000).toInt().coerceAtLeast(0)
        val wordsRead = _uiState.value.wordsReadInSession
        
        val shouldShowQuiz = wordsRead >= SpeedReadingUiState.MIN_WORDS_FOR_QUIZ
        
        _uiState.value = _uiState.value.copy(shouldShowQuiz = shouldShowQuiz)
        
        val sessionId = sessionRepository.createPendingSession(
            documentId = documentId,
            documentTitle = _uiState.value.documentTitle,
            wpmUsed = _uiState.value.wpm,
            wordsRead = wordsRead,
            durationSeconds = durationSeconds
        )
        
        _uiState.value = _uiState.value.copy(sessionId = sessionId)
        
        // Generate questions if showing quiz
        if (shouldShowQuiz) {
            viewModelScope.launch {
                try {
                    val startIdx = _uiState.value.sessionStartIndex
                    val endIdx = _uiState.value.currentWordIndex
                    val textForQuiz = _uiState.value.words.subList(startIdx, endIdx).joinToString(" ")
                    // Use NonCancellable to ensure question generation completes even if ViewModel is cleared
                    withContext(NonCancellable) {
                        sessionRepository.generateComprehensionQuestions(textForQuiz)
                    }
                } catch (e: Exception) {
                    Log.e("SpeedReading", "Failed to generate questions", e)
                    // If question generation fails, still allow navigation but mark session as no quiz
                    withContext(NonCancellable) {
                        sessionRepository.completeSessionWithoutQuiz()
                    }
                }
            }
        } else {
            viewModelScope.launch {
                try {
                    withContext(NonCancellable) {
                        sessionRepository.completeSessionWithoutQuiz()
                        userRepository.updateSessionStats(durationSeconds, 0f)
                    }
                } catch (e: Exception) {
                    Log.e("SpeedReading", "Failed to complete session", e)
                }
            }
        }
        
        return Pair(sessionId, shouldShowQuiz)
    }

    override fun onCleared() {
        super.onCleared()
        readingJob?.cancel()
    }
}

