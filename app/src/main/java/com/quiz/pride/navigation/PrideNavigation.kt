@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.quiz.pride.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.quiz.pride.ui.game.GameScreen
import com.quiz.pride.ui.info.InfoScreen
import com.quiz.pride.ui.moreApps.MoreAppsScreen
import com.quiz.pride.ui.onboarding.OnboardingScreen
import com.quiz.pride.ui.profile.ProfileScreen
import com.quiz.pride.ui.ranking.RankingScreen
import com.quiz.pride.ui.result.ResultScreen
import com.quiz.pride.ui.select.SelectGameScreen
import com.quiz.pride.ui.select.SelectScreen
import com.quiz.pride.ui.settings.SettingsScreen
import com.quiz.pride.ui.shop.ShopScreen
import com.quiz.pride.ui.MainActivity
import com.quiz.pride.utils.Constants
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.Serializable

// ==========================================
// Rutas type-safe con @Serializable
// ==========================================

@Serializable
data object OnboardingRoute

@Serializable
data object SelectRoute

@Serializable
data object SelectGameRoute

@Serializable
data class GameRoute(val gameType: String)

@Serializable
data class ResultRoute(
    val points: Int,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val bestStreak: Int = 0,
    val timePlayed: Long = 0L,
    val gameType: String = "NORMAL"
)

@Serializable
data object RankingRoute

@Serializable
data object InfoRoute

@Serializable
data object SettingsRoute

@Serializable
data object MoreAppsRoute

@Serializable
data object ProfileRoute

@Serializable
data object ShopRoute

// ==========================================

/**
 * Grafo de navegacion principal de PrideQuiz con rutas type-safe.
 *
 * Motion: usa el MotionScheme expressive del MaterialExpressiveTheme:
 * - defaultSpatialSpec<IntOffset>() (spring) para el slide horizontal
 * - defaultEffectsSpec<Float>() (tween) para el fade de opacidad
 * - slowEffectsSpec<Float>() para la transicion mas calmada del onboarding
 */
@Composable
fun PrideNavGraph(
    navController: NavHostController,
    startDestination: Any = SelectRoute,
    onOnboardingComplete: () -> Unit = {}
) {
    val motionScheme = MaterialTheme.motionScheme
    val slideSpec = motionScheme.defaultSpatialSpec<IntOffset>()
    val fadeSpec = motionScheme.defaultEffectsSpec<Float>()
    val onboardingFadeSpec = motionScheme.slowEffectsSpec<Float>()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = slideSpec
            ) + fadeIn(animationSpec = fadeSpec)
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = slideSpec
            ) + fadeOut(animationSpec = fadeSpec)
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = slideSpec
            ) + fadeIn(animationSpec = fadeSpec)
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = slideSpec
            ) + fadeOut(animationSpec = fadeSpec)
        }
    ) {
        // Onboarding Screen
        composable<OnboardingRoute>(
            enterTransition = { fadeIn(animationSpec = onboardingFadeSpec) },
            exitTransition = { fadeOut(animationSpec = onboardingFadeSpec) }
        ) {
            OnboardingScreen(
                onFinish = {
                    onOnboardingComplete()
                    navController.navigate(SelectRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                }
            )
        }

        // Menu principal
        composable<SelectRoute> {
            SelectScreen(
                onNavigateToSelectGame = {
                    navController.navigate(SelectGameRoute) {
                        launchSingleTop = true
                    }
                },
                onNavigateToInfo = {
                    navController.navigate(InfoRoute) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileRoute) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Seleccion de dificultad / modo de juego
        composable<SelectGameRoute> {
            SelectGameScreen(
                onNavigateToGame = { gameType ->
                    navController.navigate(GameRoute(gameType = gameType.name)) {
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de juego
        composable<GameRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<GameRoute>()
            val gameType = runCatching { Constants.GameType.valueOf(route.gameType) }.getOrElse { Constants.GameType.NORMAL }
            val context = LocalContext.current

            GameScreen(
                gameType = gameType,
                onNavigateToResult = { points, totalQuestions, correctAnswers, bestStreak, timePlayed ->
                    (context as? MainActivity)?.incrementGamesPlayed()
                    navController.navigate(
                        ResultRoute(
                            points = points,
                            totalQuestions = totalQuestions,
                            correctAnswers = correctAnswers,
                            bestStreak = bestStreak,
                            timePlayed = timePlayed,
                            gameType = gameType.name
                        )
                    ) {
                        // Eliminar el juego del back stack al ir a resultado
                        popUpTo<SelectRoute> { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de resultado
        composable<ResultRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ResultRoute>()
            val resultGameType = runCatching { Constants.GameType.valueOf(route.gameType) }.getOrElse { Constants.GameType.NORMAL }

            ResultScreen(
                points = route.points,
                totalQuestions = route.totalQuestions,
                correctAnswers = route.correctAnswers,
                bestStreak = route.bestStreak,
                timePlayed = route.timePlayed,
                gameType = resultGameType,
                onNavigateToGame = {
                    navController.navigate(SelectGameRoute) {
                        popUpTo<SelectRoute> { inclusive = false }
                    }
                },
                onNavigateToRanking = {
                    navController.navigate(RankingRoute)
                },
                onNavigateBack = {
                    navController.popBackStack<SelectRoute>(inclusive = false)
                }
            )
        }

        // Ranking
        composable<RankingRoute> {
            RankingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Info / Aprender
        composable<InfoRoute> {
            InfoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Ajustes
        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateToMoreApps = {
                    navController.navigate(MoreAppsRoute)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Mas aplicaciones
        composable<MoreAppsRoute> {
            MoreAppsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Perfil
        composable<ProfileRoute> {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLeaderboard = {
                    navController.navigate(RankingRoute) {
                        launchSingleTop = true
                    }
                },
                onNavigateToShop = {
                    navController.navigate(ShopRoute) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Tienda de cosmeticos
        composable<ShopRoute> {
            ShopScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
