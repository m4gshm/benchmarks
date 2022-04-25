package m4gshm.benchmark.rest.ktor

import m4gshm.benchmark.concurrency.IsolateStateMap
import m4gshm.benchmark.model.KotlinInstantTask
import m4gshm.benchmark.storage.MapStorage

fun main(args: Array<String>) {
    val options = Options("ktor-native", args)
    newServer(
        options.host, options.port, MapStorage(IsolateStateMap()), options.callGroupSize,
        options.connectionGroupSize, options.workerGroupSize, KotlinInstantTask::class
    ).start(wait = true)
}
