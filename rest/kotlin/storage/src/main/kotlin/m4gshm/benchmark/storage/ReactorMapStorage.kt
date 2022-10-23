package m4gshm.benchmark.storage

import m4gshm.benchmark.jfr.JFR.rec
import m4gshm.benchmark.rest.java.storage.ReactorStorage
import m4gshm.benchmark.rest.java.storage.model.IdAware
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.fromCallable


class ReactorMapStorage<T : IdAware<ID>, ID : Any>(private val storage: MutableMap<ID, T>) : ReactorStorage<T, ID> {

    override fun get(id: ID): Mono<T> = fromCallable { storage[id]!! }.rec(prefix, "get")

    override fun store(entity: T): Mono<T> = fromCallable {
        storage[entity.id] = entity
        entity
    }.rec(prefix, "store")

    override fun getAll(): Flux<T> =
        fromCallable { storage.values.toList() }.flatMapIterable { it }.rec(prefix, "getAll")

    override fun delete(id: ID): Mono<Boolean> = fromCallable { storage.remove(id) != null }.rec(prefix, "delete")

    private val prefix = this::class.java.name
}
