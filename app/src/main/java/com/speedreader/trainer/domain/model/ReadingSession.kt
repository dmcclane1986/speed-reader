package com.speedreader.trainer.domain.model

import com.google.firebase.Timestamp

data class ReadingSession(
    val id: String = "",
    val userId: String = "",
    val documentId: String = "",
    val documentTitle: String = "",
    val wpmUsed: Int = 0,
    val wordsRead: Int = 0,
    val comprehensionScore: Float = 0f,
    val durationSeconds: Int = 0,
    val completedAt: Timestamp = Timestamp.now()
)

