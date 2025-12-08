package com.quiz.pride.datasource

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
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
                    continuation.resume(user.right()){}
                }
                .addOnFailureListener {
                    continuation.resume(RepositoryException.NoConnectionException.left()){}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }

    override suspend fun getRanking(): MutableList<User> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(50)

            ref.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects<User>().toMutableList()){}
                }
                .addOnFailureListener {
                    continuation.resume(mutableListOf()){}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }

    override suspend fun getWorldRecords(limit: Long): String {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)

            ref.get()
                .addOnSuccessListener {
                    try {
                        continuation.resume(it.toObjects<User>().last().score.toString()){}
                    } catch (noSuchElementException: NoSuchElementException) {
                        continuation.resume(""){}
                        FirebaseCrashlytics.getInstance().recordException(Throwable(noSuchElementException.cause))
                    }
                }
                .addOnFailureListener {
                    continuation.resume(""){}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }

    // Timed ranking methods
    override suspend fun addTimedRecord(user: User): Either<RepositoryException, User> {
        return suspendCancellableCoroutine { continuation ->
            database.collection(COLLECTION_RANKING_TIMED)
                .add(user)
                .addOnSuccessListener {
                    continuation.resume(user.right()){}
                }
                .addOnFailureListener {
                    continuation.resume(RepositoryException.NoConnectionException.left()){}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }

    override suspend fun getTimedRanking(): MutableList<User> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING_TIMED)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(50)

            ref.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects<User>().toMutableList()){}
                }
                .addOnFailureListener {
                    continuation.resume(mutableListOf()){}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }

    override suspend fun getTimedWorldRecords(limit: Long): String {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING_TIMED)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)

            ref.get()
                .addOnSuccessListener {
                    try {
                        continuation.resume(it.toObjects<User>().last().score.toString()){}
                    } catch (noSuchElementException: NoSuchElementException) {
                        continuation.resume(""){}
                        FirebaseCrashlytics.getInstance().recordException(Throwable(noSuchElementException.cause))
                    }
                }
                .addOnFailureListener {
                    continuation.resume(""){}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }
}