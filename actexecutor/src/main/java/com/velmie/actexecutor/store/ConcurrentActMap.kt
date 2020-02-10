package com.velmie.actexecutor.store

import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.Id
import java.util.concurrent.ConcurrentHashMap

class ConcurrentActMap : ActMap {

    private val map = ConcurrentHashMap<Id, Act>()

    override fun contains(id: Id): Boolean {
        return map.containsKey(id)
    }

    @Synchronized
    override fun add(id: Id, act: Act) {
        map[id] = act
    }

    @Synchronized
    override fun remove(id: Id) {
        map.remove(id)
    }

    override fun removeAll() {
        map.clear()
    }
}
