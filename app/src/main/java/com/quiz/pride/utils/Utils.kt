package com.quiz.pride.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.quiz.pride.BuildConfig
import java.io.File
import java.util.*


fun File.toBase64(): String {
    val bytes = readBytes()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}
fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    activity.currentFocus?.clearFocus()
    activity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}
fun getCircularProgressDrawable(context: Context) : CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        start()
    }
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

fun Activity.screenOrientationPortrait(){
    requestedOrientation = if (Build.VERSION.SDK_INT == 26) {
        ActivityInfo.SCREEN_ORIENTATION_BEHIND
    } else {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

const val AVERAGE_MONTH_IN_MILLIS = DateUtils.DAY_IN_MILLIS * 30
fun getRelationTime(time: Long): String? {
    val now: Long = Date().time
    val delta = now - time
    val resolution: Long
    resolution = when {
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
                (delta / DateUtils.WEEK_IN_MILLIS).toInt().toString() + " weeks(s) ago"
            }
            delta <= DateUtils.YEAR_IN_MILLIS -> {
                (delta / AVERAGE_MONTH_IN_MILLIS).toInt().toString() + " month(s) ago"
            }
            else -> {
                (delta / DateUtils.YEAR_IN_MILLIS).toInt().toString() + " year(s) ago"
            }
        }
    }
    return DateUtils.getRelativeTimeSpanString(time, now, resolution).toString()
}