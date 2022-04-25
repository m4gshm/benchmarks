package m4gshm.benchmark.rest.ktor

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import m4gshm.benchmark.ktor.configErrorHandlers
import m4gshm.benchmark.ktor.configRoutes
import m4gshm.benchmark.model.Task
import m4gshm.benchmark.storage.MapStorage
import m4gshm.benchmark.storage.Storage
import kotlin.reflect.KClass

fun <T : Task<D>, D> newServer(
    host: String,
    port: Int,
    storage: MapStorage<T, String>,
    callGroupSize: Int?,
    connectionGroupSize: Int?,
    workerGroupSize: Int?,
    kClass: KClass<T>,
): ApplicationEngine {
    return embeddedServer(CIO, port = port, host = host, configure = {
        this.callGroupSize = callGroupSize ?: this.callGroupSize
        this.connectionGroupSize = connectionGroupSize ?: this.connectionGroupSize
        this.workerGroupSize = workerGroupSize ?: this.workerGroupSize

    }) {
        configure(storage, kClass)
    }
}

private fun <T : Task<D>, D> Application.configure(storage: Storage<T, String>, typeInfo: KClass<T>) {
    install(ContentNegotiation) {
        json(Json { explicitNulls = false })
    }
    configErrorHandlers()
    configRoutes(storage, typeInfo)
}


