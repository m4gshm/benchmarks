package m4gshm.benchmark.rest.ktor

import m4gshm.benchmark.concurrency.IsolateStateMap
import m4gshm.benchmark.model.JavaOffsetDateTimeTask
import m4gshm.benchmark.model.KotlinInstantTask
import m4gshm.benchmark.model.Task
import m4gshm.benchmark.rest.ktor.Options.*
import m4gshm.benchmark.rest.ktor.Options.DateType.java8
import m4gshm.benchmark.rest.ktor.Options.DateType.kotlinx
import m4gshm.benchmark.rest.ktor.Options.StorageType.map
import m4gshm.benchmark.rest.ktor.Options.StorageType.state
import m4gshm.benchmark.storage.MapStorage
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


fun main(args: Array<String>) {
    val options = Options("ktor", args)
    val host = options.host
    val port = options.port
    val storageType = options.storage
    val engine = options.engine
    val json = options.json
    val dateType = options.`date-type`

    when (dateType) {
        kotlinx -> newServer(host, port, storageType, engine, json, KotlinInstantTask::class)
        java8 -> newServer(host, port, storageType, engine, json, JavaOffsetDateTimeTask::class)
    }.start(wait = true)
}

fun <T : Task<D>, D> newServer(
    host: String, port: Int, storageType: StorageType, engine: EngineType, json: JsonType, typeInfo: KClass<T>
) = newServer(
    host = host, port = port, storage = MapStorage(
        when (storageType) {
            state -> IsolateStateMap()
            map -> ConcurrentHashMap(1024, 0.75f, Runtime.getRuntime().availableProcessors())
            else -> throw IllegalArgumentException("unsupported storage type $storageType")
        }
    ), engine = engine, json = json, typeInfo = typeInfo
)

