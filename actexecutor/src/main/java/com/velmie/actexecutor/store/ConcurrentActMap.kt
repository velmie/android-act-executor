package com.velmie.actexecutor.store

import com.velmie.actexecutor.act.Id
import kotlinx.coroutines.Job
import java.util.concurrent.ConcurrentHashMap

class ConcurrentActMap : ActMap {

    private val map = ConcurrentHashMap<Id, Job>()

    override fun contains(id: Id): Boolean {
        return map.containsKey(id)
    }

    @Synchronized
    override fun add(id: Id, job: Job) {
        map[id] = job
    }

    @Synchronized
    override fun remove(id: Id) {
        map[id]?.cancel()
        map.remove(id)
    }

    @Synchronized
    override fun replace(id: Id, job: Job) {
        map[id]?.cancel()
        map.replace(id, job)
    }

    override fun removeAll() {
        map.forEach { it.value.cancel() }
        map.clear()
    }
}
