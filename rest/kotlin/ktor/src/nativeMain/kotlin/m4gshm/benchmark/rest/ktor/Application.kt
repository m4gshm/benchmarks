package m4gshm.benchmark.rest.ktor

fun main(args: Array<String>) {
    val port = if (args.isEmpty()) 8080 else args[0].toInt()
    val host = "0.0.0.0"

    newServer(host, port).start(wait = true)
}
