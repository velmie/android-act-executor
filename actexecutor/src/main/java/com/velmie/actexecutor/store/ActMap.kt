package com.velmie.actexecutor.store

import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.Id
import kotlinx.coroutines.Job

interface ActMap {
    fun contains(id: Id): Boolean
    fun add(id: Id, job: Job)
    fun replace(id: Id, job: Job)
    fun remove(id: Id)
    fun removeAll()
}
