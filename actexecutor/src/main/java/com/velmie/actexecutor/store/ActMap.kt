package com.velmie.actexecutor.store

import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.Id

interface ActMap {
    fun contains(id: Id): Boolean
    fun add(id: Id, act: Act)
    fun remove(id: Id)
    fun removeAll()
    fun replace(id: Id, act: Act)
    fun getFirstKey(): Id
    fun getFirstValue(): Act
    fun isNotEmpty(): Boolean
    fun size(): Int
}
