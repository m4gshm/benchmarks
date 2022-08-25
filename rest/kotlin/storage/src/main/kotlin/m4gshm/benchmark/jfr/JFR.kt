package m4gshm.benchmark.jfr

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import m4gshm.benchmark.rest.java.jft.StorageEvent

object JFR {
    fun <E> Uni<E>.rec(prefix: String, name: String) = newEvent(prefix, name).let {
        onSubscription().invoke { _ -> it.start() }.onTermination().invoke(Runnable { it.finish() })
    }

    fun <E> Multi<E>.rec(prefix: String, name: String): Multi<E> = newEvent(prefix, name).let {
        onSubscription().invoke { _ -> it.start() }.onTermination().invoke(Runnable { it.finish() })
    }

    private fun newEvent(prefix: String, name: String) = StorageEvent.create("$prefix.$name")
}