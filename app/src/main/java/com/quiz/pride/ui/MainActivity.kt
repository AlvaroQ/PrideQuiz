package com.quiz.pride.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.quiz.pride.managers.AnalyticsManager
import com.quiz.pride.managers.ThemeManager
import com.quiz.pride.navigation.PrideNavGraph
import com.quiz.pride.navigation.Screen
import com.quiz.pride.ui.theme.PrideQuizTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val themeManager: ThemeManager by inject()
    private val analyticsManager: AnalyticsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Initialize Analytics first with current user or anonymous
        initializeAnalytics()

        // Firebase Anonymous Authentication (will update Analytics uid when ready)
        initializeFirebaseAuth()

        setContent {
            // Collect theme state
            val isDarkMode by themeManager.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
            val isDynamicColors by themeManager.isDynamicColorsEnabled.collectAsStateWithLifecycle(initialValue = true)
            val isOnboardingCompleted by themeManager.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = true)
            val coroutineScope = rememberCoroutineScope()

            // Determine start destination based on onboarding status
            val startDestination = if (isOnboardingCompleted) {
                Screen.Select.route
            } else {
                Screen.Onboarding.route
            }

            PrideQuizTheme(
                darkTheme = isDarkMode,
                dynamicColor = isDynamicColors
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    PrideNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        onOnboardingComplete = {
                            coroutineScope.launch {
                                themeManager.setOnboardingCompleted(true)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun initializeAnalytics() {
        // Initialize with current user or anonymous to prevent crashes
        val currentUid = Firebase.auth.currentUser?.uid ?: "anonymous"
        analyticsManager.uid = currentUid
    }

    private fun initializeFirebaseAuth() {
        val auth = Firebase.auth
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    result.user?.uid?.let { uid ->
                        Firebase.crashlytics.setUserId(uid)
                        // Update Analytics with real uid
                        analyticsManager.uid = uid
                    }
                }
                .addOnFailureListener { exception ->
                    Firebase.crashlytics.recordException(exception)
                }
        } else {
            auth.currentUser?.uid?.let { uid ->
                Firebase.crashlytics.setUserId(uid)
            }
        }
    }
}
