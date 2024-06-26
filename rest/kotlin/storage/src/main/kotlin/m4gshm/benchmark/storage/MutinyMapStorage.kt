package m4gshm.benchmark.storage

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.Uni.createFrom
import m4gshm.benchmark.jfr.mutiny.JFRStorage.rec
import m4gshm.benchmark.rest.java.jfr.StorageEvent
import m4gshm.benchmark.rest.java.storage.MutinyStorage
import m4gshm.benchmark.rest.java.storage.model.IdAware
import java.util.Optional.ofNullable


class MutinyMapStorage<T : IdAware<ID>, ID : Any>(private val storage: MutableMap<ID, T>) : MutinyStorage<T, ID> {

    override fun get(id: ID): Uni<T> = createFrom().optional {
        ofNullable(storage[id])
    }.rec(prefix, "get", StorageEvent::create)

    override fun store(entity: T): Uni<T> = createFrom().item {
        entity.apply { storage[this.id] = this }
    }.rec(prefix, "store", StorageEvent::create)

    override fun getAll(): Uni<List<T>> = createFrom().item {
        storage.values.toList()
    }.rec(prefix, "getAll", StorageEvent::create)

    override fun delete(id: ID): Uni<Boolean> = createFrom().item {
        storage.remove(id) != null
    }.rec(prefix, "delete", StorageEvent::create)

    private val prefix = this::class.java.name
}
