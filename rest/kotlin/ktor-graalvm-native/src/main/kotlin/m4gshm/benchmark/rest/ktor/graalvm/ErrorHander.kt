package m4gshm.benchmark.rest.ktor.graalvm

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.datetime.IllegalTimeZoneException

fun Application.configErrorHandlers() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val status = when (cause) {
                is IllegalTimeZoneException,
                is BadRequestException -> HttpStatusCode.BadRequest
                else -> HttpStatusCode.InternalServerError
            }
            call.application.environment.log.error(status.description, cause)
            call.response.status(status)
            call.respond(errorResponse(cause, status))
            throw cause
        }
    }
}

private fun errorResponse(cause: Throwable, status: HttpStatusCode = HttpStatusCode.InternalServerError) = Status(
    success = false,
    status = status.value,
    message = cause.base().message,
    type = cause::class.simpleName
)

private fun Throwable.base(
    touched: MutableSet<Throwable> = mutableSetOf(this)
): Throwable = when (val b: Throwable? = this.cause) {
    null, this -> this
    else -> b.base(touched)
}
