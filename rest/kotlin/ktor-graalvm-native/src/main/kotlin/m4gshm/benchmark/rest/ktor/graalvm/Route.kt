package m4gshm.benchmark.rest.ktor.graalvm

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import io.ktor.util.reflect.*

inline fun <reified T : Task<D>, reified D> Application.configRoutes(storage: Storage<T, String>) {
    routing {
        route("/task") {
            get {
                getAllTasks(storage)
            }
            get("/{id}") {
                getTask<T, D>(storage)
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

suspend fun <T> PipelineContext<Unit, ApplicationCall>.deleteTask(
    storage: Storage<T, String>
) {
    val success = storage.delete(paramId())
    call.respond(if (success) OK else NOT_FOUND)
}

suspend inline fun <reified T : Task<D>, reified D> PipelineContext<Unit, ApplicationCall>.updateTask(
    storage: Storage<in T, String>
) {
    val id = paramId()
    var task = call.receive(T::class)
    if (task.id == null) {
        task = task.withId(id = id) as T
    }
    storage.store(id, task)
    call.respond(OK)
}

suspend inline fun <reified T : Task<D>, reified D> PipelineContext<Unit, ApplicationCall>.createTask(
    storage: Storage<in T, String>
) {
    var task = call.receive(T::class)
    var id = task.id
    if (id == null) {
        id = com.benasher44.uuid.uuid4().toString()
        task = task.withId(id = id) as T
    }
    storage.store(id, task)
    call.respond(OK)
}

suspend inline fun <reified T : Task<D>, reified D> PipelineContext<Unit, ApplicationCall>.getTask(
    storage: Storage<T, String>
) {
    val task: Task<D>? = storage.get(paramId())
    if (task == null) {
        call.response.status(HttpStatusCode.NotFound)
        call.respond(NOT_FOUND)
    } else {
        call.respond(task)
    }
}

suspend inline fun <reified T : Task<D>, reified D> PipelineContext<Unit, ApplicationCall>.getAllTasks(
    storage: Storage<T, String>
) {
    call.respond(storage.all)
}

fun PipelineContext<Unit, ApplicationCall>.paramId() =
    call.parameters["id"] ?: throw IllegalArgumentException("id is undefined")

val OK = Status(success = true)
val NOT_FOUND = Status(status = 404)

