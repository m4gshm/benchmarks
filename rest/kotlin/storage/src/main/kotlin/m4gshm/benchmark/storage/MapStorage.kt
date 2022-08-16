package m4gshm.benchmark.storage

import m4gshm.benchmark.rest.java.jft.StorageEvent.start


class MapStorage<T, ID>(private val storage: MutableMap<ID, T>) : Storage<T, ID> {

    override fun get(id: ID): T? = rec("get") { storage[id] }

    override fun store(id: ID, t: T) = rec("store") { storage[id] = t }

    override fun getAll() = rec("getAll") { ArrayList(storage.values) }

    override fun delete(id: ID) = rec("delete") { storage.remove(id) != null }

    private fun <R : Any?> rec(name: String, block: () -> R) = start("MapStorage.$name").use { block.invoke() }
}
