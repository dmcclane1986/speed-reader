package com.speedreader.trainer.domain.model

import com.google.firebase.Timestamp

data class UserDocument(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val wordCount: Int = 0,
    val fileType: String = "", // "pdf", "txt", "md"
    val uploadedAt: Timestamp = Timestamp.now()
)

