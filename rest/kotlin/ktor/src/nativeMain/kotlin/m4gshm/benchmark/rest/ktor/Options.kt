package m4gshm.benchmark.rest.ktor

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

class Options(appName: String, args: Array<String>) {

    private val parser = ArgParser(appName)

    val port by parser.option(ArgType.Int, description = "listening port").default(8080)
    val host by parser.option(ArgType.String, description = "listening host").default("0.0.0.0")
    val callGroupSize by parser.option(ArgType.Int)
    val connectionGroupSize by parser.option(ArgType.Int)
    val workerGroupSize by parser.option(ArgType.Int)

    init {
        parser.parse(args)
    }

}