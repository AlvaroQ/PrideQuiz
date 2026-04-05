package com.quiz.pride.application

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.quiz.pride.BuildConfig
import com.quiz.pride.managers.XpSyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.Module

@OptIn(ExperimentalCoroutinesApi::class)
class PrideApp : Application() {

    private val xpSyncManager: XpSyncManager by inject()

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        initializeFirebaseAuth()
    }

    private fun initializeKoin() {
        startKoin {
            androidContext(this@PrideApp)
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            modules(appModule + dataModule + scopesModule)
        }
    }

    private fun initializeFirebaseAuth() {
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously()
        }
    }

    override fun onTerminate() {
        xpSyncManager.cancel()
        super.onTerminate()
    }
}