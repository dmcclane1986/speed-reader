package com.speedreader.trainer.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.speedreader.trainer.domain.model.UserDocument
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun getDocumentsFlow(): Flow<List<UserDocument>> = callbackFlow {
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

    suspend fun uploadDocument(uri: Uri, title: String, fileType: String): Result<UserDocument> {
        val userId = currentUserId ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            val content = extractText(uri, fileType)
            val wordCount = content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            
            val document = UserDocument(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                content = content,
                wordCount = wordCount,
                fileType = fileType,
                uploadedAt = Timestamp.now()
            )
            
            firestore.collection("documents")
                .document(document.id)
                .set(document)
                .await()
            
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractText(uri: Uri, fileType: String): String {
        return when (fileType.lowercase()) {
            "pdf" -> extractPdfText(uri)
            "txt", "md" -> extractPlainText(uri)
            else -> throw IllegalArgumentException("Unsupported file type: $fileType")
        }
    }

    private fun extractPdfText(uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                val stripper = PDFTextStripper()
                return stripper.getText(document)
            }
        }
        throw Exception("Failed to read PDF file")
    }

    private fun extractPlainText(uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            return reader.readText()
        }
        throw Exception("Failed to read text file")
    }

    suspend fun deleteDocument(documentId: String): Result<Unit> {
        return try {
            firestore.collection("documents")
                .document(documentId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReadingProgress(documentId: String, wordIndex: Int): Result<Unit> {
        return try {
            firestore.collection("documents")
                .document(documentId)
                .update(
                    mapOf(
                        "lastReadWordIndex" to wordIndex,
                        "lastReadAt" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetReadingProgress(documentId: String): Result<Unit> {
        return try {
            firestore.collection("documents")
                .document(documentId)
                .update(
                    mapOf(
                        "lastReadWordIndex" to 0,
                        "lastReadAt" to null
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

