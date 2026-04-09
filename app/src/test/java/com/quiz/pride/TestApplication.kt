package com.quiz.pride

import android.app.Application

/**
 * Application vacia para tests de Robolectric/Roborazzi.
 * Evita que Koin, Firebase y AdMob se inicialicen durante screenshot tests.
 */
class TestApplication : Application()
