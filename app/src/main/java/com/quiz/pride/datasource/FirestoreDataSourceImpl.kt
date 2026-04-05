package com.quiz.pride.datasource

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User
import com.quiz.pride.utils.Constants.COLLECTION_RANKING
import com.quiz.pride.utils.Constants.COLLECTION_RANKING_TIMED
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@ExperimentalCoroutinesApi
class FirestoreDataSourceImpl(private val database: FirebaseFirestore) : FirestoreDataSource {

    override suspend fun addRecord(user: User): Either<RepositoryException, User> {
        return suspendCancellableCoroutine { continuation ->
            database.collection(COLLECTION_RANKING)
                .add(user)
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(user.right()))
                }
                .addOnFailureListener {
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }

    override suspend fun getRanking(): Either<RepositoryException, List<User>> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(50)

            ref.get()
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(it.toObjects<User>().right()))
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }

    override suspend fun getWorldRecords(limit: Long): Either<RepositoryException, String> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)

            ref.get()
                .addOnSuccessListener {
                    try {
                        val record = it.toObjects<User>().last().score.toString()
                        continuation.resumeWith(Result.success(record.right()))
                    } catch (noSuchElementException: NoSuchElementException) {
                        FirebaseCrashlytics.getInstance().recordException(Throwable(noSuchElementException.cause))
                        continuation.resumeWith(Result.success(RepositoryException.DataNotFoundException.left()))
                    }
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }

    // Timed ranking methods
    override suspend fun addTimedRecord(user: User): Either<RepositoryException, User> {
        return suspendCancellableCoroutine { continuation ->
            database.collection(COLLECTION_RANKING_TIMED)
                .add(user)
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(user.right()))
                }
                .addOnFailureListener {
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }

    override suspend fun getTimedRanking(): Either<RepositoryException, List<User>> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING_TIMED)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(50)

            ref.get()
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(it.toObjects<User>().right()))
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }

    override suspend fun getTimedWorldRecords(limit: Long): Either<RepositoryException, String> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING_TIMED)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)

            ref.get()
                .addOnSuccessListener {
                    try {
                        val record = it.toObjects<User>().last().score.toString()
                        continuation.resumeWith(Result.success(record.right()))
                    } catch (noSuchElementException: NoSuchElementException) {
                        FirebaseCrashlytics.getInstance().recordException(Throwable(noSuchElementException.cause))
                        continuation.resumeWith(Result.success(RepositoryException.DataNotFoundException.left()))
                    }
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }
}
