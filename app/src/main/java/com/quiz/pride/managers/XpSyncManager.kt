package com.quiz.pride.managers

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.quiz.data.repository.XpLeaderboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.syncDataStore by preferencesDataStore(name = "xp_sync_preferences")

/**
 * Manages XP synchronization between local DataStore and Firestore
 */
class XpSyncManager(
    private val context: Context,
    private val progressionManager: ProgressionManager,
    private val xpLeaderboardRepository: XpLeaderboardRepository,
    private val networkManager: NetworkManager
) {
    companion object {
        private const val TAG = "XpSyncManager"
        private val LAST_SYNCED_TIME = longPreferencesKey("last_synced_time")
        private val PENDING_SYNC = booleanPreferencesKey("pending_sync")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val hasPendingSync: Flow<Boolean> = context.syncDataStore.data
        .map { it[PENDING_SYNC] ?: false }

    val lastSyncTime: Flow<Long> = context.syncDataStore.data
        .map { it[LAST_SYNCED_TIME] ?: 0L }

    /**
     * Triggers a sync after game completion
     * Should be called from ResultViewModel after recording game result
     */
    fun triggerSync() {
        scope.launch {
            if (networkManager.isNetworkAvailable()) {
                performSync()
            } else {
                // Mark as pending for when network becomes available
                markSyncPending(true)
                Log.d(TAG, "Network not available, sync marked as pending")
            }
        }
    }

    /**
     * Called when app resumes to sync any pending changes
     */
    fun syncIfNeeded() {
        scope.launch {
            val pending = context.syncDataStore.data.first()[PENDING_SYNC] ?: false
            if (pending && networkManager.isNetworkAvailable()) {
                performSync()
            }
        }
    }

    /**
     * Force sync - can be called manually from UI
     */
    suspend fun forceSync(): Boolean {
        return if (networkManager.isNetworkAvailable()) {
            performSync()
        } else {
            markSyncPending(true)
            false
        }
    }

    private suspend fun performSync(): Boolean {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            Log.w(TAG, "No user UID available, skipping sync")
            return false
        }

        return try {
            val entry = progressionManager.getLeaderboardEntry(uid)

            // Only sync if user has a nickname set
            if (entry.nickname.isBlank()) {
                Log.d(TAG, "No nickname set, skipping sync")
                return false
            }

            val result = xpLeaderboardRepository.syncUserXp(entry)

            result.fold(
                ifLeft = { error ->
                    Log.e(TAG, "Sync failed: $error")
                    markSyncPending(true)
                    false
                },
                ifRight = {
                    Log.d(TAG, "Sync successful for user: ${entry.nickname}")
                    markSyncPending(false)
                    updateLastSyncTime()
                    true
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            markSyncPending(true)
            false
        }
    }

    private suspend fun markSyncPending(pending: Boolean) {
        context.syncDataStore.edit { preferences ->
            preferences[PENDING_SYNC] = pending
        }
    }

    private suspend fun updateLastSyncTime() {
        context.syncDataStore.edit { preferences ->
            preferences[LAST_SYNCED_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Get current user's UID
     */
    fun getCurrentUserId(): String? = Firebase.auth.currentUser?.uid
}
