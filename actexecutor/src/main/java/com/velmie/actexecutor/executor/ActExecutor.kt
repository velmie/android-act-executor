package com.velmie.actexecutor.executor

import androidx.lifecycle.Observer
import com.velmie.actexecutor.BuildConfig
import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.DelayAct
import com.velmie.actexecutor.act.LiveDataAct
import com.velmie.actexecutor.act.SimpleAct
import com.velmie.actexecutor.store.ActMap
import com.velmie.networkutils.core.Resource
import com.velmie.networkutils.core.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.system.measureTimeMillis

class ActExecutor(private val actMap: ActMap) : ActExecutorInterface {

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    @Synchronized
    override fun execute(act: Act) {
        return when {
            actMap.contains(act.id) -> {
                Timber.d("id: ${act.id} - Act duplicate")
            }
            else -> startExecution(act)
        }
    }

    private fun startExecution(act: Act) {
        actMap.add(act.id, act)
        val removeFromMap = { actMap.remove(act.id) }
        when (act) {
            is SimpleAct -> {
                act.actFunction()
                removeFromMap()
            }
            is DelayAct -> {
                val invokeTime = measureTimeMillis { act.actFunction() }
                scope.launch {
                    delay(act.delay - invokeTime)
                    removeFromMap()
                }
            }
            is LiveDataAct<*> -> {
                act.liveData.observe(act.lifecycleOwner, Observer {
                    when (it) {
                        is Resource<*> -> {
                            act.afterAct(it)
                            if (it.status == Status.ERROR || it.status == Status.SUCCESS) {
                                removeFromMap()
                            }
                        }
                        else -> {
                            removeFromMap()
                            throw IllegalArgumentException("Type T in LiveData<T> unregistered")
                        }
                    }
                })
            }
            else -> throw IllegalArgumentException("Type Act unregistered")
        }
    }

    fun enableLogging() {
        Timber.plant(Timber.DebugTree())
    }
}
