package com.quiz.pride.managers

import android.content.Context
import timber.log.Timber
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.quiz.pride.common.DataStoreKeys
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.quiz.data.repository.XpLeaderboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.syncDataStore by preferencesDataStore(name = "xp_sync_preferences")

/**
 * Manages XP synchronization between local DataStore and Firestore.
 * El [applicationScope] se inyecta externamente para que su ciclo de vida
 * sea gestionado por la Application y no por esta clase.
 */
class XpSyncManager(
    private val context: Context,
    private val progressionManager: ProgressionManager,
    private val gameStatsManager: GameStatsManager,
    private val xpLeaderboardRepository: XpLeaderboardRepository,
    private val networkManager: NetworkManager,
    private val applicationScope: CoroutineScope
) {
    companion object {
        private val LAST_SYNCED_TIME = DataStoreKeys.XpSyncKeys.LAST_SYNCED_TIME
        private val PENDING_SYNC = DataStoreKeys.XpSyncKeys.PENDING_SYNC
    }

    /**
     * Triggers a sync after game completion
     * Should be called from ResultViewModel after recording game result
     */
    fun triggerSync() {
        applicationScope.launch {
            if (networkManager.isNetworkAvailable()) {
                performSync()
            } else {
                // Mark as pending for when network becomes available
                markSyncPending(true)
                Timber.d("Network not available, sync marked as pending")
            }
        }
    }

    private suspend fun performSync(): Boolean {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            Timber.w("No user UID available, skipping sync")
            return false
        }

        return try {
            val stats = gameStatsManager.getStatistics()
            val entry = progressionManager.getLeaderboardEntry(uid, stats)

            // Only sync if user has a nickname set
            if (entry.nickname.isBlank()) {
                Timber.d("No nickname set, skipping sync")
                return false
            }

            val result = xpLeaderboardRepository.syncUserXp(entry)

            result.fold(
                ifLeft = { error ->
                    Timber.e("Sync failed: $error")
                    markSyncPending(true)
                    false
                },
                ifRight = {
                    Timber.d("Sync successful for user: ${entry.nickname}")
                    markSyncPending(false)
                    updateLastSyncTime()
                    true
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Sync error")
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
