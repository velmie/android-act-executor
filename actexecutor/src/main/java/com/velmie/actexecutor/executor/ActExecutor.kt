package com.velmie.actexecutor.executor

import android.util.Log
import androidx.lifecycle.Observer
import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.LiveDataAct
import com.velmie.actexecutor.act.SimpleAct
import com.velmie.actexecutor.store.ActMap
import com.velmie.networkutils.core.Resource
import com.velmie.networkutils.core.Status

class ActExecutor(private val actMap: ActMap) : ActExecutorInterface {

    companion object {
        val TAG = "ActExecutor"
    }

    @Synchronized
    override fun execute(act: Act) {
        return when {
            actMap.contains(act.id) -> {
                Log.d(TAG, "${act.id} - Act duplicate")
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
                act.actFunction()
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
                        else -> throw IllegalArgumentException("Type T in LiveData<T> unregistered")
                    }
                })
            }
            else -> throw IllegalArgumentException("Type Act unregistered")
        }
    }
}
