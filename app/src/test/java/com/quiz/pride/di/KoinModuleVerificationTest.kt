package com.quiz.pride.di

import com.quiz.pride.application.dataSourceModule
import com.quiz.pride.application.repositoryModule
import com.quiz.pride.application.useCaseModule
import com.quiz.pride.application.viewModelModule
import org.junit.Test
import org.koin.test.verify.verify

/**
 * Verifica que todos los modulos de Koin resuelven correctamente sus dependencias.
 *
 * Usa la API verify() de Koin (no el deprecado checkModules()).
 * verify() analiza estaticamente el grafo de dependencias sin necesidad de
 * un contexto Android real — solo verifica que para cada declaracion de binding
 * exista un provider registrado para todas sus dependencias.
 *
 * Nota: managerModule requiere androidContext() y no puede verificarse
 * con verify() en un unit test puro. Se verifica en instrumented tests.
 */
class KoinModuleVerificationTest {

    @Test
    fun `repositoryModule resuelve todas sus dependencias`() {
        repositoryModule.verify()
    }

    @Test
    fun `useCaseModule resuelve todas sus dependencias`() {
        useCaseModule.verify(
            extraTypes = listOf(
                // Repositories que vienen de repositoryModule
                com.quiz.data.repository.SharedPreferencesRepository::class,
                com.quiz.data.repository.PrideByIdRepository::class,
                com.quiz.data.repository.RankingRepository::class,
                com.quiz.data.repository.AppsRecommendedRepository::class,
                com.quiz.data.repository.XpLeaderboardRepository::class,
                com.quiz.data.datasource.GameResultProcessorDataSource::class
            )
        )
    }

    @Test
    fun `viewModelModule resuelve todas sus dependencias`() {
        viewModelModule.verify(
            extraTypes = listOf(
                // Use cases and managers injected into ViewModels
                com.quiz.usecases.GetPrideById::class,
                com.quiz.usecases.GetPaymentDone::class,
                com.quiz.usecases.GetAppsRecommended::class,
                com.quiz.usecases.SaveTopScore::class,
                com.quiz.usecases.GetRecordScore::class,
                com.quiz.usecases.GetPersonalRecord::class,
                com.quiz.usecases.SetPersonalRecord::class,
                com.quiz.usecases.GetRankingScore::class,
                com.quiz.usecases.GetXpLeaderboard::class,
                com.quiz.usecases.GetUserGlobalRank::class,
                com.quiz.usecases.SetPaymentDone::class,
                com.quiz.usecases.GetPrideList::class,
                com.quiz.usecases.ProcessGameResultUseCase::class,
                com.quiz.pride.managers.AnalyticsManager::class,
                com.quiz.pride.managers.ThemeManager::class,
                com.quiz.pride.managers.ProgressionManager::class,
                com.quiz.pride.managers.GameStatsManager::class,
                com.quiz.pride.managers.AchievementManager::class,
                com.quiz.pride.managers.XpSyncManager::class,
                com.quiz.pride.managers.AdFrequencyManager::class,
                com.quiz.pride.managers.BillingManager::class,
                com.quiz.pride.managers.ConsentManager::class,
                // SavedStateHandle injected automatically by Koin for ViewModels
                androidx.lifecycle.SavedStateHandle::class
            )
        )
    }

    @Test
    fun `dataSourceModule resuelve todas sus dependencias`() {
        dataSourceModule.verify(
            extraTypes = listOf(
                // Firestore instance from managerModule
                com.google.firebase.firestore.FirebaseFirestore::class,
                // Context-dependent datasources
                com.quiz.pride.managers.GameStatsManager::class,
                com.quiz.pride.managers.AchievementManager::class,
                com.quiz.pride.managers.XpSyncManager::class
            )
        )
    }
}
