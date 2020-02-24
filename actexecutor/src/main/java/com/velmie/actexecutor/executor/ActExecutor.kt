package com.velmie.actexecutor.executor

import androidx.lifecycle.Observer
import com.velmie.actexecutor.BuildConfig
import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.Id
import com.velmie.actexecutor.act.SimpleAct
import com.velmie.actexecutor.act.DelayAct
import com.velmie.actexecutor.act.LiveDataAct
import com.velmie.actexecutor.store.ActMap
import com.velmie.networkutils.core.Resource
import com.velmie.networkutils.core.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.system.measureTimeMillis

class ActExecutor(
    private val actMap: ActMap,
    private val strategyType: ActStrategy = ActStrategy.DEFAULT,
    actDispatcher: ActDispatchers = ActDispatchers.DEFAULT
) : ActExecutorInterface {

    private val scope: CoroutineScope = CoroutineScope(actDispatcher.dispatcher)
    private var job: Job? = null

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @Synchronized
    override fun execute(act: Act) {
        return when (strategyType) {
            ActStrategy.DEFAULT -> startAct(act)
            ActStrategy.REPLACE -> startReplaceAct(act)
            ActStrategy.QUEUE -> startQueueAct(act = act)
        }
    }

    private fun startAct(act: Act) {
        when {
            actMap.contains(act.id) -> Timber.d("id: ${act.id} - Act duplicate")
            actMap.size() == 1 -> Timber.d("Only one event can be processed at the same time")
            else -> executeAct(act.id, act)
        }
    }

    private fun startReplaceAct(act: Act) {
        when {
            actMap.contains(act.id) -> actMap.replace(act.id, act)
            actMap.size() == 1 -> Timber.d("Only one event can be processed at the same time")
            else -> executeAct(act.id, act)
        }
    }

    private fun startQueueAct(id: Id = (Math.random()).toString(), act: Act) {
        executeAct(id, act)
    }

    private fun executeAct(id: Id, act: Act) {
        try {
            if (!actMap.contains(id)) {
                actMap.add(id, act)
            }

            val removeFromMap = { actMap.remove(id) }
            if ((job == null) || (job!!.isCancelled) || (job!!.isCompleted)) {
                when (act) {
                    is SimpleAct -> {
                        act.actFunction()
                        removeFromMap()
                        executeNextActIfExist()
                    }
                    is DelayAct -> {
                        val invokeTime = measureTimeMillis { act.actFunction() }
                        job = scope.launch {
                            delay(act.delay - invokeTime)
                            removeFromMap()
                            job?.cancel()
                            executeNextActIfExist()
                        }
                    }
                    is LiveDataAct<*> -> {
                        act.liveData.observe(act.lifecycleOwner, Observer {
                            when (it) {
                                is Resource<*> -> {
                                    act.afterAct(it)
                                    if (it.status == Status.ERROR || it.status == Status.SUCCESS) {
                                        removeFromMap()
                                        executeNextActIfExist()
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
        } catch (exception: IllegalArgumentException) {
            throw exception
        } catch (exception: java.lang.RuntimeException) {
            throw RuntimeException("You cannot perform UI tasks outside the UI thread. For perform UI tasks switch to dispatcher.MAIN in the constructor ActExecutor")
        }
    }

    private fun executeNextActIfExist() {
        if (actMap.isNotEmpty()) {
            val id = actMap.getFirstKey()
            val value = actMap.getFirstValue()
            startQueueAct(id, value)
        }
    }

    fun enableLogging() {
        Timber.plant(Timber.DebugTree())
    }
}
