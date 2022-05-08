package m4gshm.benchmark.rest.ktor

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.slf4j.event.Level
import org.slf4j.event.Level.DEBUG

class Options(appName: String, args: Array<String>) {

    private val parser = ArgParser(appName)

    val port by parser.option(ArgType.Int, description = "listening port").default(8080)
    val host by parser.option(ArgType.String, description = "listening host").default("0.0.0.0")

    val storage by parser.option(
        ArgType.Choice(StorageType.values().asList(), { StorageType.valueOf(it) }), description = "storage type"
    ).default(StorageType.map)
    val json by parser.option(
        ArgType.Choice(JsonType.values().asList(), { JsonType.valueOf(it) }), description = "json engine type"
    ).default(JsonType.kotlinx)
    val engine: EngineType by parser.option(
        ArgType.Choice(EngineType.values().asList(), { EngineType.valueOf(it) }),
        description = "ktor http server engine type"
    ).default(EngineType.netty)
    val `date-type`: DateType by parser.option(
        ArgType.Choice(DateType.values().asList(), { DateType.valueOf(it) }),
        description = "date engine type"
    ).default(DateType.kotlinx)

    val requestLogLevel by parser.option(
        ArgType.Choice(Level.values().asList(), { Level.valueOf(it) }),
        description = "http request log level"
    ).default(DEBUG)

    init {
        parser.parse(args)
    }

    enum class StorageType {
        map, state
    }

    enum class EngineType {
        netty, cio
    }

    enum class JsonType {
        kotlinx, jackson
    }

    enum class DateType {
        kotlinx, java8
    }
}