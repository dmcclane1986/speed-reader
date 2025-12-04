package com.speedreader.trainer.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.speedreader.trainer.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    fun getUserProfileFlow(): Flow<UserProfile?> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val profile = snapshot?.toObject(UserProfile::class.java)
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserProfile(): UserProfile? {
        val userId = currentUserId ?: return null
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateBaselineResults(wpm: Int, comprehension: Float): Result<Unit> {
        val userId = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "baselineWpm" to wpm,
                        "baselineComprehension" to comprehension,
                        "hasCompletedBaseline" to true
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSessionStats(
        durationSeconds: Int,
        comprehensionScore: Float
    ): Result<Unit> {
        val userId = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val profile = getUserProfile() ?: return Result.failure(Exception("Profile not found"))
            
            // Calculate streak
            val today = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.time

            val lastPractice = profile.lastPracticeDate?.toDate()
            val newStreak = if (lastPractice != null) {
                val yesterday = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -1)
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time
                
                when {
                    lastPractice >= today -> profile.currentStreak // Already practiced today
                    lastPractice >= yesterday -> profile.currentStreak + 1 // Continuing streak
                    else -> 1 // Streak broken, start new
                }
            } else {
                1 // First practice
            }

            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "totalReadingTimeSeconds" to profile.totalReadingTimeSeconds + durationSeconds,
                        "sessionsCompleted" to profile.sessionsCompleted + 1,
                        "currentStreak" to newStreak,
                        "lastPracticeDate" to Timestamp.now()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

