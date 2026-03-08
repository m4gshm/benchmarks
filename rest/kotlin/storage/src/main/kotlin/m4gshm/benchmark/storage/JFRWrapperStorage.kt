package m4gshm.benchmark.storage

import m4gshm.benchmark.jfr.reactor.JFR.rec
import m4gshm.benchmark.rest.java.storage.ReactorStorage
import m4gshm.benchmark.rest.java.storage.model.IdAware
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


class JFRWrapperStorage<T : IdAware<ID>, ID : Any>(private val storage: ReactorStorage<T, ID>) : ReactorStorage<T, ID> {

    override fun get(id: ID): Mono<T> = storage.get(id).rec(prefix, "get")

    override fun store(entity: T): Mono<T> = storage.store(entity).rec(prefix, "store")

    override fun getAll(): Flux<T> = storage.all.rec(prefix, "getAll")

    override fun delete(id: ID): Mono<Boolean> = storage.delete(id).rec(prefix, "delete")

    private val prefix = storage::class.java.name
}
