package com.quiz.pride.datasource

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.quiz.data.datasource.XpLeaderboardDataSource
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.utils.Constants.COLLECTION_XP_LEADERBOARD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@ExperimentalCoroutinesApi
class XpLeaderboardDataSourceImpl(
    private val database: FirebaseFirestore
) : XpLeaderboardDataSource {

    override suspend fun syncUserXp(entry: XpLeaderboardEntry): Either<RepositoryException, XpLeaderboardEntry> {
        return suspendCancellableCoroutine { continuation ->
            val data = hashMapOf(
                "uid" to entry.uid,
                "nickname" to entry.nickname,
                "imageBase64" to entry.imageBase64,
                "totalXp" to entry.totalXp,
                "level" to entry.level,
                "title" to entry.title,
                "totalGamesPlayed" to entry.totalGamesPlayed,
                "accuracy" to entry.accuracy,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            // Add createdAt only if document doesn't exist
            database.collection(COLLECTION_XP_LEADERBOARD)
                .document(entry.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        data["createdAt"] = FieldValue.serverTimestamp()
                    }

                    database.collection(COLLECTION_XP_LEADERBOARD)
                        .document(entry.uid)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            continuation.resume(entry.right()) {}
                        }
                        .addOnFailureListener { e ->
                            continuation.resume(RepositoryException.NoConnectionException.left()) {}
                            FirebaseCrashlytics.getInstance().recordException(Throwable(e.cause))
                        }
                }
                .addOnFailureListener { e ->
                    continuation.resume(RepositoryException.NoConnectionException.left()) {}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(e.cause))
                }
        }
    }

    override suspend fun getUserXpEntry(uid: String): Either<RepositoryException, XpLeaderboardEntry?> {
        return suspendCancellableCoroutine { continuation ->
            database.collection(COLLECTION_XP_LEADERBOARD)
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val entry = document.toObject(XpLeaderboardEntry::class.java)
                        continuation.resume(entry.right()) {}
                    } else {
                        continuation.resume((null as XpLeaderboardEntry?).right()) {}
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(RepositoryException.NoConnectionException.left()) {}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(e.cause))
                }
        }
    }

    override suspend fun getXpLeaderboard(limit: Int): List<XpLeaderboardEntry> {
        return suspendCancellableCoroutine { continuation ->
            database.collection(COLLECTION_XP_LEADERBOARD)
                .orderBy("totalXp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .addOnSuccessListener { result ->
                    val entries = result.documents.mapNotNull { doc ->
                        doc.toObject(XpLeaderboardEntry::class.java)
                    }
                    continuation.resume(entries) {}
                }
                .addOnFailureListener { e ->
                    continuation.resume(emptyList()) {}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(e.cause))
                }
        }
    }

    override suspend fun getUserRank(uid: String, userXp: Long): Either<RepositoryException, Int> {
        return suspendCancellableCoroutine { continuation ->
            // Count how many users have more XP than the current user
            database.collection(COLLECTION_XP_LEADERBOARD)
                .whereGreaterThan("totalXp", userXp)
                .get()
                .addOnSuccessListener { result ->
                    // Rank = count of users with more XP + 1
                    val rank = result.size() + 1
                    continuation.resume(rank.right()) {}
                }
                .addOnFailureListener { e ->
                    continuation.resume(RepositoryException.NoConnectionException.left()) {}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(e.cause))
                }
        }
    }
}
