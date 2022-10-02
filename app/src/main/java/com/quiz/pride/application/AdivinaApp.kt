package com.quiz.pride.application

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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