package com.speedreader.trainer.domain.model

/**
 * Pre-loaded baseline test content with passage and questions
 */
object BaselineTestContent {
    val passage = """
The art of reading has evolved significantly throughout human history, from ancient scrolls to modern digital screens. In today's fast-paced world, the ability to read quickly while maintaining comprehension has become an invaluable skill. Speed reading, once considered a niche technique practiced by a select few, has now gained mainstream attention as people seek to consume more information in less time.

The human brain is remarkably adaptable when it comes to processing written text. Research has shown that the average adult reads at approximately 200 to 300 words per minute, yet with proper training, many individuals can double or even triple their reading speed without sacrificing understanding. This improvement stems from eliminating inefficient reading habits that most people develop during childhood.

One of the primary obstacles to faster reading is subvocalization, the habit of silently pronouncing each word in your mind as you read. While this internal speech helped us learn to read as children, it creates a bottleneck that limits our reading speed to the pace of spoken language. By training ourselves to recognize words visually rather than phonetically, we can bypass this limitation.

Another key factor in reading speed is the number of fixations our eyes make while scanning text. Untrained readers often fixate on individual words, moving their eyes in a sequential pattern across each line. Skilled speed readers, however, train their peripheral vision to capture groups of words in a single glance, dramatically reducing the number of eye movements required to read a page.

The relationship between speed and comprehension is not as straightforward as one might assume. Contrary to popular belief, reading faster does not necessarily mean understanding less. In fact, many speed readers report improved comprehension because maintaining a brisk pace keeps the mind engaged and prevents it from wandering. The key lies in finding the optimal balance between speed and understanding for each type of material.

Different reading purposes call for different approaches. Technical documents, legal contracts, and academic papers may require slower, more deliberate reading, while news articles, emails, and general interest content can often be processed much more quickly. Effective readers learn to adjust their speed based on the complexity and importance of the material at hand.

Practice remains the cornerstone of developing speed reading abilities. Like any skill, reading faster requires consistent effort and gradual progression. Starting with easier texts and gradually increasing difficulty, while regularly testing comprehension, helps build both confidence and capability. Modern technology has made practice more accessible than ever, with numerous apps and tools designed to help readers track their progress and push their boundaries.
    """.trimIndent()

    val wordCount = passage.split(Regex("\\s+")).size

    val questions = listOf(
        ComprehensionQuestion(
            id = "baseline_1",
            question = "According to the passage, what is the average reading speed of an adult?",
            options = listOf(
                "100 to 150 words per minute",
                "200 to 300 words per minute",
                "400 to 500 words per minute",
                "600 to 700 words per minute"
            ),
            correctAnswerIndex = 1
        ),
        ComprehensionQuestion(
            id = "baseline_2",
            question = "What is subvocalization?",
            options = listOf(
                "Reading out loud to others",
                "Skipping words while reading",
                "Silently pronouncing words in your mind while reading",
                "Reading multiple books at once"
            ),
            correctAnswerIndex = 2
        ),
        ComprehensionQuestion(
            id = "baseline_3",
            question = "How do skilled speed readers reduce the number of eye movements?",
            options = listOf(
                "By closing one eye while reading",
                "By using their peripheral vision to capture groups of words",
                "By reading only the first word of each sentence",
                "By memorizing the text beforehand"
            ),
            correctAnswerIndex = 1
        ),
        ComprehensionQuestion(
            id = "baseline_4",
            question = "According to the passage, what happens when readers maintain a brisk reading pace?",
            options = listOf(
                "Comprehension always decreases",
                "The mind tends to wander more",
                "The mind stays engaged and comprehension may improve",
                "Readers become tired quickly"
            ),
            correctAnswerIndex = 2
        ),
        ComprehensionQuestion(
            id = "baseline_5",
            question = "What types of documents may require slower, more deliberate reading?",
            options = listOf(
                "News articles and emails",
                "Technical documents, legal contracts, and academic papers",
                "General interest content",
                "Social media posts"
            ),
            correctAnswerIndex = 1
        ),
        ComprehensionQuestion(
            id = "baseline_6",
            question = "What is described as the 'cornerstone' of developing speed reading abilities?",
            options = listOf(
                "Natural talent",
                "Expensive equipment",
                "Practice",
                "Formal education"
            ),
            correctAnswerIndex = 2
        )
    )
}

