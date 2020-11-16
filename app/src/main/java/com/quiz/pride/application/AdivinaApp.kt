package com.quiz.pride.application

import android.app.Application

class AdivinaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initDI()
    }
}