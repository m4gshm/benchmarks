package m4gshm.benchmark.rest.ktor

import com.benasher44.uuid.Uuid
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
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
import m4gshm.benchmark.storage.MapStorage
import m4gshm.benchmark.storage.Storage
import org.slf4j.event.Level
import java.util.concurrent.ConcurrentHashMap


fun newServer(host: String, port: Int): ApplicationEngine {
    val storage = MapStorage<Task, String>(
        ConcurrentHashMap(
            1024, 0.75f,
            Runtime.getRuntime().availableProcessors()
        )
    )
    return embeddedServer(Netty, port = port, host = host, configure = {
//        this.callGroupSize = parallelism
//        this.connectionGroupSize = 1
//        this.workerGroupSize = 1
//        this.requestQueueLimit = requestQueueLimit * 2
//        this.runningLimit = runningLimit * 2
        tcpKeepAlive = true
    }) {
        configure(storage)
    }
}

private fun Application.configure(storage: Storage<Task, String>) {
    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
    }
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
        })
//        jackson {
//            enable(SerializationFeature.INDENT_OUTPUT)
//            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//            registerModule(JavaTimeModule())
//        }
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
    routing(storage)
}

private fun Application.routing(storage: Storage<Task, String>) {
    routing {
        route("/task") {
            get {
                getAllTasks(storage)
            }
            get("/{id}") {
                getTask(storage)
            }
            post {
                createTask(storage)
            }
            put("/{id}") {
                updateTask(storage)
            }
            delete("/{id}") {
                deleteTask(storage)
            }
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.deleteTask(
    storage: Storage<Task, String>
) {
    storage.delete(paramId())
    call.respond(OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.updateTask(
    storage: Storage<Task, String>
) {
    val id = paramId()
    var task = call.receive<Task>()
    if (task.id == null) {
        task = task.copy(id = id)
    }
    storage.store(id, task)
    call.respond(OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.createTask(
    storage: Storage<Task, String>
) {
    var task = call.receive<Task>()
    var id = task.id
    if (id == null) {
        id = Uuid.randomUUID().toString()
        task = task.copy(id = id)
    }
    storage.store(id, task)
    call.respond(OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getTask(
    storage: Storage<Task, String>
) {
    val task = storage.get(paramId())
    if (task == null) {
        call.response.status(HttpStatusCode.NotFound)
        call.respond(NOT_FOUND)
    } else {
        call.respond(task)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getAllTasks(
    storage: Storage<Task, String>
) {
    call.respond(storage.getAll())
}

private fun PipelineContext<Unit, ApplicationCall>.paramId() =
    call.parameters["id"] ?: throw IllegalArgumentException("id is undefined")

private val OK = Status(success = true)
private val NOT_FOUND = Status(status = 404)

private fun errorResponse(cause: Throwable, status: HttpStatusCode = HttpStatusCode.InternalServerError) = Status(
    success = false,
    status = status.value,
    message = cause.message,
    type = cause::class.java.simpleName
)

@Serializable
private data class Status(
    val success: Boolean? = null,
    val status: Int? = null,
    val message: String? = null,
    val type: String? = null
)