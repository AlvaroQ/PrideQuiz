package com.quiz.pride.application

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.quiz.data.datasource.DataBaseSource
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.data.datasource.GameResultProcessorDataSource
import com.quiz.data.datasource.SharedPreferencesLocalDataSource
import com.quiz.data.datasource.XpLeaderboardDataSource
import com.quiz.data.repository.AppsRecommendedRepository
import com.quiz.data.repository.AppsRecommendedRepositoryImpl
import com.quiz.data.repository.PrideByIdRepository
import com.quiz.data.repository.PrideByIdRepositoryImpl
import com.quiz.data.repository.RankingRepository
import com.quiz.data.repository.RankingRepositoryImpl
import com.quiz.data.repository.SharedPreferencesRepository
import com.quiz.data.repository.SharedPreferencesRepositoryImpl
import com.quiz.data.repository.XpLeaderboardRepository
import com.quiz.data.repository.XpLeaderboardRepositoryImpl
import com.quiz.pride.datasource.DataBaseSourceImpl
import com.quiz.pride.datasource.FirestoreDataSourceImpl
import com.quiz.pride.datasource.GameResultProcessorImpl
import com.quiz.pride.datasource.XpLeaderboardDataSourceImpl
import com.quiz.pride.managers.AdFrequencyManager
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.BillingManager
import com.quiz.pride.managers.ConsentManager
import com.quiz.pride.managers.NetworkManager
import com.quiz.pride.managers.OnboardingPreferences
import com.quiz.pride.managers.AchievementManager
import com.quiz.pride.managers.GameStatsManager
import com.quiz.pride.managers.ProgressionManager
import com.quiz.pride.managers.SharedPrefsDataSource
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.managers.XpSyncManager
import com.quiz.pride.ui.leaderboard.XpLeaderboardViewModel
import com.quiz.pride.ui.game.GameViewModel
import com.quiz.pride.ui.info.InfoViewModel
import com.quiz.pride.ui.moreApps.MoreAppsViewModel
import com.quiz.pride.ui.profile.ProfileViewModel
import com.quiz.pride.ui.ranking.RankingViewModel
import com.quiz.pride.ui.result.ResultViewModel
import com.quiz.pride.ui.select.SelectGameViewModel
import com.quiz.pride.ui.select.SelectViewModel
import com.quiz.pride.ui.settings.SettingsViewModel
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPersonalRecord
import com.quiz.usecases.GetPrideById
import com.quiz.usecases.GetPrideList
import com.quiz.usecases.GetRankingScore
import com.quiz.usecases.GetRecordScore
import com.quiz.usecases.GetUserGlobalRank
import com.quiz.usecases.GetXpLeaderboard
import com.quiz.usecases.SaveTopScore
import com.quiz.usecases.SetPaymentDone
import com.quiz.usecases.SetPersonalRecord
import com.quiz.usecases.ProcessGameResultUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Managers globales de la aplicacion (singletons con ciclo de vida de Application)
@OptIn(ExperimentalCoroutinesApi::class)
val managerModule = module {
    single {
        Firebase.firestore.apply {
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(
                    persistentCacheSettings {
                        setSizeBytes(100 * 1024 * 1024) // 100 MB
                    }
                )
            }
        }
    }
    // Scope de aplicacion con SupervisorJob: sobrevive fallos individuales
    // y se cancela cuando la Application es destruida por el OS
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    single { AnalyticsManager(androidContext()) }
    single { ThemeManager(androidContext()) }
    single { OnboardingPreferences(androidContext()) }
    // ConsentManager: singleton para gestionar el ciclo GDPR/UMP durante toda la sesion
    single { ConsentManager(androidContext()) }
    single { NetworkManager(androidContext(), get()) }
    single { AdFrequencyManager(androidContext()) }
    single { ProgressionManager(androidContext()) }
    single { GameStatsManager(androidContext(), get()) }
    single { AchievementManager(androidContext(), get(), get()) }

    // El applicationScope (CoroutineScope) se inyecta para que su ciclo de vida
    // sea gestionado externamente en lugar de crear un scope interno sin cancelacion garantizada
    single { XpSyncManager(androidContext(), get(), get(), get(), get(), get()) }

    // BillingManager: singleton para gestionar el ciclo de vida del BillingClient
    single { BillingManager(androidContext()) }
}

// DataSources como singletons: son stateless y reutilizables
@OptIn(ExperimentalCoroutinesApi::class)
val dataSourceModule = module {
    single<DataBaseSource> { DataBaseSourceImpl() }
    single<FirestoreDataSource> { FirestoreDataSourceImpl(get()) }
    single<SharedPreferencesLocalDataSource> { SharedPrefsDataSource(get()) }
    single<XpLeaderboardDataSource> { XpLeaderboardDataSourceImpl(get()) }
    // GameResultProcessorDataSource: singleton porque los managers que delega son singletons
    single<GameResultProcessorDataSource> { GameResultProcessorImpl(get(), get(), get()) }
}

// Repositories con cache son single: el cache in-memory debe sobrevivir entre navigaciones
// Repositories sin cache son factory: nueva instancia por request, sin estado compartido
val repositoryModule = module {
    single<PrideByIdRepository> { PrideByIdRepositoryImpl(get()) }
    single<AppsRecommendedRepository> { AppsRecommendedRepositoryImpl(get()) }
    single<RankingRepository> { RankingRepositoryImpl(get()) }
    factory<SharedPreferencesRepository> { SharedPreferencesRepositoryImpl(get()) }
    factory<XpLeaderboardRepository> { XpLeaderboardRepositoryImpl(get()) }
}

// Use Cases como factory: stateless, nueva instancia cada vez
val useCaseModule = module {
    factory { GetPaymentDone(get()) }
    factory { SetPaymentDone(get()) }
    factory { GetPrideById(get()) }
    factory { GetRecordScore(get()) }
    factory { GetAppsRecommended(get()) }
    factory { SaveTopScore(get()) }
    factory { GetPersonalRecord(get()) }
    factory { SetPersonalRecord(get()) }
    factory { GetRankingScore(get()) }
    factory { GetPrideList(get()) }

    // Procesamiento de resultado de partida (encapsula stats + achievements + xp sync)
    factory { ProcessGameResultUseCase(get()) }

    // XP Leaderboard use cases
    factory { GetXpLeaderboard(get()) }
    factory { GetUserGlobalRank(get()) }
}

// ViewModels con lifecycle-aware scope
val viewModelModule = module {
    // SavedStateHandle es inyectado automaticamente por Koin 4.x (koin-android)
    // cuando el ViewModel lo declara como parametro de constructor
    viewModel { GameViewModel(get(), get(), get(), get(), get()) }
    viewModel {
        ResultViewModel(
            getAppsRecommended = get(),
            saveTopScore = get(),
            getRecordScore = get(),
            getPersonalRecord = get(),
            setPersonalRecord = get(),
            getPaymentDone = get(),
            processGameResult = get(),
            progressionManager = get(),
            analyticsManager = get(),
            adFrequencyManager = get()
        )
    }
    viewModel { RankingViewModel(get(), get(), get(), get()) }
    viewModel { InfoViewModel(get(), get(), get()) }
    viewModel { MoreAppsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { XpLeaderboardViewModel(get(), get(), get()) }
    viewModel { SelectViewModel(get()) }
    viewModel { SelectGameViewModel(get()) }
}

// Alias para compatibilidad: PrideApp carga estos modulos
@OptIn(ExperimentalCoroutinesApi::class)
val appModule = managerModule
val dataModule = repositoryModule + dataSourceModule
val scopesModule = useCaseModule + viewModelModule
