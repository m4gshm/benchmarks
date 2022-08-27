package m4gshm.benchmark.storage

import m4gshm.benchmark.rest.java.jft.StorageEvent.start
import m4gshm.benchmark.rest.java.storage.model.IdAware
import m4gshm.benchmark.rest.java.storage.Storage


class MapStorage<T : IdAware<ID>, ID>(private val storage: MutableMap<ID, T>) : Storage<T, ID> {

    override fun get(id: ID): T? = rec("get") { storage[id] }

    override fun store(t: T): T = rec("store") { t.apply { storage[this.id] = this } }

    override fun getAll() = rec("getAll") { storage.values.toList() }

    override fun delete(id: ID) = rec("delete") { storage.remove(id) != null }

    private fun <R : Any?> rec(name: String, block: () -> R) = start("MapStorage.$name").use { block.invoke() }
}
