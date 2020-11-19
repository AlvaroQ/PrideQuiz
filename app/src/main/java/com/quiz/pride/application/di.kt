package com.quiz.pride.application

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.quiz.data.datasource.DataBaseSource
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.data.repository.AppsRecommendedRepository
import com.quiz.data.repository.PrideByIdRepository
import com.quiz.data.repository.RankingRepository
import com.quiz.pride.common.ResourceProvider
import com.quiz.pride.common.ResourceProviderImpl
import com.quiz.pride.datasource.DataBaseSourceImpl
import com.quiz.pride.datasource.FirestoreDataSourceImpl
import com.quiz.pride.ui.game.GameFragment
import com.quiz.pride.ui.game.GameViewModel
import com.quiz.pride.ui.info.InfoFragment
import com.quiz.pride.ui.info.InfoViewModel
import com.quiz.pride.ui.ranking.RankingFragment
import com.quiz.pride.ui.ranking.RankingViewModel
import com.quiz.pride.ui.result.ResultFragment
import com.quiz.pride.ui.result.ResultViewModel
import com.quiz.pride.ui.select.SelectFragment
import com.quiz.pride.ui.select.SelectViewModel
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
}

val dataModule = module {
    factory { PrideByIdRepository(get()) }
    factory { AppsRecommendedRepository(get()) }
    factory { RankingRepository(get()) }
}

private val scopesModule = module {
    scope(named<SelectFragment>()) {
        viewModel { SelectViewModel() }
    }
    scope(named<GameFragment>()) {
        viewModel { GameViewModel(get(), get()) }
        scoped { GetPrideById(get()) }
    }
    scope(named<ResultFragment>()) {
        viewModel { ResultViewModel(get(), get(), get()) }
        scoped { GetRecordScore(get()) }
        scoped { GetAppsRecommended(get()) }
        scoped { SaveTopScore(get()) }
    }
    scope(named<RankingFragment>()) {
        viewModel { RankingViewModel(get()) }
        scoped { GetRankingScore(get()) }
    }
    scope(named<InfoFragment>()) {
        viewModel { InfoViewModel(get()) }
        scoped { GetSymbolFlagList(get()) }
    }
}
