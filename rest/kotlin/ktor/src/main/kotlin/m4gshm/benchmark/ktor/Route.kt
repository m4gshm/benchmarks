package m4gshm.benchmark.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import m4gshm.benchmark.rest.java.jft.RestControllerEvent
import m4gshm.benchmark.rest.java.storage.model.Task
import kotlin.reflect.KClass

fun <T : Task<T, D>, D> Application.configRoutes(storage: Storage<T, String>, typeInfo: KClass<T>) = routing {
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

private suspend fun rec(name: String, block: suspend () -> Unit) =
    RestControllerEvent.start("Route.$name").use { block.invoke() }

private suspend fun <T> PipelineContext<Unit, ApplicationCall>.deleteTask(
    storage: Storage<T, String>
) = rec("deleteTask") {
    val success = storage.delete(paramId())
    call.respond(if (success) OK else NOT_FOUND)
}

private suspend fun <T : Task<T, D>, D> PipelineContext<Unit, ApplicationCall>.updateTask(
    storage: Storage<in T, String>, typeInfo: KClass<T>
) = rec("updateTask") {
    val id = paramId()
    val task = call.receive(typeInfo)
    storage.store(task.withId(id))
    call.respond(OK)
}

private suspend fun <T : Task<T, D>, D> PipelineContext<Unit, ApplicationCall>.createTask(
    storage: Storage<in T, String>, typeInfo: KClass<T>
) = rec("createTask") {
    var task = call.receive(typeInfo)
    var id = task.id
    if (id == null) {
        id = com.benasher44.uuid.uuid4().toString()
        task = task.withId(id)
    }
    storage.store(task)
    call.respond(OK)
}

private suspend fun <T : Task<T, D>, D> PipelineContext<Unit, ApplicationCall>.getTask(
    storage: Storage<T, String>
) = rec("getTask") {
    val task: Any? = storage.get(paramId())
    if (task == null) {
        call.response.status(HttpStatusCode.NotFound)
        call.respond(NOT_FOUND)
    } else {
        call.respond(task)
    }
}


private suspend fun <T : Task<T, D>, D> PipelineContext<Unit, ApplicationCall>.getAllTasks(
    storage: Storage<T, String>
) = rec("getAllTasks") {
    val message: List<Any> = storage.getAll()
    call.respond(message)
}

private fun PipelineContext<Unit, ApplicationCall>.paramId() =
    call.parameters["id"] ?: throw IllegalArgumentException("id is undefined")

private val OK = Status(success = true)
private val NOT_FOUND = Status(status = 404)
