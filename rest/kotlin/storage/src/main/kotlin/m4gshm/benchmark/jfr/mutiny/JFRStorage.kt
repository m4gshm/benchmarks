package m4gshm.benchmark.jfr.mutiny

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import m4gshm.benchmark.rest.java.jfr.BaseEvent

object JFRStorage {

    fun <E> Uni<E>.rec(
        prefix: String, name: String, factory: (String) -> BaseEvent
    ) = newEvent(prefix, name, factory).let {
        onSubscription().invoke { _ -> it.start() }.onTermination().invoke(Runnable { it.finish() })
    }

    fun <E> Multi<E>.rec(
        prefix: String, name: String, factory: (String) -> BaseEvent
    ) = newEvent(prefix, name, factory).let {
        onSubscription().invoke { _ -> it.start() }.onTermination().invoke(Runnable { it.finish() })
    }

    private fun newEvent(prefix: String, name: String, factory: (String) -> BaseEvent) =
        factory.invoke("$prefix.$name")

}