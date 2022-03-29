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

    commandLine("k6", "run", "--vus", "100", "--iterations", "20000", "-e", "SERVER_PORT=$port", "script.js")
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

        warmUp(port, 2000)
    }
    doLast {
        kill(process)
    }

    commandLine("k6", "run", "--vus", "100", "--iterations", "20000", "-e", "SERVER_PORT=$port", "script.js")
}

val goBench = tasks.create("httpBenchmarkGo", Exec::class.java) {
    val port = "8080"

    group = "benchmark"
    doNotTrackState("benchmark")
    var process: Process? = null
    doFirst {
        val workDir = File(project.projectDir, "../go")
        val p = ProcessBuilder("go", "run", ".")
            .directory(workDir)
            .redirectError(File(workDir, "go_err.txt"))
            .start()

        checkRun("go server", p)
        process = p

        warmUp(port, 500)
    }
    doLast {
        kill(process)
    }

    commandLine("k6", "run", "--vus", "100", "--iterations", "20000", "-e", "SERVER_PORT=$port", "script.js")
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
            val runText = process.inputReader().readText()
            project.logger.warn(runText)
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
    destroy(process.toHandle())
    process.waitFor(10, TimeUnit.SECONDS)
}

fun destroy(process: ProcessHandle?) {
    if (process == null) {
        return
    }
    process.descendants().forEach { c ->
        project.logger.warn("destroy children pid: " + c.pid() + ", cmd:" + c.info().command().orElse(""))
        c.destroy()
    }
    project.logger.warn("destroy main pid: " + process.pid() + ", cmd:" + process.info().command().orElse(""))
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
            project.logger.error("warmup error status: " + response.statusCode() + ", body:" + response.body())
        }
    } catch (e: Exception) {
        project.logger.error("warmup error", e)
        break
    }
    project.logger.warn("warmup finish in " + LocalDateTime.now())
}