package com.quiz.pride.datasource

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.App
import com.quiz.domain.Pride
import com.quiz.pride.BuildConfig
import com.quiz.pride.utils.Constants.PATH_REFERENCE_APPS
import com.quiz.pride.utils.Constants.PATH_REFERENCE_COUNTRIES
import com.quiz.pride.utils.log
import kotlinx.coroutines.suspendCancellableCoroutine

class DataBaseSourceImpl : DataBaseSource {

    override suspend fun getPrideById(id: Int): Pride {
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_COUNTRIES + id)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        continuation.resume(dataSnapshot.getValue(Pride::class.java) as Pride){}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        log("getPrideById FAILED", "Failed to read value.", error.toException())
                        continuation.resume(Pride()){}
                        FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                    }
                })
        }
    }

    override suspend fun getAppsRecommended(): MutableList<App> {
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_APPS)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var value = dataSnapshot.getValue<MutableList<App>>()
                        if(value == null) value = mutableListOf()
                        continuation.resume(value.filter { it.url != BuildConfig.APPLICATION_ID }.toMutableList()){}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        log("DataBaseBaseSourceImpl", "Failed to read value.", error.toException())
                        continuation.resume(mutableListOf()){}
                        FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                    }
                })
        }
    }
}