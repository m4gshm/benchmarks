package m4gshm.benchmark.rest.ktor


import m4gshm.benchmark.model.KotlinInstantTaskIml
import m4gshm.benchmark.rest.java.storage.model.jpa.TaskEntity
import m4gshm.benchmark.rest.ktor.Options.DateType.java8
import m4gshm.benchmark.rest.ktor.Options.DateType.kotlinx
import m4gshm.benchmark.storage.MapStorage
import java.util.concurrent.ConcurrentHashMap


fun main(args: Array<String>) {
    val options = Options("ktor", args)
    val host = options.host
    val port = options.port
    val engine = options.engine
    val json = options.json
    val dateType = options.`date-type`
    val requestLogLevel = options.requestLogLevel
    when (dateType) {
        kotlinx -> newServer(
            host, port, MapStorage(ConcurrentHashMap()), engine, json, KotlinInstantTaskIml::class, requestLogLevel
        )
        java8 -> newServer(host, port, MapStorage(ConcurrentHashMap()), engine, json, TaskEntity::class, requestLogLevel)
    }.start(wait = true)
}
