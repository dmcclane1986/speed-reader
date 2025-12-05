package com.speedreader.trainer.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.speedreader.trainer.data.remote.ChatCompletionRequest
import com.speedreader.trainer.data.remote.ChatMessage
import com.speedreader.trainer.data.remote.OpenAIService
import com.speedreader.trainer.domain.model.ComprehensionQuestion
import com.speedreader.trainer.domain.model.ReadingSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ReadingSessionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val openAIService: OpenAIService
) {
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    // Temporary storage for pending sessions and questions
    private var pendingSession: ReadingSession? = null
    private var pendingQuestions: List<ComprehensionQuestion>? = null
    private var isGeneratingQuestions: Boolean = false

    fun getSessionsFlow(): Flow<List<ReadingSession>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("sessions")
            .whereEqualTo("userId", userId)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val sessions = snapshot?.documents?.mapNotNull {
                    it.toObject(ReadingSession::class.java)
                } ?: emptyList()
                trySend(sessions)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getRecentSessions(limit: Int = 10): List<ReadingSession> {
        val userId = currentUserId ?: return emptyList()
        return try {
            val snapshot = firestore.collection("sessions")
                .whereEqualTo("userId", userId)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(ReadingSession::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllSessionsCount(): Int {
        val userId = currentUserId ?: return 0
        return try {
            val snapshot = firestore.collection("sessions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            firestore.collection("sessions")
                .document(sessionId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestComprehensionScore(): Float? {
        val userId = currentUserId ?: return null
        return try {
            val snapshot = firestore.collection("sessions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hasQuiz", true)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()
                ?.toObject(ReadingSession::class.java)
                ?.comprehensionScore
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAverageComprehensionScore(): Float? {
        val userId = currentUserId ?: return null
        return try {
            val snapshot = firestore.collection("sessions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hasQuiz", true)
                .get()
                .await()
            val sessions = snapshot.documents.mapNotNull { it.toObject(ReadingSession::class.java) }
            if (sessions.isEmpty()) null
            else sessions.map { it.comprehensionScore }.average().toFloat()
        } catch (e: Exception) {
            null
        }
    }

    fun createPendingSession(
        documentId: String,
        documentTitle: String,
        wpmUsed: Int,
        wordsRead: Int,
        durationSeconds: Int
    ): String {
        val sessionId = UUID.randomUUID().toString()
        pendingSession = ReadingSession(
            id = sessionId,
            userId = currentUserId ?: "",
            documentId = documentId,
            documentTitle = documentTitle,
            wpmUsed = wpmUsed,
            wordsRead = wordsRead,
            durationSeconds = durationSeconds,
            completedAt = Timestamp.now()
        )
        return sessionId
    }

    fun getPendingQuestions(): List<ComprehensionQuestion>? = pendingQuestions
    
    suspend fun waitForQuestions(timeoutMs: Long = 30000): List<ComprehensionQuestion>? {
        val startTime = System.currentTimeMillis()
        while (pendingQuestions == null && isGeneratingQuestions) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                Log.w("ReadingSession", "Timeout waiting for questions")
                return null
            }
            delay(200) // Check every 200ms
        }
        return pendingQuestions
    }

    suspend fun completeSession(
        comprehensionScore: Float
    ): Result<ReadingSession> {
        val session = pendingSession ?: return Result.failure(Exception("No pending session"))
        currentUserId ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            val completedSession = session.copy(
                comprehensionScore = comprehensionScore,
                hasQuiz = true
            )
            
            firestore.collection("sessions")
                .document(completedSession.id)
                .set(completedSession)
                .await()
            
            // Clear pending data
            pendingSession = null
            pendingQuestions = null
            
            Result.success(completedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeSessionWithoutQuiz(): Result<ReadingSession> {
        val session = pendingSession ?: return Result.failure(Exception("No pending session"))
        currentUserId ?: return Result.failure(Exception("Not logged in"))
        
        return try {
            val completedSession = session.copy(
                comprehensionScore = 0f,
                hasQuiz = false
            )
            
            firestore.collection("sessions")
                .document(completedSession.id)
                .set(completedSession)
                .await()
            
            // Clear pending data
            pendingSession = null
            pendingQuestions = null
            
            Result.success(completedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateComprehensionQuestions(text: String): List<ComprehensionQuestion> {
        isGeneratingQuestions = true
        pendingQuestions = null // Clear previous questions
        
        Log.d("ReadingSession", "Generating questions for text of length: ${text.length}")
        
        // Calculate number of questions based on text length
        val wordCount = text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val questionCount = when {
            wordCount < 150 -> 3
            wordCount < 300 -> 4
            else -> 5
        }
        
        val truncatedText = if (text.length > 3000) text.take(3000) + "..." else text
        
        val prompt = """
            Based on this text passage, generate exactly $questionCount multiple-choice comprehension questions.
            The questions should test understanding of specific facts, details, and concepts from the text.
            Do NOT generate generic questions about reading ability or memory - focus only on the content.
            
            TEXT:
            $truncatedText
            
            Generate questions in this exact JSON format (no markdown, just raw JSON):
            [
              {
                "question": "specific question about the text content?",
                "options": ["option A", "option B", "option C", "option D"],
                "correctIndex": 0
              }
            ]
            
            Important:
            - Questions must be about specific information IN the text
            - Each question must have exactly 4 options
            - correctIndex is 0-3 indicating which option is correct
            - Generate exactly $questionCount questions
        """.trimIndent()

        return try {
            Log.d("ReadingSession", "Calling OpenAI API...")
            val response = openAIService.generateCompletion(
                ChatCompletionRequest(
                    messages = listOf(
                        ChatMessage(
                            role = "system",
                            content = "You are a reading comprehension quiz generator. Generate questions that test understanding of specific facts and details from the given text. Always respond with valid JSON only."
                        ),
                        ChatMessage(
                            role = "user",
                            content = prompt
                        )
                    ),
                    temperature = 0.7,
                    max_tokens = 1500
                )
            )

            val content = response.choices.firstOrNull()?.message?.content ?: "[]"
            Log.d("ReadingSession", "OpenAI response: $content")
            
            val questions = parseQuestions(content)
            Log.d("ReadingSession", "Parsed ${questions.size} questions")
            
            // Store for later retrieval
            pendingQuestions = questions
            isGeneratingQuestions = false
            
            questions
        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.w("ReadingSession", "Question generation was cancelled", e)
            isGeneratingQuestions = false
            // Re-throw cancellation to respect coroutine cancellation
            throw e
        } catch (e: Exception) {
            Log.e("ReadingSession", "Failed to generate questions", e)
            isGeneratingQuestions = false
            // Return empty list on other errors, but log them
            emptyList()
        }
    }

    private fun parseQuestions(jsonString: String): List<ComprehensionQuestion> {
        return try {
            // Clean up the response
            val cleanJson = jsonString
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val gson = com.google.gson.Gson()
            val listType = object : com.google.gson.reflect.TypeToken<List<QuestionDto>>() {}.type
            val questions: List<QuestionDto> = gson.fromJson(cleanJson, listType)
            
            questions.map { dto ->
                ComprehensionQuestion(
                    id = "q_${UUID.randomUUID()}",
                    question = dto.question,
                    options = dto.options.take(4), // Ensure max 4 options
                    correctAnswerIndex = dto.correctIndex.coerceIn(0, dto.options.size - 1)
                )
            }.take(5) // Ensure max 5 questions
        } catch (e: Exception) {
            Log.e("ReadingSession", "Failed to parse questions: $jsonString", e)
            emptyList()
        }
    }

    private data class QuestionDto(
        val question: String,
        val options: List<String>,
        val correctIndex: Int
    )
}

