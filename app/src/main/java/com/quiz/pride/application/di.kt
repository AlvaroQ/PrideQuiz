package com.quiz.pride.application

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.quiz.data.datasource.DataBaseSource
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.data.datasource.SharedPreferencesLocalDataSource
import com.quiz.data.repository.AppsRecommendedRepository
import com.quiz.data.repository.PrideByIdRepository
import com.quiz.data.repository.RankingRepository
import com.quiz.data.repository.SharedPreferencesRepository
import com.quiz.pride.common.ResourceProvider
import com.quiz.pride.common.ResourceProviderImpl
import com.quiz.pride.datasource.DataBaseSourceImpl
import com.quiz.pride.datasource.FirestoreDataSourceImpl
import com.quiz.pride.managers.SharedPrefsDataSource
import com.quiz.pride.ui.game.GameFragment
import com.quiz.pride.ui.game.GameViewModel
import com.quiz.pride.ui.info.InfoFragment
import com.quiz.pride.ui.info.InfoViewModel
import com.quiz.pride.ui.moreApps.MoreAppsFragment
import com.quiz.pride.ui.moreApps.MoreAppsViewModel
import com.quiz.pride.ui.ranking.RankingFragment
import com.quiz.pride.ui.ranking.RankingViewModel
import com.quiz.pride.ui.result.ResultFragment
import com.quiz.pride.ui.result.ResultViewModel
import com.quiz.pride.ui.select.SelectFragment
import com.quiz.pride.ui.select.SelectGameFragment
import com.quiz.pride.ui.select.SelectGameViewModel
import com.quiz.pride.ui.select.SelectViewModel
import com.quiz.pride.ui.settings.SettingsFragment
import com.quiz.pride.ui.settings.SettingsViewModel
import com.quiz.usecases.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun Application.initDI() {
    startKoin {
        androidLogger()
        androidContext(this@initDI)
        koin.loadModules(listOf(
            appModule,
            dataModule,
            scopesModule
        ))
        koin.createRootScope()
    }
}

private val appModule = module {
    factory<ResourceProvider> { ResourceProviderImpl(androidContext().resources) }
    factory { Firebase.firestore }
    single<CoroutineDispatcher> { Dispatchers.Main }
    factory<DataBaseSource> { DataBaseSourceImpl() }
    factory<FirestoreDataSource> { FirestoreDataSourceImpl(get()) }
    factory<SharedPreferencesLocalDataSource> { SharedPrefsDataSource(get()) }
}

val dataModule = module {
    factory { PrideByIdRepository(get()) }
    factory { AppsRecommendedRepository(get()) }
    factory { RankingRepository(get()) }
    factory { SharedPreferencesRepository(get()) }
}

private val scopesModule = module {
    scope(named<SelectFragment>()) {
        viewModel { SelectViewModel() }
    }
    scope(named<SelectGameFragment>()) {
        viewModel { SelectGameViewModel() }
    }
    scope(named<GameFragment>()) {
        viewModel { GameViewModel(get(), get(), get()) }
        scoped { GetPrideById(get()) }
        scoped { GetPaymentDone(get()) }
    }
    scope(named<ResultFragment>()) {
        viewModel { ResultViewModel(get(), get(), get(), get(), get(), get()) }
        scoped { GetRecordScore(get()) }
        scoped { GetAppsRecommended(get()) }
        scoped { SaveTopScore(get()) }
        scoped { GetPersonalRecord(get())}
        scoped { SetPersonalRecord(get())}
        scoped { GetPaymentDone(get()) }
    }
    scope(named<RankingFragment>()) {
        viewModel { RankingViewModel(get(), get()) }
        scoped { GetRankingScore(get()) }
        scoped { GetPaymentDone(get()) }
    }
    scope(named<InfoFragment>()) {
        viewModel { InfoViewModel(get(), get()) }
        scoped { GetPrideList(get()) }
        scoped { GetPaymentDone(get()) }
    }
    scope(named<MoreAppsFragment>()) {
        viewModel { MoreAppsViewModel(get(), get()) }
        scoped { GetAppsRecommended(get()) }
        scoped { GetPaymentDone(get()) }
    }
    scope(named<SettingsFragment>()) {
        viewModel { SettingsViewModel(get(), get()) }
        scoped { GetPaymentDone(get()) }
        scoped { SetPaymentDone(get()) }
    }
}
