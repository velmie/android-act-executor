package com.velmie.actexecutor.executor

import com.velmie.actexecutor.BuildConfig
import com.velmie.actexecutor.act.*
import com.velmie.actexecutor.store.ActMap
import com.velmie.networkutils.core.Error
import com.velmie.networkutils.core.Resource
import com.velmie.networkutils.core.Success
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.system.measureTimeMillis

class ActExecutor(private val actMap: ActMap) : ActExecutorInterface {

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun execute(act: Act) {
        scope.launch {
            if (actMap.contains(act.id)) {
                when (act.actPolicy) {
                    ActPolicy.DEFAULT -> Timber.d("id: ${act.id} - Act duplicate")
                    ActPolicy.REPLACE -> {
                        actMap.replace(act.id, startExecution(act))
                        Timber.d("id: ${act.id} - Act was replaced")
                    }
                    ActPolicy.IN_LINE -> {
                        while (actMap.contains(act.id)) {
                            delay(100)
                        }
                        actMap.add(act.id, startExecution(act))
                        Timber.d("id: ${act.id} - Act duplicate")
                    }
                }
            } else {
                actMap.add(act.id, startExecution(act))
            }
        }
    }

    private suspend fun startExecution(act: Act) = scope.launch {
        val removeFromMap = { actMap.remove(act.id) }
        when (act) {
            is SimpleAct -> {
                act.actFunction()
                removeFromMap()
            }
            is DelayAct -> {
                val invokeTime = measureTimeMillis { act.actFunction() }
                withContext(scope.coroutineContext) {
                    withContext(Dispatchers.IO) {
                        delay(act.delay - invokeTime)
                        removeFromMap()
                    }
                }
            }
            is FlowAct<*> -> {
                act.coroutineScope.launch {
                    act.flow.collect {
                        when (it) {
                            is Resource<*> -> {
                                act.afterAct(it)
                                if (it is Error<*> || it is Success<*>) {
                                    removeFromMap()
                                    cancel()
                                }
                            }
                            else -> {
                                removeFromMap()
                                throw IllegalArgumentException("Type T in Flow<T> unregistered")
                            }
                        }
                    }
                }
            }
            is LiveDataAct<*> -> {
                act.liveData.observe(act.lifecycleOwner) {
                    when (it) {
                        is Resource<*> -> {
                            act.afterAct(it)
                            if (it is Error<*> || it is Success<*>) {
                                removeFromMap()
                            }
                        }
                        else -> {
                            removeFromMap()
                            throw IllegalArgumentException("Type T in LiveData<T> unregistered")
                        }
                    }
                }
            }
            else -> throw IllegalArgumentException("Type Act unregistered")
        }
        joinAll()
    }

    fun enableLogging() {
        Timber.plant(Timber.DebugTree())
    }
}
