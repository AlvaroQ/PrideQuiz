package com.quiz.pride.datasource

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.AggregateSource
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
            val sanitizedNickname = entry.nickname.trim().replace(Regex("[<>\"'&;/\\\\]"), "").take(20)
            val data = hashMapOf(
                "uid" to entry.uid,
                "nickname" to sanitizedNickname,
                "imageBase64" to entry.imageBase64,
                "totalXp" to entry.totalXp,
                "level" to entry.level,
                "title" to entry.title,
                "totalGamesPlayed" to entry.totalGamesPlayed,
                "accuracy" to entry.accuracy,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            // set() con merge: crea el documento si no existe, actualiza campos si existe.
            // createdAt se omite del sync: si el documento es nuevo, Firestore lo crea sin ese campo.
            // Esto elimina el get() previo y reduce a 1 sola operacion de escritura.
            database.collection(COLLECTION_XP_LEADERBOARD)
                .document(entry.uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(entry.right()))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
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
                        continuation.resumeWith(Result.success(entry.right()))
                    } else {
                        continuation.resumeWith(Result.success((null as XpLeaderboardEntry?).right()))
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
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
                    continuation.resumeWith(Result.success(entries))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWith(Result.success(emptyList()))
                    FirebaseCrashlytics.getInstance().recordException(Throwable(e.cause))
                }
        }
    }

    override suspend fun getUserRank(uid: String, userXp: Long): Either<RepositoryException, Int> {
        return suspendCancellableCoroutine { continuation ->
            // Count how many users have more XP using aggregate count (1 read instead of N)
            database.collection(COLLECTION_XP_LEADERBOARD)
                .whereGreaterThan("totalXp", userXp)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener { result ->
                    val rank = result.count.toInt() + 1
                    continuation.resumeWith(Result.success(rank.right()))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                    FirebaseCrashlytics.getInstance().recordException(Throwable(e.cause))
                }
        }
    }
}
