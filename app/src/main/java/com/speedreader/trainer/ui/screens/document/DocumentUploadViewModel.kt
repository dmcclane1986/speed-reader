package com.speedreader.trainer.ui.screens.document

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speedreader.trainer.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentUploadUiState(
    val selectedUri: Uri? = null,
    val fileName: String = "",
    val title: String = "",
    val fileType: String = "",
    val isUploading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class DocumentUploadViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUploadUiState())
    val uiState: StateFlow<DocumentUploadUiState> = _uiState.asStateFlow()

    fun setSelectedFile(uri: Uri, fileName: String) {
        val fileType = when {
            fileName.endsWith(".pdf", ignoreCase = true) -> "pdf"
            fileName.endsWith(".txt", ignoreCase = true) -> "txt"
            fileName.endsWith(".md", ignoreCase = true) -> "md"
            else -> ""
        }
        
        val title = fileName.substringBeforeLast(".")
        
        _uiState.value = _uiState.value.copy(
            selectedUri = uri,
            fileName = fileName,
            title = title,
            fileType = fileType,
            error = if (fileType.isEmpty()) "Unsupported file type" else null
        )
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun uploadDocument() {
        val state = _uiState.value
        val uri = state.selectedUri ?: return
        
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Please enter a title")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isUploading = true, error = null)
            
            val result = documentRepository.uploadDocument(
                uri = uri,
                title = state.title,
                fileType = state.fileType
            )
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        isSuccess = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        error = exception.message ?: "Upload failed"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

