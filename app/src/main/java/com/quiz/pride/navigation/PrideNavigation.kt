package com.quiz.pride.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quiz.pride.ui.game.GameScreen
import com.quiz.pride.ui.info.InfoScreen
import com.quiz.pride.ui.moreApps.MoreAppsScreen
import com.quiz.pride.ui.onboarding.OnboardingScreen
import com.quiz.pride.ui.ranking.RankingScreen
import com.quiz.pride.ui.result.ResultScreen
import com.quiz.pride.ui.select.SelectGameScreen
import com.quiz.pride.ui.select.SelectScreen
import com.quiz.pride.ui.settings.SettingsScreen
import com.quiz.pride.utils.Constants

/**
 * Sealed class representing all screens in the app
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Select : Screen("select")
    object SelectGame : Screen("select_game")

    object Game : Screen("game/{gameType}") {
        fun createRoute(gameType: Constants.GameType) = "game/${gameType.name}"
    }

    object Result : Screen("result/{points}/{totalQuestions}/{correctAnswers}/{bestStreak}/{timePlayed}") {
        fun createRoute(
            points: Int,
            totalQuestions: Int = 0,
            correctAnswers: Int = 0,
            bestStreak: Int = 0,
            timePlayed: Long = 0
        ) = "result/$points/$totalQuestions/$correctAnswers/$bestStreak/$timePlayed"
    }

    object Ranking : Screen("ranking")
    object Info : Screen("info")
    object Settings : Screen("settings")
    object MoreApps : Screen("more_apps")
}

private const val TRANSITION_DURATION = 300

/**
 * Main Navigation Graph for PrideQuiz
 */
@Composable
fun PrideNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Select.route,
    onOnboardingComplete: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        }
    ) {
        // Onboarding Screen
        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            OnboardingScreen(
                onFinish = {
                    onOnboardingComplete()
                    navController.navigate(Screen.Select.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Menu
        composable(Screen.Select.route) {
            SelectScreen(
                onNavigateToSelectGame = {
                    navController.navigate(Screen.SelectGame.route)
                },
                onNavigateToInfo = {
                    navController.navigate(Screen.Info.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Game Difficulty Selection
        composable(Screen.SelectGame.route) {
            SelectGameScreen(
                onNavigateToGame = { gameType ->
                    navController.navigate(Screen.Game.createRoute(gameType))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Game Screen
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("gameType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameTypeString = backStackEntry.arguments?.getString("gameType") ?: "NORMAL"
            val gameType = Constants.GameType.valueOf(gameTypeString)

            GameScreen(
                gameType = gameType,
                onNavigateToResult = { points, totalQuestions, correctAnswers, bestStreak, timePlayed ->
                    navController.navigate(
                        Screen.Result.createRoute(points, totalQuestions, correctAnswers, bestStreak, timePlayed)
                    ) {
                        // Clear game from back stack
                        popUpTo(Screen.Select.route) { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Result Screen
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("points") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType; defaultValue = 0 },
                navArgument("correctAnswers") { type = NavType.IntType; defaultValue = 0 },
                navArgument("bestStreak") { type = NavType.IntType; defaultValue = 0 },
                navArgument("timePlayed") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val points = backStackEntry.arguments?.getInt("points") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
            val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0
            val bestStreak = backStackEntry.arguments?.getInt("bestStreak") ?: 0
            val timePlayed = backStackEntry.arguments?.getLong("timePlayed") ?: 0L

            ResultScreen(
                points = points,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                bestStreak = bestStreak,
                timePlayed = timePlayed,
                onNavigateToGame = {
                    navController.navigate(Screen.SelectGame.route) {
                        popUpTo(Screen.Select.route) { inclusive = false }
                    }
                },
                onNavigateToRanking = {
                    navController.navigate(Screen.Ranking.route)
                },
                onNavigateBack = {
                    navController.popBackStack(Screen.Select.route, inclusive = false)
                }
            )
        }

        // Ranking Screen
        composable(Screen.Ranking.route) {
            RankingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Info/Learn Screen
        composable(Screen.Info.route) {
            InfoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToMoreApps = {
                    navController.navigate(Screen.MoreApps.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // More Apps Screen
        composable(Screen.MoreApps.route) {
            MoreAppsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
