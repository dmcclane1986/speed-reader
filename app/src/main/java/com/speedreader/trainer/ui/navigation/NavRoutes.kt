package com.speedreader.trainer.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash : NavRoutes("splash")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object BaselineTest : NavRoutes("baseline_test")
    object BaselineReading : NavRoutes("baseline_reading")
    object BaselineQuiz : NavRoutes("baseline_quiz")
    object BaselineResults : NavRoutes("baseline_results")
    object Dashboard : NavRoutes("dashboard")
    object DocumentUpload : NavRoutes("document_upload")
    object DocumentList : NavRoutes("document_list")
    object SpeedReading : NavRoutes("speed_reading/{documentId}") {
        fun createRoute(documentId: String) = "speed_reading/$documentId"
    }
    object ComprehensionQuiz : NavRoutes("comprehension_quiz/{sessionId}") {
        fun createRoute(sessionId: String) = "comprehension_quiz/$sessionId"
    }
    object SessionResults : NavRoutes("session_results/{sessionId}") {
        fun createRoute(sessionId: String) = "session_results/$sessionId"
    }
    object Progress : NavRoutes("progress")
    object Settings : NavRoutes("settings")
}

