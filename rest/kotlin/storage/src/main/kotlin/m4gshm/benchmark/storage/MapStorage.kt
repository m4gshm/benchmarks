package m4gshm.benchmark.storage


class MapStorage<T, ID>(private val storage: MutableMap<ID, T>) : Storage<T, ID> {

    override fun get(id: ID): T? = storage[id]

    override fun store(id: ID, t: T) {
        storage[id] = t
    }

    override fun getAll() = ArrayList(storage.values)

    override fun delete(id: ID): Boolean {
        return storage.remove(id) != null
    }
}
