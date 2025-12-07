package com.quiz.pride.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quiz.pride.ui.game.GameScreen
import com.quiz.pride.ui.info.InfoScreen
import com.quiz.pride.ui.moreApps.MoreAppsScreen
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
    object Select : Screen("select")
    object SelectGame : Screen("select_game")

    object Game : Screen("game/{gameType}") {
        fun createRoute(gameType: Constants.GameType) = "game/${gameType.name}"
    }

    object Result : Screen("result/{points}") {
        fun createRoute(points: Int) = "result/$points"
    }

    object Ranking : Screen("ranking")
    object Info : Screen("info")
    object Settings : Screen("settings")
    object MoreApps : Screen("more_apps")
}

/**
 * Main Navigation Graph for PrideQuiz
 */
@Composable
fun PrideNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Select.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
                onNavigateToResult = { points ->
                    navController.navigate(Screen.Result.createRoute(points)) {
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
                navArgument("points") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val points = backStackEntry.arguments?.getInt("points") ?: 0

            ResultScreen(
                points = points,
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
