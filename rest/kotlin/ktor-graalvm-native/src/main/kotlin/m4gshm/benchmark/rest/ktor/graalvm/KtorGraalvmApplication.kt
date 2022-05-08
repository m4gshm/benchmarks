package m4gshm.benchmark.rest.ktor.graalvm


import kotlinx.datetime.Instant
import m4gshm.benchmark.rest.ktor.graalvm.Options.*
import m4gshm.benchmark.rest.ktor.graalvm.Options.DateType.java8
import m4gshm.benchmark.rest.ktor.graalvm.Options.DateType.kotlinx
import m4gshm.benchmark.rest.ktor.graalvm.Options.StorageType.map
import m4gshm.benchmark.rest.ktor.graalvm.Options.StorageType.state
import org.slf4j.event.Level
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap


fun main(args: Array<String>) {
    val options = Options("ktor", args)
    val host = options.host
    val port = options.port
    val storageType = options.storage
    val engine = options.engine
    val json = options.json
    val dateType = options.`date-type`
    val requestLogLevel = options.requestLogLevel
    when (dateType) {
        kotlinx -> newServer<KotlinInstantTask, Instant>(host, port, storageType, engine, json, requestLogLevel)
        java8 -> newServer<JavaOffsetDateTimeTask, OffsetDateTime>(
            host,
            port,
            storageType,
            engine,
            json,
            requestLogLevel
        )
    }.start(wait = true)
}

inline fun <reified T : Task<D>, reified D> newServer(
    host: String, port: Int, storageType: StorageType, engine: EngineType, json: JsonType, requestLogLevel: Level
) = newServer<T, D>(
    host = host, port = port, storage = MapStorage(
        when (storageType) {
            state -> IsolateStateMap()
            map -> ConcurrentHashMap(1024, 0.75f, Runtime.getRuntime().availableProcessors())
            else -> throw IllegalArgumentException("unsupported storage type $storageType")
        }
    ), engine = engine, json = json, requestLogLevel = requestLogLevel
)

