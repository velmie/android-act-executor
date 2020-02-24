package com.velmie.actexecutor.store

import com.velmie.actexecutor.act.Act
import com.velmie.actexecutor.act.Id
import java.util.Collections
import kotlin.collections.LinkedHashMap

class ConcurrentActMap : ActMap {

    private val map: MutableMap<Id, Act> = Collections.synchronizedMap(LinkedHashMap<Id, Act>())

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

    override fun replace(id: Id, act: Act) {
        map[id] = act
    }

    override fun getFirstKey(): Id {
        return map.keys.first()
    }

    override fun getFirstValue(): Act {
        return map.values.first()
    }

    override fun isNotEmpty(): Boolean {
        return map.isNotEmpty()
    }

    override fun size(): Int {
        return map.size
    }
}
