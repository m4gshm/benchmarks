package m4gshm.benchmark.jfr.reactor

import m4gshm.benchmark.rest.java.jft.StorageEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object JFR {

    fun <E> Mono<E>.rec(prefix: String, name: String): Mono<E> {
        val event = newEvent(prefix, name)
        return doOnSubscribe { event.start() }.doOnTerminate { event.finish() }
    }

    fun <E> Flux<E>.rec(prefix: String, name: String): Flux<E> {
        val event = newEvent(prefix, name)
        return doOnSubscribe { event.start() }.doOnTerminate { event.finish() }
    }

    private fun newEvent(prefix: String, name: String) = StorageEvent.create("$prefix.$name")
}