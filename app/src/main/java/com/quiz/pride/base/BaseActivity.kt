package com.quiz.pride.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.utils.log
import com.quiz.pride.utils.screenOrientationPortrait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity(var uiContext: CoroutineContext = Dispatchers.Main) :
    AppCompatActivity(),
    BaseViewModel,
    CoroutineScope {

    private val tag = "BaseActivity"
    private lateinit var auth: FirebaseAuth

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = uiContext + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenOrientationPortrait()

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        // Initialize Firebase Auth
        auth = Firebase.auth
        AnalyticsManager.initialize(this, getUID())
    }

    public override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    log(tag, "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    log(tag, "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        val isSignedIn = user != null
        log(tag, "updateUI, isSignedON = $isSignedIn")


        if (isSignedIn) {
            FirebaseCrashlytics.getInstance().setUserId(user?.uid!!)
            log(tag, "updateUI, you are login in")
        }
    }

    private fun getUID(): String {
        return if (auth.currentUser == null) {
            ""
        } else {
            auth.currentUser?.uid!!
        }
    }
}