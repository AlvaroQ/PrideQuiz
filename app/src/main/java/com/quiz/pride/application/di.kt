package com.quiz.pride.application

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.quiz.data.datasource.DataBaseSource
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.data.datasource.SharedPreferencesLocalDataSource
import com.quiz.data.datasource.XpLeaderboardDataSource
import com.quiz.data.repository.AppsRecommendedRepository
import com.quiz.data.repository.PrideByIdRepository
import com.quiz.data.repository.RankingRepository
import com.quiz.data.repository.SharedPreferencesRepository
import com.quiz.data.repository.XpLeaderboardRepository
import com.quiz.pride.datasource.DataBaseSourceImpl
import com.quiz.pride.datasource.FirestoreDataSourceImpl
import com.quiz.pride.datasource.XpLeaderboardDataSourceImpl
import com.quiz.pride.managers.AdFrequencyManager
import com.quiz.pride.managers.NetworkManager
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
import com.quiz.usecases.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val appModule = module {
    factory { Firebase.firestore }
    single<CoroutineDispatcher> { Dispatchers.Main }
    factory<DataBaseSource> { DataBaseSourceImpl() }
    factory<FirestoreDataSource> { FirestoreDataSourceImpl(get()) }
    factory<SharedPreferencesLocalDataSource> { SharedPrefsDataSource(get()) }
    factory<XpLeaderboardDataSource> { XpLeaderboardDataSourceImpl(get()) }

    // Theme Manager (DataStore based)
    single { ThemeManager(androidContext()) }

    // Network Manager for offline support
    single { NetworkManager(androidContext()) }

    // Ad Frequency Manager for controlling ad display frequency
    single { AdFrequencyManager(androidContext()) }

    // Progression Manager for XP, levels, and achievements
    single { ProgressionManager(androidContext()) }

    // XP Sync Manager for Firestore leaderboard synchronization
    single { XpSyncManager(androidContext(), get(), get(), get()) }
}

val dataModule = module {
    factoryOf(::PrideByIdRepository)
    factoryOf(::AppsRecommendedRepository)
    factoryOf(::RankingRepository)
    factoryOf(::SharedPreferencesRepository)
    factoryOf(::XpLeaderboardRepository)
}

val scopesModule = module {
    viewModel { SelectViewModel() }
    viewModel { SelectGameViewModel() }
    viewModel { GameViewModel(get(), get()) }
    viewModel { ResultViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { RankingViewModel(get(), get(), get(), get()) }
    viewModel { InfoViewModel(get(), get()) }
    viewModel { MoreAppsViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
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

    // Timed ranking use cases
    factory { GetTimedRankingScore(get()) }
    factory { GetTimedRecordScore(get()) }
    factory { SaveTimedTopScore(get()) }

    // XP Leaderboard use cases
    factory { SyncUserXp(get()) }
    factory { GetXpLeaderboard(get()) }
    factory { GetUserGlobalRank(get()) }
    factory { GetUserXpEntry(get()) }
}
