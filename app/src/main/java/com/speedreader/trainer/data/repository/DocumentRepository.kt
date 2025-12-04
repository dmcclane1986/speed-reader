package com.speedreader.trainer.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.speedreader.trainer.domain.model.UserDocument
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import javax.inject.Inject

class DocumentRepository @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    fun getUserDocumentsFlow(): Flow<List<UserDocument>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("documents")
            .whereEqualTo("userId", userId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val documents = snapshot?.documents?.mapNotNull {
                    it.toObject(UserDocument::class.java)
                } ?: emptyList()
                trySend(documents)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getDocument(documentId: String): UserDocument? {
        return try {
            val doc = firestore.collection("documents").document(documentId).get().await()
            doc.toObject(UserDocument::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadDocument(uri: Uri, title: String): Result<UserDocument> {
        val userId = currentUserId ?: return Result.failure(Exception("Not logged in"))
        
        return withContext(Dispatchers.IO) {
            try {
                val fileType = getFileType(uri)
                val content = extractText(uri, fileType)
                val wordCount = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size

                val documentId = UUID.randomUUID().toString()
                val document = UserDocument(
                    id = documentId,
                    userId = userId,
                    title = title.ifBlank { getFileName(uri) },
                    content = content,
                    wordCount = wordCount,
                    fileType = fileType,
                    uploadedAt = Timestamp.now()
                )

                firestore.collection("documents")
                    .document(documentId)
                    .set(document)
                    .await()

                Result.success(document)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteDocument(documentId: String): Result<Unit> {
        return try {
            firestore.collection("documents").document(documentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileType(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("pdf") == true -> "pdf"
            mimeType?.contains("markdown") == true -> "md"
            mimeType?.contains("text") == true -> "txt"
            uri.path?.endsWith(".md") == true -> "md"
            uri.path?.endsWith(".pdf") == true -> "pdf"
            else -> "txt"
        }
    }

    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment?.substringAfterLast("/") ?: "Untitled Document"
    }

    private fun extractText(uri: Uri, fileType: String): String {
        return when (fileType) {
            "pdf" -> extractPdfText(uri)
            else -> extractPlainText(uri)
        }
    }

    private fun extractPdfText(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                PDFTextStripper().getText(document)
            }
        } ?: ""
    }

    private fun extractPlainText(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: ""
    }
}

