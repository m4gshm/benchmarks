import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodySubscribers
import java.nio.charset.Charset
import java.time.LocalDateTime


val springMvcBench = tasks.create("httpBenchmarkSpringMvc", Exec::class.java) {
    val project = ":rest:java:mvc"
    dependsOn("$project:bootJar")

    group = "benchmark"
    doNotTrackState("benchmark")

    val port = "8084"
    var process: Process? = null
    doFirst {

        val jar = project(project).tasks.getByName<Jar>("bootJar").archiveFile.get().asFile.absolutePath
        val p = Runtime.getRuntime().exec("java -Dserver.port=$port -jar $jar")
        checkRun("java server", p)
        process = p

        warmUp(port, 500)
    }
    doLast {
        kill(process)
    }

    commandLine("k6", "run", "--vus", "10", "--iterations", "20000", "-e", "SERVER_PORT=$port", "script.js")
}

val springWebfluxBench = tasks.create("httpBenchmarkSpringFebflux", Exec::class.java) {
    val project = ":rest:java:webflux"
    dependsOn("$project:bootJar")

    group = "benchmark"
    doNotTrackState("benchmark")

    val port = "8086"
    var process: Process? = null
    doFirst {

        val jar = project(project).tasks.getByName<Jar>("bootJar").archiveFile.get().asFile.absolutePath
        val p = Runtime.getRuntime().exec("java -Dserver.port=$port -jar $jar")
        checkRun("java server", p)
        process = p

        warmUp(port, 1000)
    }
    doLast {
        kill(process)
    }

    commandLine("k6", "run", "--vus", "10", "--iterations", "20000", "-e", "SERVER_PORT=$port", "script.js")
}

val goBench = tasks.create("httpBenchmarkGo", Exec::class.java) {
    group = "benchmark"
    doNotTrackState("benchmark")
    var process: Process? = null
    doFirst {

        val p = ProcessBuilder("go", "run", ".")
            .directory(File("../go"))
            .redirectError(File("go_err.txt"))
            .redirectOutput(File("go_out.txt"))
            .start()

//        val p = Runtime.getRuntime().exec("go run .", null, File("../go"))
        checkRun("go server", p)
        process = p
    }
    doLast {
        kill(process)
    }
    commandLine("k6", "run", "--vus", "10", "--iterations", "20000", "-e", "SERVER_PORT=8080", "script.js")
}

fun Task.checkRun(name: String, process: Process) {
    project.logger.warn("$name pid:" + process.pid())
    if (process.waitFor(500, TimeUnit.MILLISECONDS)) {
        val errorReader = process.errorReader()
        val runErrors = errorReader.readText()
        if (runErrors.isNotEmpty()) {
            throw IllegalStateException("$name error: $runErrors")
        }
    } else {
        Thread.sleep(3000)
        if (!process.isAlive) {
            val errorReader = process.errorReader()
            val runErrors = errorReader.readText()
            throw IllegalStateException("$name fail start failed: $runErrors")
        }
    }
}

tasks.create("benchmarks") {
    group = "benchmark"
    dependsOn(springMvcBench, springWebfluxBench, goBench)
}

fun Task.kill(process: Process?) {
    if (process == null) {
        return
    }
    destroyChildren(process.children())
    project.logger.warn("destroy server pid: " + process.pid())
    process.destroy()
    process.waitFor(10, TimeUnit.SECONDS)
}

fun destroyChildren(children: java.util.stream.Stream<ProcessHandle>) {
    children.forEach { c ->
        c.children().forEach { cc -> destroyChildren(cc.children()) }
        project.logger.warn("destroy children pid: " + c.pid())
        c.destroy()
    }
}

fun Task.warmUp(port: String, calls: Int) {
    val request = HttpRequest.newBuilder(URI.create("http://localhost:$port/task"))
        .POST(HttpRequest.BodyPublishers.ofString("{\"id\":\"warm\"}"))
        .header("Content-Type", "application/json")
        .build()

    project.logger.warn("warmup start in " + LocalDateTime.now())
    for (i in 0..calls) try {
        val response = HttpClient.newHttpClient()
            .send(request) { BodySubscribers.ofString(Charset.defaultCharset()) }
        if (response.statusCode() != 200) {
            project.logger.error("warmup error: " + response.body())
        }
    } catch (e: Exception) {
        project.logger.error("warmup error", e)
        break
    }
    project.logger.warn("warmup finish in " + LocalDateTime.now())
}