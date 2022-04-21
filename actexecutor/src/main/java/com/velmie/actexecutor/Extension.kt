package com.velmie.actexecutor

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.FlowAct
import com.velmie.actexecutor.act.Id
import com.velmie.actexecutor.act.LiveDataAct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

fun <T : Any> LiveData<T>.toAct(
    id: Id,
    lifecycleOwner: LifecycleOwner,
    doAfterAct: (T) -> Unit
): Act = LiveDataAct(id, this, lifecycleOwner, doAfterAct)

fun <T : Any> Flow<T>.toAct(
    id: Id,
    coroutineScope: CoroutineScope,
    doAfterAct: (T) -> Unit
): Act = FlowAct(id, this, coroutineScope, doAfterAct)
