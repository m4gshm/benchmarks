package m4gshm.benchmark.rest.ktor

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import m4gshm.benchmark.model.Task
import m4gshm.benchmark.storage.MapStorage
import m4gshm.benchmark.storage.Storage
import kotlin.reflect.KClass

fun <T: Task<D>, D> newServer(
    host: String,
    port: Int,
    storage: MapStorage<Task<D>, String>,
    kClass: KClass<T>
): ApplicationEngine {
    return embeddedServer(CIO, port = port, host = host) {
        configure(storage, kClass)
    }
}

private fun <T : Task<D>, D> Application.configure(storage: Storage<Task<D>, String>, kClass: KClass<T>) {
    install(ContentNegotiation) {
        json(Json { explicitNulls = false })
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val status = when (cause) {
                is kotlinx.datetime.IllegalTimeZoneException,
                is BadRequestException -> HttpStatusCode.BadRequest
                else -> HttpStatusCode.InternalServerError
            }
            call.application.environment.log.error(status.description, cause)
            call.response.status(status)
            call.respond(errorResponse(cause, status))
            throw cause
        }
    }
    routing {
        route("/task") {
            get {
                call.respond(storage.getAll())
            }
            get("/{id}") {
                val task: Task<D>? = storage.get(paramId())
                if (task == null) {
                    call.response.status(HttpStatusCode.NotFound)
                    call.respond(NOT_FOUND)
                } else {
                    call.respond(task)
                }
            }
            post {
                var task: Task<D> = call.receive(kClass)
                var id = task.id
                if (id == null) {
                    id = com.benasher44.uuid.uuid4().toString()
                    task = task.withId(id = id)
                }
                storage.store(id, task)
                call.respond(OK)
            }
            put("/{id}") {
                val id = paramId()
                var task : Task<D> = call.receive(kClass)
                if (task.id == null) {
                    task = task.withId(id = id)
                }
                storage.store(id, task)
                call.respond(OK)
            }
            delete("/{id}") {
                storage.delete(paramId())
                call.respond(OK)
            }
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.paramId() =
    call.parameters["id"] ?: throw IllegalArgumentException("id is undefined")

private val OK = Status(success = true)
private val NOT_FOUND = Status(status = 404)

private fun errorResponse(cause: Throwable, status: HttpStatusCode = HttpStatusCode.InternalServerError) = Status(
    success = false,
    status = status.value,
    message = cause.message,
    type = cause::class.simpleName
)

@Serializable
private data class Status(
    val success: Boolean? = null,
    val status: Int? = null,
    val message: String? = null,
    val type: String? = null
)