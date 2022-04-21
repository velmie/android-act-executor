package com.velmie.actexecutor.act

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class FlowAct<T : Any>(
    override val id: Id,
    val flow: Flow<T>,
    val coroutineScope: CoroutineScope,
    val doAfterAct: (T) -> Unit
) : Act {

    fun afterAct(result: Any) {
        doAfterAct(result as T)
    }
}
