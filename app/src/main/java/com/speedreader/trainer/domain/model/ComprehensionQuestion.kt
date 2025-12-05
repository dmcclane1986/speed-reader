package com.speedreader.trainer.domain.model

data class ComprehensionQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0
)

