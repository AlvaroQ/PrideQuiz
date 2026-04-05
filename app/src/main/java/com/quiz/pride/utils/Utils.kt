package com.quiz.pride.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import com.quiz.pride.BuildConfig
import com.quiz.pride.R
import java.io.File
import java.util.*


fun File.toBase64(): String {
    val bytes = readBytes()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}
fun log(tag: String?, msg: String?, error: Throwable? = null){
    if (BuildConfig.BUILD_TYPE != "release") {
        if (error != null){
            Log.e(tag, msg, error)
        } else {
            Log.d(tag, msg!!)
        }
    }
}

const val AVERAGE_MONTH_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 30
private const val YEAR_IN_MILLIS = 365L * DateUtils.DAY_IN_MILLIS
fun getRelationTime(context: Context, time: Long): String {
    val now: Long = Date().time
    val delta = now - time
    val resolution: Long = when {
        delta <= DateUtils.MINUTE_IN_MILLIS -> {
            DateUtils.SECOND_IN_MILLIS
        }
        delta <= DateUtils.HOUR_IN_MILLIS -> {
            DateUtils.MINUTE_IN_MILLIS
        }
        delta <= DateUtils.DAY_IN_MILLIS -> {
            DateUtils.HOUR_IN_MILLIS
        }
        delta <= DateUtils.WEEK_IN_MILLIS -> {
            DateUtils.DAY_IN_MILLIS
        }
        else -> return when {
            delta <= AVERAGE_MONTH_IN_MILLIS -> {
                context.resources.getQuantityString(R.plurals.weeks_ago, (delta / DateUtils.WEEK_IN_MILLIS).toInt(), (delta / DateUtils.WEEK_IN_MILLIS).toInt())
            }
            delta <= YEAR_IN_MILLIS -> {
                context.resources.getQuantityString(R.plurals.months_ago, (delta / AVERAGE_MONTH_IN_MILLIS).toInt(), (delta / AVERAGE_MONTH_IN_MILLIS).toInt())
            }
            else -> {
                context.resources.getQuantityString(R.plurals.years_ago, (delta / YEAR_IN_MILLIS).toInt(), (delta / YEAR_IN_MILLIS).toInt())
            }
        }
    }
    return DateUtils.getRelativeTimeSpanString(time, now, resolution).toString()
}

fun rateApp(context: Context) {
    val uri: Uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        context.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")))
    }
}

fun shareApp(points: Int, context: Context) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))

        var shareMessage =
                if(points < 0) context.resources.getString(R.string.share_message_general)
                else context.resources.getString(R.string.share_message, points)

        shareMessage =
                """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                """.trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)

        // Create intent to show the chooser dialog
        val chooser: Intent = Intent.createChooser(shareIntent, context.getString(R.string.choose_one))

        // Verify the original intent will resolve to at least one activity
        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        log(context.getString(R.string.share), e.toString())
    }
}

fun openAppOnPlayStore(context: Context, appPackageName: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
    } catch (notFoundException: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
    }
}