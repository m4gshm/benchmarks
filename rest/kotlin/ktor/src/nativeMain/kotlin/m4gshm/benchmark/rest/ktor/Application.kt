package m4gshm.benchmark.rest.ktor

import m4gshm.benchmark.concurrency.IsolateStateMap
import m4gshm.benchmark.model.KotlinInstantTask
import m4gshm.benchmark.storage.MapStorage

fun main(args: Array<String>) {
    val port = if (args.isEmpty()) 8080 else args[0].toInt()
    val host = "0.0.0.0"

    newServer(host, port, MapStorage(IsolateStateMap()), KotlinInstantTask::class).start(wait = true)
}
