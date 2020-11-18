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
import kotlinx.coroutines.suspendCancellableCoroutine

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
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(8)

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
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit)

            ref.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects<User>().last().points.toString()){}
                }
                .addOnFailureListener {
                    continuation.resume(""){}
                    FirebaseCrashlytics.getInstance().recordException(Throwable(it.cause))
                }
        }
    }
}