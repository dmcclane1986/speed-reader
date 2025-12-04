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
    val selectedFileName: String = "",
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DocumentUploadViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUploadUiState())
    val uiState: StateFlow<DocumentUploadUiState> = _uiState.asStateFlow()

    fun selectFile(uri: Uri) {
        val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Selected file"
        _uiState.value = _uiState.value.copy(
            selectedUri = uri,
            selectedFileName = fileName,
            error = null
        )
    }

    fun uploadDocument(customTitle: String) {
        val uri = _uiState.value.selectedUri ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, error = null)

            val result = documentRepository.uploadDocument(
                uri = uri,
                title = customTitle.ifBlank { _uiState.value.selectedFileName }
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadSuccess = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        error = e.message ?: "Upload failed"
                    )
                }
            )
        }
    }
}

