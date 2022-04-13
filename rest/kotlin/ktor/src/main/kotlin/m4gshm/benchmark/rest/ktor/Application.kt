package m4gshm.benchmark.rest.ktor

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import m4gshm.benchmark.storage.MemoryStorage
import m4gshm.benchmark.storage.Storage
import m4gshm.benchmark.storage.Task
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.slf4j.event.Level
import java.util.*

fun main(args: Array<String>) {
    val port = if (args.isEmpty()) 8080 else args[0].toInt()
    val host = "0.0.0.0"

    val app = startKoin {
        modules(module {
            single<Storage<Task, String>> { MemoryStorage() }
            single<ApplicationEngine> {
                embeddedServer(Netty, port = port, host = host) {
                    configure(get())
                }
            }
        })
    }

    val server: ApplicationEngine by app.koin.inject()
    server.start(wait = true)
}

fun Application.configure(storage: Storage<Task, String>) {
    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
        }
    }
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.application.environment.log.error("bad request", cause)
            call.respond(errorResponse(cause, BadRequest))
            call.response.status(BadRequest)
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("internal error", cause)
            call.respond(errorResponse(cause, InternalServerError))
            call.response.status(InternalServerError)
        }
    }
    routing {
        route("/task") {
            get {
                call.respond(storage.all)
            }
            get("/{id}") {
                val id = call.parameters["id"]
                val task = storage.get(id)
                if (task == null) {
                    call.response.status(NotFound)
                } else {
                    call.respond(task)
                }
            }
            post {
                val task = call.receive<Task>()
                var id = task.id
                if (id == null) {
                    id = UUID.randomUUID().toString()
                    task.id = id
                }
                storage.store(id, task)
                call.respond(OK)
            }
            put("/{id}") {
                val id = call.parameters["id"]
                val task = call.receive<Task>()
                if (task.id == null) {
                    task.id = id
                }
                storage.store(id, task)
                call.respond(OK)
            }
            delete("/{id}") {
                val id = call.parameters["id"]
                storage.delete(id)
                call.respond(OK)
            }
        }
    }
}

private val OK = Status(true)

private fun errorResponse(cause: Throwable, status: HttpStatusCode = InternalServerError) = mapOf(
    "code" to status.value,
    "message" to cause.message,
    "type" to cause::class.java.simpleName
)

data class Status(val success: Boolean)