package com.quiz.pride.application

import android.app.Application
import com.quiz.pride.managers.XpSyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@OptIn(ExperimentalCoroutinesApi::class)
class PrideApp : Application() {

    private val xpSyncManager: XpSyncManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PrideApp)
            androidLogger()
            modules(appModule + dataModule + scopesModule)
        }
    }

    override fun onTerminate() {
        xpSyncManager.cancel()
        super.onTerminate()
    }
}