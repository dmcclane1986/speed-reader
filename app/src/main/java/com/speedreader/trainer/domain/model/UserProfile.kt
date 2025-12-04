package com.speedreader.trainer.domain.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val baselineWpm: Int = 0,
    val baselineComprehension: Float = 0f,
    val hasCompletedBaseline: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val totalReadingTimeSeconds: Long = 0,
    val sessionsCompleted: Int = 0,
    val currentStreak: Int = 0,
    val lastPracticeDate: Timestamp? = null
)

