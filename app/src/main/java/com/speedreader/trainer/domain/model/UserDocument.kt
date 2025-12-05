package com.speedreader.trainer.domain.model

import com.google.firebase.Timestamp

data class UserDocument(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val wordCount: Int = 0,
    val fileType: String = "",
    val uploadedAt: Timestamp = Timestamp.now(),
    val lastReadWordIndex: Int = 0,
    val lastReadAt: Timestamp? = null
) {
    val hasProgress: Boolean
        get() = lastReadWordIndex > 0
    
    val progressPercent: Int
        get() = if (wordCount > 0) ((lastReadWordIndex.toFloat() / wordCount) * 100).toInt() else 0
}

