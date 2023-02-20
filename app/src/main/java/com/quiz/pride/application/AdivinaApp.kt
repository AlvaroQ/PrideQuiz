package com.quiz.pride.application

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@OptIn(ExperimentalCoroutinesApi::class)
class AdivinaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AdivinaApp)
            androidLogger()
            modules(appModule + dataModule + scopesModule)
        }
    }
}