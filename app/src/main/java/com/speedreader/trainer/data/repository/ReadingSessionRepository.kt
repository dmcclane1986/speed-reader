package com.speedreader.trainer.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.speedreader.trainer.data.remote.ChatCompletionRequest
import com.speedreader.trainer.data.remote.ChatMessage
import com.speedreader.trainer.data.remote.OpenAIService
import com.speedreader.trainer.domain.model.ComprehensionQuestion
import com.speedreader.trainer.domain.model.ReadingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ReadingSessionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val openAIService: OpenAIService
) {
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    // Temporary storage for session data during quiz
    private val pendingSessions = mutableMapOf<String, PendingSession>()

    data class PendingSession(
        val session: ReadingSession,
        val textContent: String,
        val questions: List<ComprehensionQuestion>
    )

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

    suspend fun getSession(sessionId: String): ReadingSession? {
        // Check pending sessions first
        pendingSessions[sessionId]?.let { return it.session }
        
        return try {
            val doc = firestore.collection("sessions").document(sessionId).get().await()
            doc.toObject(ReadingSession::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createPendingSession(
        documentId: String,
        documentTitle: String,
        textContent: String,
        wpmUsed: Int,
        wordsRead: Int,
        durationSeconds: Int
    ): Result<String> {
        val userId = currentUserId ?: return Result.failure(Exception("Not logged in"))
        
        return withContext(Dispatchers.IO) {
            try {
                val sessionId = UUID.randomUUID().toString()
                val session = ReadingSession(
                    id = sessionId,
                    userId = userId,
                    documentId = documentId,
                    documentTitle = documentTitle,
                    wpmUsed = wpmUsed,
                    wordsRead = wordsRead,
                    durationSeconds = durationSeconds,
                    completedAt = Timestamp.now()
                )

                // Generate comprehension questions
                val questions = generateComprehensionQuestions(textContent)

                pendingSessions[sessionId] = PendingSession(
                    session = session,
                    textContent = textContent,
                    questions = questions
                )

                Result.success(sessionId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getPendingQuestions(sessionId: String): List<ComprehensionQuestion> {
        return pendingSessions[sessionId]?.questions ?: emptyList()
    }

    suspend fun completeSession(sessionId: String, comprehensionScore: Float): Result<ReadingSession> {
        val pending = pendingSessions[sessionId]
            ?: return Result.failure(Exception("Session not found"))

        return try {
            val completedSession = pending.session.copy(comprehensionScore = comprehensionScore)
            
            firestore.collection("sessions")
                .document(sessionId)
                .set(completedSession)
                .await()

            pendingSessions.remove(sessionId)
            Result.success(completedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun generateComprehensionQuestions(text: String): List<ComprehensionQuestion> {
        return withContext(Dispatchers.IO) {
            try {
                // Truncate text if too long (API limit considerations)
                val truncatedText = if (text.length > 3000) {
                    text.take(3000) + "..."
                } else {
                    text
                }

                val prompt = """
                    Based on the following text, generate 4 multiple choice comprehension questions.
                    
                    TEXT:
                    $truncatedText
                    
                    Respond ONLY with a JSON array in this exact format (no other text):
                    [
                        {
                            "question": "Question text here?",
                            "options": ["Option A", "Option B", "Option C", "Option D"],
                            "correctAnswerIndex": 0
                        }
                    ]
                    
                    Make sure:
                    - Questions test understanding of main ideas and details
                    - All 4 options are plausible
                    - correctAnswerIndex is 0, 1, 2, or 3
                """.trimIndent()

                val request = ChatCompletionRequest(
                    model = "gpt-3.5-turbo",
                    messages = listOf(
                        ChatMessage(role = "system", content = "You are a reading comprehension quiz generator. Always respond with valid JSON only."),
                        ChatMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.7,
                    max_tokens = 1000
                )

                val response = openAIService.generateCompletion(request)
                val jsonContent = response.choices.firstOrNull()?.message?.content ?: "[]"
                
                parseQuestionsFromJson(jsonContent)
            } catch (e: Exception) {
                // Return fallback questions if API fails
                getFallbackQuestions()
            }
        }
    }

    private fun parseQuestionsFromJson(json: String): List<ComprehensionQuestion> {
        return try {
            // Clean the JSON string
            val cleanJson = json.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val gson = Gson()
            val questionsList = gson.fromJson(cleanJson, Array<QuestionJson>::class.java)
            
            questionsList.mapIndexed { index, q ->
                ComprehensionQuestion(
                    id = "q_$index",
                    question = q.question,
                    options = q.options,
                    correctAnswerIndex = q.correctAnswerIndex
                )
            }
        } catch (e: Exception) {
            getFallbackQuestions()
        }
    }

    private data class QuestionJson(
        val question: String,
        val options: List<String>,
        val correctAnswerIndex: Int
    )

    private fun getFallbackQuestions(): List<ComprehensionQuestion> {
        return listOf(
            ComprehensionQuestion(
                id = "fallback_1",
                question = "How well do you think you understood the main idea of the text?",
                options = listOf(
                    "Very well - I could explain it to someone else",
                    "Fairly well - I got the general idea",
                    "Somewhat - I understood parts of it",
                    "Not well - I struggled to follow"
                ),
                correctAnswerIndex = 0
            ),
            ComprehensionQuestion(
                id = "fallback_2",
                question = "How many key details can you recall from the passage?",
                options = listOf(
                    "Several specific details",
                    "A few details",
                    "Only one or two",
                    "None specifically"
                ),
                correctAnswerIndex = 0
            ),
            ComprehensionQuestion(
                id = "fallback_3",
                question = "If you had to summarize the text, how confident would you be?",
                options = listOf(
                    "Very confident",
                    "Somewhat confident",
                    "Not very confident",
                    "Not confident at all"
                ),
                correctAnswerIndex = 0
            )
        )
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
}

