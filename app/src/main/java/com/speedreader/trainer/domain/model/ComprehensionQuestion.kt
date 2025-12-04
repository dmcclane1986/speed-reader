package com.speedreader.trainer.domain.model

data class ComprehensionQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0
)

data class QuizResult(
    val questions: List<ComprehensionQuestion>,
    val userAnswers: List<Int>,
    val score: Float // 0.0 to 1.0
)

