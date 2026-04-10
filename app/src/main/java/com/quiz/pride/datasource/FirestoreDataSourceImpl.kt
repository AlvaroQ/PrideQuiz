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

    private fun User.sanitized(): User =
        copy(name = name.replace(Regex("[<>\"'&;/\\\\]"), "").take(20))

    override suspend fun addRecord(user: User): Either<RepositoryException, User> {
        // Fix 2: sanitizar nombre antes de escribir en Firestore
        val sanitizedUser = user.sanitized()
        return suspendCancellableCoroutine { continuation ->
            database.collection(COLLECTION_RANKING)
                .add(sanitizedUser)
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(sanitizedUser.right()))
                }
                .addOnFailureListener {
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                    // Fix 5: pasar la excepcion original, no envolver en Throwable
                    FirebaseCrashlytics.getInstance().recordException(it)
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
                    // Fix 5: pasar la excepcion original, no envolver en Throwable
                    FirebaseCrashlytics.getInstance().recordException(it)
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }

    override suspend fun getWorldRecords(limit: Int, gameMode: String): Either<RepositoryException, String> {
        return suspendCancellableCoroutine { continuation ->
            // Cuando gameMode no esta vacio, filtramos por modo usando el indice compuesto
            // (gameMode ASC, score DESC) creado en Firestore.
            // Con gameMode vacio, no filtramos para mantener compatibilidad con entradas legacy.
            val baseQuery = database.collection(COLLECTION_RANKING)
            val filteredQuery = if (gameMode.isNotEmpty()) {
                baseQuery.whereEqualTo("gameMode", gameMode)
            } else {
                baseQuery
            }
            val ref = filteredQuery
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            ref.get()
                .addOnSuccessListener {
                    // Fix 1: usar lastOrNull() para evitar NoSuchElementException en lista vacia.
                    // Si la lista esta vacia o tiene menos documentos que el limite,
                    // el usuario siempre califica (DataNotFoundException -> usar score 0).
                    val lastScore = it.toObjects<User>().lastOrNull()?.score?.toString()
                    if (lastScore != null) {
                        continuation.resumeWith(Result.success(lastScore.right()))
                    } else {
                        continuation.resumeWith(Result.success(RepositoryException.DataNotFoundException.left()))
                    }
                }
                .addOnFailureListener {
                    // Fix 5: pasar la excepcion original, no envolver en Throwable
                    FirebaseCrashlytics.getInstance().recordException(it)
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }

    // Timed ranking methods
    override suspend fun addTimedRecord(user: User): Either<RepositoryException, User> {
        // Fix 2: sanitizar nombre antes de escribir en Firestore
        val sanitizedUser = user.sanitized()
        return suspendCancellableCoroutine { continuation ->
            database.collection(COLLECTION_RANKING_TIMED)
                .add(sanitizedUser)
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(sanitizedUser.right()))
                }
                .addOnFailureListener {
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                    // Fix 5: pasar la excepcion original, no envolver en Throwable
                    FirebaseCrashlytics.getInstance().recordException(it)
                }
        }
    }

    override suspend fun getTimedRanking(): Either<RepositoryException, List<User>> {
        return suspendCancellableCoroutine { continuation ->
            // Fix 3: alinear limite de display con limite de clasificacion (TOP_RANKING_LIMIT = 20)
            val ref = database
                .collection(COLLECTION_RANKING_TIMED)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(20)

            ref.get()
                .addOnSuccessListener {
                    continuation.resumeWith(Result.success(it.toObjects<User>().right()))
                }
                .addOnFailureListener {
                    // Fix 5: pasar la excepcion original, no envolver en Throwable
                    FirebaseCrashlytics.getInstance().recordException(it)
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }

    override suspend fun getTimedWorldRecords(limit: Int): Either<RepositoryException, String> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(COLLECTION_RANKING_TIMED)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            ref.get()
                .addOnSuccessListener {
                    // Fix 1: usar lastOrNull() para evitar NoSuchElementException en lista vacia.
                    // Si la lista esta vacia o tiene menos documentos que el limite,
                    // el usuario siempre califica (DataNotFoundException -> usar score 0).
                    val lastScore = it.toObjects<User>().lastOrNull()?.score?.toString()
                    if (lastScore != null) {
                        continuation.resumeWith(Result.success(lastScore.right()))
                    } else {
                        continuation.resumeWith(Result.success(RepositoryException.DataNotFoundException.left()))
                    }
                }
                .addOnFailureListener {
                    // Fix 5: pasar la excepcion original, no envolver en Throwable
                    FirebaseCrashlytics.getInstance().recordException(it)
                    continuation.resumeWith(Result.success(RepositoryException.NoConnectionException.left()))
                }
        }
    }
}
