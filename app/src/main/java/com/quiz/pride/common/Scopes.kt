package com.quiz.pride.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

interface Scope : CoroutineScope {
    var job: Job
    val uiDispatcher : CoroutineDispatcher

    class Impl(override val uiDispatcher: CoroutineDispatcher): Scope {
        override lateinit var job: Job
    }

    override val coroutineContext: CoroutineContext
        get() = uiDispatcher + job

    fun initScope(){
        job = SupervisorJob()
    }

    fun destroyScope(){
        job.cancel()
    }
}