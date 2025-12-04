package com.speedreader.trainer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.speedreader.trainer.ui.screens.auth.LoginScreen
import com.speedreader.trainer.ui.screens.auth.RegisterScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineQuizScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineReadingScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineResultsScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineTestScreen
import com.speedreader.trainer.ui.screens.dashboard.DashboardScreen
import com.speedreader.trainer.ui.screens.document.DocumentListScreen
import com.speedreader.trainer.ui.screens.document.DocumentUploadScreen
import com.speedreader.trainer.ui.screens.progress.ProgressScreen
import com.speedreader.trainer.ui.screens.quiz.ComprehensionQuizScreen
import com.speedreader.trainer.ui.screens.quiz.SessionResultsScreen
import com.speedreader.trainer.ui.screens.reading.SpeedReadingScreen
import com.speedreader.trainer.ui.screens.settings.SettingsScreen
import com.speedreader.trainer.ui.screens.splash.SplashScreen

@Composable
fun SpeedReaderNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        composable(NavRoutes.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToBaseline = {
                    navController.navigate(NavRoutes.BaselineTest.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.Register.route)
                },
                onLoginSuccess = { hasCompletedBaseline ->
                    if (hasCompletedBaseline) {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.BaselineTest.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.BaselineTest.route) {
                        popUpTo(NavRoutes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.BaselineTest.route) {
            BaselineTestScreen(
                onStartTest = {
                    navController.navigate(NavRoutes.BaselineReading.route)
                }
            )
        }

        composable(NavRoutes.BaselineReading.route) {
            BaselineReadingScreen(
                onFinishReading = {
                    navController.navigate(NavRoutes.BaselineQuiz.route) {
                        popUpTo(NavRoutes.BaselineReading.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.BaselineQuiz.route) {
            BaselineQuizScreen(
                onQuizComplete = {
                    navController.navigate(NavRoutes.BaselineResults.route) {
                        popUpTo(NavRoutes.BaselineQuiz.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.BaselineResults.route) {
            BaselineResultsScreen(
                onContinue = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.BaselineTest.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Dashboard.route) {
            DashboardScreen(
                onNavigateToUpload = {
                    navController.navigate(NavRoutes.DocumentUpload.route)
                },
                onNavigateToDocuments = {
                    navController.navigate(NavRoutes.DocumentList.route)
                },
                onNavigateToProgress = {
                    navController.navigate(NavRoutes.Progress.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onStartReading = { documentId ->
                    navController.navigate(NavRoutes.SpeedReading.createRoute(documentId))
                }
            )
        }

        composable(NavRoutes.DocumentUpload.route) {
            DocumentUploadScreen(
                onUploadComplete = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.DocumentList.route) {
            DocumentListScreen(
                onSelectDocument = { documentId ->
                    navController.navigate(NavRoutes.SpeedReading.createRoute(documentId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoutes.SpeedReading.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
            SpeedReadingScreen(
                documentId = documentId,
                onSessionComplete = { sessionId ->
                    navController.navigate(NavRoutes.ComprehensionQuiz.createRoute(sessionId)) {
                        popUpTo(NavRoutes.SpeedReading.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavRoutes.ComprehensionQuiz.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ComprehensionQuizScreen(
                sessionId = sessionId,
                onQuizComplete = {
                    navController.navigate(NavRoutes.SessionResults.createRoute(sessionId)) {
                        popUpTo(NavRoutes.ComprehensionQuiz.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.SessionResults.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionResultsScreen(
                sessionId = sessionId,
                onContinue = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.SessionResults.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Progress.route) {
            ProgressScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignOut = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

