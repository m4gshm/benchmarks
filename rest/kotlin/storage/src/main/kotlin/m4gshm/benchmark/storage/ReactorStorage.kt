package m4gshm.benchmark.storage

import reactor.core.publisher.Mono

interface ReactorStorage<T, ID> {
    fun get(id: ID): Mono<T>
    fun store(id: ID, entity: T): Mono<Void>
    fun getAll(): Mono<List<T>>
    fun delete(id: ID): Mono<Boolean>
}