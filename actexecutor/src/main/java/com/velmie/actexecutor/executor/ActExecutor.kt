package com.velmie.actexecutor.executor

import android.util.Log
import androidx.lifecycle.Observer
import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.LiveDataAct
import com.velmie.actexecutor.act.SimpleAct
import com.velmie.actexecutor.store.ActMap
import com.velmie.networkutils.core.Resource
import com.velmie.networkutils.core.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

private const val DEFAULT_DELAY_IN_MILLIS = 200

class ActExecutor(private val actMap: ActMap) : ActExecutorInterface {

    companion object {
        const val TAG = "ActExecutor"
    }

    @Synchronized
    override fun execute(act: Act) {
        return when {
            actMap.contains(act.id) -> {
                Log.d(TAG, "id: ${act.id} - Act duplicate")
                Unit
            }
            else -> startExecution(act)
        }
    }

    private fun startExecution(act: Act) {
        actMap.add(act.id, act)
        val removeFromMap = { actMap.remove(act.id) }
        when (act) {
            is SimpleAct -> {
                finishDelay(
                    fullDelay = act.delay,
                    invokeFunTime = measureTimeMillis { act.actFunction.invoke() })
                { removeFromMap() }
            }
            is LiveDataAct<*> -> {
                act.liveData.observe(act.lifecycleOwner, Observer {
                    when (it) {
                        is Resource<*> -> {
                            if (it.status == Status.ERROR || it.status == Status.SUCCESS) {
                                finishDelay(
                                    fullDelay = act.delay,
                                    invokeFunTime = measureTimeMillis { act.afterAct(it) })
                                { removeFromMap() }
                            }
                        }
                        else -> throw IllegalArgumentException("Type T in LiveData<T> unregistered")
                    }
                })
            }
            else -> throw IllegalArgumentException("Type Act unregistered")
        }
    }

    private fun finishDelay(
        fullDelay: Int? = DEFAULT_DELAY_IN_MILLIS,
        invokeFunTime: Long,
        doAfterDelay: () -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(fullDelay!! - invokeFunTime)
            doAfterDelay()
        }
    }
}
