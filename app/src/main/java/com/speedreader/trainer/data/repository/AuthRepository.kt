package com.speedreader.trainer.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.speedreader.trainer.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isLoggedIn: Boolean
        get() = currentUser != null

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Create user profile in Firestore
                val profile = UserProfile(
                    uid = user.uid,
                    email = email,
                    displayName = displayName,
                    createdAt = Timestamp.now()
                )
                firestore.collection("users")
                    .document(user.uid)
                    .set(profile)
                    .await()
                Result.success(user)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let { user ->
                // Check if user profile exists, create if not
                val doc = firestore.collection("users").document(user.uid).get().await()
                if (!doc.exists()) {
                    val profile = UserProfile(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "",
                        createdAt = Timestamp.now()
                    )
                    firestore.collection("users")
                        .document(user.uid)
                        .set(profile)
                        .await()
                }
                Result.success(user)
            } ?: Result.failure(Exception("Google sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

