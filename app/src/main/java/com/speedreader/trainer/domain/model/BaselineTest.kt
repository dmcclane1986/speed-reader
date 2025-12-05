package com.speedreader.trainer.domain.model

data class BaselineTest(
    val passage: String,
    val wordCount: Int,
    val questions: List<ComprehensionQuestion>
)

object BaselineTestData {
    val test = BaselineTest(
        passage = """
            The Art of Speed Reading

            Speed reading is a collection of techniques designed to increase reading rate without sacrificing comprehension. The average adult reads at approximately 200-300 words per minute, but skilled speed readers can achieve rates of 1,000 words per minute or more while maintaining good understanding of the material.

            The foundation of speed reading lies in eliminating subvocalization, the habit of silently pronouncing words in your head as you read. This internal voice limits reading speed to speaking speed, typically around 150-200 words per minute. By training yourself to recognize words visually without pronouncing them, you can significantly increase your reading pace.

            Another key technique is expanding your peripheral vision to take in groups of words rather than reading one word at a time. The human eye can actually perceive several words in a single fixation. By reducing the number of eye movements per line, readers can dramatically increase their speed.

            Meta-guiding, or using a finger or pointer to guide your eyes across the page, helps maintain focus and prevents regression, the tendency to re-read passages unnecessarily. This simple technique can immediately boost reading speed by 25-50%.

            Preview and review strategies also enhance speed reading effectiveness. Before diving into detailed reading, quickly scanning headings, first sentences, and key terms creates a mental framework that makes subsequent reading faster and more meaningful.

            Practice is essential for developing speed reading skills. Like any skill, reading faster requires consistent effort and gradual progression. Starting with easier materials and progressively challenging yourself with more complex texts helps build sustainable speed improvements.

            The ultimate goal of speed reading is not merely to read faster, but to process and retain information more efficiently. A truly skilled speed reader adjusts their pace based on material difficulty and purpose, reading technical material more slowly while breezing through familiar content.
        """.trimIndent(),
        wordCount = 298,
        questions = listOf(
            ComprehensionQuestion(
                id = "baseline_1",
                question = "What is subvocalization?",
                options = listOf(
                    "Reading out loud to others",
                    "Silently pronouncing words in your head while reading",
                    "Skipping words while reading",
                    "Reading multiple books at once"
                ),
                correctAnswerIndex = 1
            ),
            ComprehensionQuestion(
                id = "baseline_2",
                question = "According to the passage, what is the average adult reading speed?",
                options = listOf(
                    "100-150 words per minute",
                    "200-300 words per minute",
                    "400-500 words per minute",
                    "600-700 words per minute"
                ),
                correctAnswerIndex = 1
            ),
            ComprehensionQuestion(
                id = "baseline_3",
                question = "What is 'meta-guiding'?",
                options = listOf(
                    "Reading about metaphysics",
                    "Using a finger or pointer to guide your eyes",
                    "Skipping entire paragraphs",
                    "Reading only headings"
                ),
                correctAnswerIndex = 1
            ),
            ComprehensionQuestion(
                id = "baseline_4",
                question = "By how much can meta-guiding immediately boost reading speed?",
                options = listOf(
                    "5-10%",
                    "10-15%",
                    "25-50%",
                    "75-100%"
                ),
                correctAnswerIndex = 2
            ),
            ComprehensionQuestion(
                id = "baseline_5",
                question = "What is 'regression' in the context of reading?",
                options = listOf(
                    "Reading backwards",
                    "The tendency to re-read passages unnecessarily",
                    "Forgetting what you read",
                    "Reading only familiar words"
                ),
                correctAnswerIndex = 1
            )
        )
    )
}

