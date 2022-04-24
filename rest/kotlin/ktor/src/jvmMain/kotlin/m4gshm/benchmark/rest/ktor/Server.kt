package m4gshm.benchmark.rest.ktor

import com.benasher44.uuid.Uuid
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import m4gshm.benchmark.model.Task
import m4gshm.benchmark.options.Options.EngineType
import m4gshm.benchmark.options.Options.JsonType
import m4gshm.benchmark.storage.MapStorage
import m4gshm.benchmark.storage.Storage
import org.slf4j.event.Level
import kotlin.reflect.KClass

fun <T : Task<D>, D> newServer(
    host: String,
    port: Int,
    storage: MapStorage<in T, String>,
    engine: EngineType = EngineType.netty,
    json: JsonType = JsonType.kotlinx,
    typeInfo: KClass<T>
): ApplicationEngine {
    return embeddedServer(
        when (engine) {
            EngineType.netty -> Netty
            else -> CIO
        }, port = port, host = host
    ) {
        configure(storage, json, typeInfo)
    }
}

private fun <T : Task<D>, D> Application.configure(
    storage: Storage<in T, String>,
    jsonType: JsonType = JsonType.kotlinx,
    typeInfo: KClass<T>
) {
    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
    }
    install(ContentNegotiation) {
        when (jsonType) {
            JsonType.kotlinx -> {
                json(Json {
                    explicitNulls = false
                })
            }
            JsonType.jackson -> {
                jackson {
                    disable(WRITE_DATES_AS_TIMESTAMPS)
                    setDefaultPropertyInclusion(NON_NULL)
                    registerModule(JavaTimeModule())
                    registerModule(InstantModule())
                }
            }
        }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val status = when (cause) {
                is kotlinx.datetime.IllegalTimeZoneException,
                is BadRequestException -> HttpStatusCode.BadRequest
                else -> InternalServerError
            }
            call.application.environment.log.error(status.description, cause)
            call.response.status(status)
            call.respond(errorResponse(cause, status))
            throw cause
        }
    }
    routing(storage, typeInfo)
}

private fun <T : Task<D>, D> Application.routing(storage: Storage<in T, String>, typeInfo: KClass<T>) {
    routing {
        route("/task") {
            get {
                getAllTasks(storage)
            }
            get("/{id}") {
                getTask(storage)
            }
            post {
                createTask(storage, typeInfo)
            }
            put("/{id}") {
                updateTask(storage, typeInfo)
            }
            delete("/{id}") {
                deleteTask(storage)
            }
        }
    }
}

private suspend fun <T> PipelineContext<Unit, ApplicationCall>.deleteTask(
    storage: Storage<T, String>
) {
    storage.delete(paramId())
    call.respond(OK)
}

private suspend fun <T : Task<D>, D> PipelineContext<Unit, ApplicationCall>.updateTask(
    storage: Storage<in T, String>, typeInfo: KClass<T>
) {
    val id = paramId()
    var task = call.receive(typeInfo)
    if (task.id == null) {
        task = task.withId(id = id) as T
    }
    storage.store(id, task)
    call.respond(OK)
}

private suspend fun <T : Task<D>, D> PipelineContext<Unit, ApplicationCall>.createTask(
    storage: Storage<in T, String>, typeInfo: KClass<T>
) {
    var task = call.receive(typeInfo)
    var id = task.id
    if (id == null) {
        id = Uuid.randomUUID().toString()
        task = task.withId(id = id) as T
    }
    storage.store(id, task)
    call.respond(OK)
}

private suspend fun <T : Task<D>, D> PipelineContext<Unit, ApplicationCall>.getTask(storage: Storage<in T, String>) {
    val task: Any? = storage.get(paramId())
    if (task == null) {
        call.response.status(HttpStatusCode.NotFound)
        call.respond(NOT_FOUND)
    } else {
        call.respond(task)
    }
}

private suspend fun <T> PipelineContext<Unit, ApplicationCall>.getAllTasks(storage: Storage<T, String>) {
    call.respond(storage.getAll())
}

private fun PipelineContext<Unit, ApplicationCall>.paramId() =
    call.parameters["id"] ?: throw IllegalArgumentException("id is undefined")

private val OK = Status(success = true)
private val NOT_FOUND = Status(status = 404)

private fun errorResponse(cause: Throwable, status: HttpStatusCode = InternalServerError) = Status(
    success = false,
    status = status.value,
    message = cause.base().message,
    type = cause::class.java.simpleName
)

private fun Throwable.base(
    touched: MutableSet<Throwable> = mutableSetOf(this)
): Throwable = when (val b: Throwable? = this.cause) {
    null, this -> this
    else -> b.base(touched)
}

@Serializable
private data class Status(
    val success: Boolean? = null,
    val status: Int? = null,
    val message: String? = null,
    val type: String? = null
)