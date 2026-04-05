package com.quiz.pride.application

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.quiz.data.datasource.DataBaseSource
import com.quiz.data.datasource.FirestoreDataSource
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
import com.quiz.pride.datasource.XpLeaderboardDataSourceImpl
import com.quiz.pride.managers.AdFrequencyManager
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.NetworkManager
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
import com.quiz.pride.ui.settings.SettingsViewModel
import com.quiz.usecases.GetAppsRecommended
import com.quiz.usecases.GetPaymentDone
import com.quiz.usecases.GetPersonalRecord
import com.quiz.usecases.GetPrideById
import com.quiz.usecases.GetPrideList
import com.quiz.usecases.GetRankingScore
import com.quiz.usecases.GetRecordScore
import com.quiz.usecases.GetUserGlobalRank
import com.quiz.usecases.GetUserXpEntry
import com.quiz.usecases.GetXpLeaderboard
import com.quiz.usecases.SaveTopScore
import com.quiz.usecases.SetPaymentDone
import com.quiz.usecases.SetPersonalRecord
import com.quiz.usecases.SyncUserXp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val appModule = module {
    factory { Firebase.firestore }
    single<CoroutineDispatcher> { Dispatchers.Main }
    factory<DataBaseSource> { DataBaseSourceImpl() }
    factory<FirestoreDataSource> { FirestoreDataSourceImpl(get()) }
    factory<SharedPreferencesLocalDataSource> { SharedPrefsDataSource(get()) }
    factory<XpLeaderboardDataSource> { XpLeaderboardDataSourceImpl(get()) }

    // Analytics Manager
    single { AnalyticsManager(androidContext()) }

    // Theme Manager (DataStore based)
    single { ThemeManager(androidContext()) }

    // Network Manager for offline support
    single { NetworkManager(androidContext()) }

    // Ad Frequency Manager for controlling ad display frequency
    single { AdFrequencyManager(androidContext()) }

    // Progression Manager for XP, levels, and profile
    single { ProgressionManager(androidContext()) }

    // Game Stats Manager for recording game results and statistics
    single { GameStatsManager(androidContext(), get()) }

    // Achievement Manager for unlocking and checking achievements
    single { AchievementManager(androidContext(), get(), get()) }

    // XP Sync Manager for Firestore leaderboard synchronization
    single { XpSyncManager(androidContext(), get(), get(), get(), get()) }
}

val dataModule = module {
    factory<PrideByIdRepository> { PrideByIdRepositoryImpl(get()) }
    factory<AppsRecommendedRepository> { AppsRecommendedRepositoryImpl(get()) }
    factory<RankingRepository> { RankingRepositoryImpl(get()) }
    factory<SharedPreferencesRepository> { SharedPreferencesRepositoryImpl(get()) }
    factory<XpLeaderboardRepository> { XpLeaderboardRepositoryImpl(get()) }
}

val scopesModule = module {
    viewModel { GameViewModel(get(), get(), get()) }
    viewModel {
        ResultViewModel(
            getAppsRecommended = get(),
            saveTopScore = get(),
            getRecordScore = get(),
            getPersonalRecord = get(),
            setPersonalRecord = get(),
            getPaymentDone = get(),
            progressionManager = get(),
            gameStatsManager = get(),
            achievementManager = get(),
            xpSyncManager = get(),
            analyticsManager = get()
        )
    }
    viewModel { RankingViewModel(get(), get(), get(), get()) }
    viewModel { InfoViewModel(get(), get(), get()) }
    viewModel { MoreAppsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get()) }
    viewModel { XpLeaderboardViewModel(get(), get(), get()) }

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

    // XP Leaderboard use cases
    factory { SyncUserXp(get()) }
    factory { GetXpLeaderboard(get()) }
    factory { GetUserGlobalRank(get()) }
    factory { GetUserXpEntry(get()) }
}
