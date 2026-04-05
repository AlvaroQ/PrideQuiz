package com.quiz.pride.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.App
import com.quiz.domain.Pride
import com.quiz.pride.BuildConfig
import com.quiz.pride.datasource.dto.AppDto
import com.quiz.pride.datasource.dto.PrideDto
import com.quiz.pride.utils.Constants.PATH_REFERENCE_APPS
import com.quiz.pride.utils.Constants.PATH_REFERENCE_PRIDE
import com.quiz.pride.utils.Constants.TOTAL_ITEM_EACH_LOAD
import com.quiz.pride.utils.log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@ExperimentalCoroutinesApi
class DataBaseSourceImpl : DataBaseSource {

    override suspend fun getPrideById(id: Int): Pride {
        return suspendCancellableCoroutine { continuation ->
            val ref = FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_PRIDE + id)
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val pride = dataSnapshot.getValue(PrideDto::class.java)?.toDomain() ?: Pride()
                    continuation.resumeWith(Result.success(pride))
                }

                override fun onCancelled(error: DatabaseError) {
                    log("getPrideById FAILED", "Failed to read value.", error.toException())
                    continuation.resumeWith(Result.success(Pride()))
                    FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                }
            }
            ref.addListenerForSingleValueEvent(listener)
            continuation.invokeOnCancellation { ref.removeEventListener(listener) }
        }
    }

    override suspend fun getPrideList(currentPage: Int): List<Pride> {
        return suspendCancellableCoroutine { continuation ->
            val ref = FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_PRIDE)
                .orderByKey()
                .startAt((currentPage * TOTAL_ITEM_EACH_LOAD).toString())
                .limitToFirst(TOTAL_ITEM_EACH_LOAD)
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val prideList = mutableListOf<Pride>()
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot in dataSnapshot.children) {
                            val pride = snapshot.getValue(PrideDto::class.java)?.toDomain()
                            if (pride != null) {
                                prideList.add(pride)
                            }
                        }
                    }
                    continuation.resumeWith(Result.success(prideList))
                }

                override fun onCancelled(error: DatabaseError) {
                    log("DataBaseBaseSourceImpl", "Failed to read value.", error.toException())
                    continuation.resumeWith(Result.success(mutableListOf()))
                    FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                }
            }
            ref.addListenerForSingleValueEvent(listener)
            continuation.invokeOnCancellation { ref.removeEventListener(listener) }
        }
    }

    override suspend fun getAppsRecommended(): List<App> {
        return suspendCancellableCoroutine { continuation ->
            val ref = FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_APPS)
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val appList = mutableListOf<App>()
                    if (dataSnapshot.hasChildren()) {
                        for (snapshot in dataSnapshot.children) {
                            val app = snapshot.getValue(AppDto::class.java)?.toDomain()
                            if (app != null) {
                                appList.add(app)
                            }
                        }
                    }
                    continuation.resumeWith(Result.success(appList
                        .sortedBy { it.priority }
                        .filter { it.url != BuildConfig.APPLICATION_ID }))
                }

                override fun onCancelled(error: DatabaseError) {
                    log("DataBaseBaseSourceImpl", "Failed to read value.", error.toException())
                    continuation.resumeWith(Result.success(mutableListOf()))
                    FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                }
            }
            ref.addListenerForSingleValueEvent(listener)
            continuation.invokeOnCancellation { ref.removeEventListener(listener) }
        }
    }
}
