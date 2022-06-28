package com.velmie.actexecutor.act

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

class LiveDataAct<T : Any>(
    override val id: Id,
    val liveData: LiveData<T>,
    val lifecycleOwner: LifecycleOwner,
    override val actPolicy: ActPolicy = ActPolicy.DEFAULT,
    val doAfterAct: (T) -> Unit
) : Act {

    fun afterAct(result: Any) {
        doAfterAct(result as T)
    }
}
