package com.quiz.pride.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.quiz.pride.R
import com.quiz.pride.utils.listener.SafeClickListener


fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        startAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_xy_collapse))

        if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sound", true)) {
            MediaPlayer.create(context, R.raw.click).start()
        }
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
fun View.setBackground(context: Context, isCorrect: Boolean) {
    background =  ContextCompat.getDrawable(context,
        if(isCorrect) { R.drawable.button_radius_correct }
        else { R.drawable.button_radius_wrong })
}

fun View.traslationAnimation() {
    // center to left
    ObjectAnimator.ofFloat(this, "translationX", -width.toFloat()).apply {
        duration = 200
        start()
    }

    // (HIDDEN) left to right
    ObjectAnimator.ofFloat(this, "translationX", width.toFloat()).apply {
        duration = 0
        startDelay = 200
        start()
    }

    // Right to center
    ObjectAnimator.ofFloat(this, "translationX", 0f).apply {
        duration = 200
        startDelay = 200
        start()
    }
}

fun View.traslationAnimationFadeIn() {
    traslationAnimation()
    val animation = this.animate().alpha(1f).setInterpolator(AccelerateDecelerateInterpolator())
    animation.duration = 200
    animation.startDelay = 300
    animation.start()
}