package com.velmie.actexecutor

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.velmie.actexecutor.act.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

fun <T : Any> LiveData<T>.toAct(
    id: Id,
    lifecycleOwner: LifecycleOwner,
    doAfterAct: (T) -> Unit,
    actPolicy: ActPolicy = ActPolicy.DEFAULT
): Act = LiveDataAct(id, this, lifecycleOwner, actPolicy, doAfterAct)

fun <T : Any> Flow<T>.toAct(
    id: Id,
    coroutineScope: CoroutineScope,
    doAfterAct: (T) -> Unit,
    actPolicy: ActPolicy = ActPolicy.DEFAULT
): Act = FlowAct(id, this, coroutineScope, actPolicy, doAfterAct)
