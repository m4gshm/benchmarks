package m4gshm.benchmark.storage

interface Storage<T, ID> {
    fun get(id: ID): T?
    fun store(id: ID, t: T)
    fun getAll(): List<T>
    fun delete(id: ID): Boolean
}