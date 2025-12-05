package com.speedreader.trainer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.speedreader.trainer.ui.screens.auth.LoginScreen
import com.speedreader.trainer.ui.screens.auth.RegisterScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineQuizScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineReadingScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineResultsScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineTestScreen
import com.speedreader.trainer.ui.screens.baseline.BaselineViewModel
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
                onNavigateToBaseline = {
                    navController.navigate(NavRoutes.BaselineTest.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBaseline = {
                    navController.navigate(NavRoutes.BaselineTest.route) {
                        popUpTo(NavRoutes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Baseline flow with shared ViewModel
        navigation(
            startDestination = NavRoutes.BaselineTest.route,
            route = "baseline_flow"
        ) {
            composable(NavRoutes.BaselineTest.route) {
                val parentEntry = navController.getBackStackEntry("baseline_flow")
                val sharedViewModel: BaselineViewModel = hiltViewModel(parentEntry)
                BaselineTestScreen(
                    viewModel = sharedViewModel,
                    onStartReading = {
                        navController.navigate(NavRoutes.BaselineReading.route)
                    }
                )
            }

            composable(NavRoutes.BaselineReading.route) {
                val parentEntry = navController.getBackStackEntry("baseline_flow")
                val sharedViewModel: BaselineViewModel = hiltViewModel(parentEntry)
                BaselineReadingScreen(
                    viewModel = sharedViewModel,
                    onFinishReading = {
                        navController.navigate(NavRoutes.BaselineQuiz.route)
                    }
                )
            }

            composable(NavRoutes.BaselineQuiz.route) {
                val parentEntry = navController.getBackStackEntry("baseline_flow")
                val sharedViewModel: BaselineViewModel = hiltViewModel(parentEntry)
                BaselineQuizScreen(
                    viewModel = sharedViewModel,
                    onQuizComplete = {
                        navController.navigate(NavRoutes.BaselineResults.route)
                    }
                )
            }

            composable(NavRoutes.BaselineResults.route) {
                val parentEntry = navController.getBackStackEntry("baseline_flow")
                val sharedViewModel: BaselineViewModel = hiltViewModel(parentEntry)
                BaselineResultsScreen(
                    viewModel = sharedViewModel,
                    onContinueToDashboard = {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo("baseline_flow") { inclusive = true }
                        }
                    }
                )
            }
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
                onNavigateToReading = { documentId ->
                    navController.navigate(NavRoutes.SpeedReading.createRoute(documentId))
                }
            )
        }

        composable(NavRoutes.DocumentUpload.route) {
            DocumentUploadScreen(
                onNavigateBack = { navController.popBackStack() },
                onUploadSuccess = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.DocumentList.route) {
            DocumentListScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectDocument = { documentId ->
                    navController.navigate(NavRoutes.SpeedReading.createRoute(documentId))
                }
            )
        }

        composable(
            route = NavRoutes.SpeedReading.route,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: return@composable
            SpeedReadingScreen(
                documentId = documentId,
                onNavigateBack = { navController.popBackStack() },
                onSessionComplete = { sessionId, shouldShowQuiz ->
                    if (shouldShowQuiz) {
                        navController.navigate(NavRoutes.ComprehensionQuiz.createRoute(sessionId)) {
                            popUpTo(NavRoutes.SpeedReading.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.SessionResults.createRoute(sessionId)) {
                            popUpTo(NavRoutes.SpeedReading.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.ComprehensionQuiz.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            ComprehensionQuizScreen(
                sessionId = sessionId,
                onQuizComplete = { resultSessionId ->
                    navController.navigate(NavRoutes.SessionResults.createRoute(resultSessionId)) {
                        popUpTo(NavRoutes.ComprehensionQuiz.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.SessionResults.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            SessionResultsScreen(
                sessionId = sessionId,
                onContinue = {
                    navController.navigate(NavRoutes.Dashboard.route) {
                        popUpTo(NavRoutes.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Progress.route) {
            ProgressScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

