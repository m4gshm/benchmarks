package m4gshm.benchmark.rest.ktor

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import m4gshm.benchmark.ktor.configErrorHandlers
import m4gshm.benchmark.ktor.configRoutes
import m4gshm.benchmark.rest.java.storage.model.Task
import m4gshm.benchmark.rest.ktor.Options.EngineType
import m4gshm.benchmark.rest.ktor.Options.EngineType.netty
import m4gshm.benchmark.rest.ktor.Options.JsonType
import m4gshm.benchmark.rest.ktor.Options.JsonType.kotlinx
import m4gshm.benchmark.storage.MapStorage
import org.slf4j.event.Level
import kotlin.reflect.KClass

fun <T : Task<T, D>, D> newServer(
    host: String,
    port: Int,
    storage: MapStorage<T, String>,
    engine: EngineType = netty,
    json: JsonType = kotlinx,
    typeInfo: KClass<T>,
    requestLogLevel: Level
): ApplicationEngine {
    return embeddedServer(
        when (engine) {
            netty -> Netty
            else -> CIO
        }, port = port, host = host
    ) {
        configure(storage, json, typeInfo, requestLogLevel)
    }
}

private fun <T : Task<T, D>, D> Application.configure(
    storage: Storage<T, String>,
    jsonType: JsonType = kotlinx,
    typeInfo: KClass<T>,
    requestLogLevel: Level
) {
    install(CallLogging) {
        level = requestLogLevel
        filter { call -> call.request.path().startsWith("/") }
    }
    install(ContentNegotiation) {
        when (jsonType) {
            kotlinx -> json(Json { explicitNulls = false })
            JsonType.jackson -> jackson {
                disable(WRITE_DATES_AS_TIMESTAMPS)
                setDefaultPropertyInclusion(NON_NULL)
                registerModule(JavaTimeModule())
                registerModule(InstantModule())
            }
        }
    }
    configErrorHandlers()
    configRoutes(storage, typeInfo)
}


