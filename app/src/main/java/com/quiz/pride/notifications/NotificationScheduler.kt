package com.quiz.pride.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Programa un recordatorio diario que dispara una notificacion local
 * para recuperar al usuario antes de que pierda su racha.
 *
 * El job se registra con ExistingPeriodicWorkPolicy.KEEP — si ya existe
 * un schedule activo, no se reemplaza. Para cambiar el horario usa
 * [reschedule] (que pasa REPLACE).
 */
object NotificationScheduler {

    private const val WORK_NAME = "pride_quiz_daily_reminder"

    /** Programa la notificacion diaria. Por defecto a las 20:00 hora local. */
    fun scheduleDailyReminder(context: Context, hour: Int = 20, minute: Int = 0) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(computeDelayMillis(hour, minute), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }

    /** Reprograma, reemplazando el schedule existente (usar al cambiar la hora). */
    fun reschedule(context: Context, hour: Int, minute: Int) {
        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(computeDelayMillis(hour, minute), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
    }

    /** Cancela el recordatorio diario (si el usuario lo deshabilita en settings). */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun computeDelayMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
