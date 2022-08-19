package com.quiz.pride.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresPermission
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quiz.pride.BuildConfig
import com.quiz.pride.R
import java.io.File
import java.util.*


fun showBanner(show: Boolean, adView: AdView){
    if(show) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    } else {
        adView.visibility = View.GONE
    }
}
fun loadBonificado(activity: Activity, show: Boolean, rewardedAd: RewardedAd) {
    if(show) {
        val adLoadCallback: RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                rewardedAd.show(activity, null)
            }

            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
    }
}
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
            delta <= DateUtils.YEAR_IN_MILLIS -> {
                context.resources.getQuantityString(R.plurals.months_ago, (delta / AVERAGE_MONTH_IN_MILLIS).toInt(), (delta / AVERAGE_MONTH_IN_MILLIS).toInt())
            }
            else -> {
                context.resources.getQuantityString(R.plurals.years_ago, (delta / DateUtils.YEAR_IN_MILLIS).toInt(), (delta / DateUtils.YEAR_IN_MILLIS).toInt())
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
                Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")))
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
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.choose_one)))
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