import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodySubscribers
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


val springMvcBench = tasks.create("httpBenchmarkSpringMvc", Exec::class.java) {
    val buildJarTask = "bootJar"
    val project = ":rest:java:mvc"
    dependsOn("$project:$buildJarTask")

    group = "benchmark"
    doNotTrackState("benchmark")

    val port = "8084"
    var process: Process? = null
    doFirst {
        try {
            val jar = project(project).tasks.getByName<Jar>(buildJarTask).archiveFile.get().asFile.absolutePath
            val p = Runtime.getRuntime().exec("java -Dserver.port=$port -jar $jar")
            checkRun("java server", p)
            process = p

            warmUp(p, port, 100000)
        } catch (e: Exception) {
            kill(process)
            throw e
        }
    }
    doLast {
        kill(process)
    }
    setupCmd(port)
}


val ktorBench = tasks.create("httpBenchmarkKtor", Exec::class.java) {
    val buildJarTask = "shadowJar"
    val project = ":rest:kotlin:ktor"
    dependsOn("$project:$buildJarTask")

    group = "benchmark"
    doNotTrackState("benchmark")

    val port = "8088"
    var process: Process? = null
    doFirst {
        try {
            val jar = project(project).tasks.getByName<Jar>(buildJarTask).archiveFile.get().asFile.absolutePath
            val p = Runtime.getRuntime().exec("java -jar $jar $port")
            checkRun("kotlin ktor server", p)
            process = p

            warmUp(p, port, 100000)
        } catch (e: Exception) {
            kill(process)
            throw e
        }
    }
    doLast {
        kill(process)
    }
    setupCmd(port)
}

val springWebfluxBench = tasks.create("httpBenchmarkSpringWebflux", Exec::class.java) {
    val buildJarTask = "bootJar"
    val project = ":rest:java:webflux"
    dependsOn("$project:$buildJarTask")

    group = "benchmark"
    doNotTrackState("benchmark")

    val port = "8086"
    var process: Process? = null
    doFirst {
        try {
            val jar = project(project).tasks.getByName<Jar>(buildJarTask).archiveFile.get().asFile.absolutePath

            val p = ProcessBuilder("java", "-Dserver.port=$port", "-jar", "$jar")
                .redirectError(File(this.project.buildDir, "error.txt"))
                .redirectOutput(File(this.project.buildDir, "output.txt"))
                .start()

            checkRun("java server", p)
            process = p

            warmUp(p, port, 100000)
        } catch (e: Exception) {
            this.project.logger.error("kill process by error ", e)
            kill(process)
            throw e
        }
    }
    doLast {
        kill(process)
    }
    setupCmd(port)
}

val springWebfluxNativeBench = tasks.create("httpBenchmarkSpringWebfluxNative", Exec::class.java) {
    val buildTask = "nativeCompile"
    val projectName = "webflux-native"
    val project = ":rest:java:$projectName"
    dependsOn("$project:$buildTask")

    group = "benchmark"
    doNotTrackState("benchmark")

    // lsof -t -i :8087
    val port = "8087"
    var process: Process? = null
    doFirst {
        try {
            val webfluxNativeProject = project(project)

            val isWin = org.gradle.internal.os.OperatingSystem.current().isWindows
            val workDir = File(webfluxNativeProject.buildDir, "native/nativeCompile")
            val execFileName = if (isWin) File(workDir, "${projectName}.exe").absolutePath else "./$projectName"
            val p = ProcessBuilder(execFileName, "-Dserver.port=$port").directory(workDir)
                .start()
            checkRun("native java server", p)
            process = p

            warmUp(p, port, 100000)
        } catch (e: Exception) {
            kill(process)
            throw e
        }
    }
    doLast {
        kill(process)
    }
    setupCmd(port)
}

val goBench = tasks.create("httpBenchmarkGo", Exec::class.java) {
    val port = "8080"

    group = "benchmark"
    doNotTrackState("benchmark")
    var process: Process? = null
    doFirst {
        try {
            val workDir = File(project.projectDir, "../go")
            val p = ProcessBuilder("go", "run", ".").directory(workDir)
                .start()

            checkRun("go server", p)
            process = p

            warmUp(p, port, 100000)
        } catch (e: Exception) {
            kill(process)
            throw e
        }
    }
    doLast {
        kill(process)
    }
    setupCmd(port)
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
    dependsOn(springMvcBench, springWebfluxBench, springWebfluxNativeBench, goBench)
}

fun kill(process: Process?) {
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
    process.destroy()
    project.logger.warn("destroy main pid: " + process.pid() + ", cmd:" + process.info().command().orElse(""))
}

fun warmUp(p: Process, port: String, calls: Int, threads: Int = 50) {
    val request = HttpRequest.newBuilder(URI.create("http://localhost:$port/task"))
        .version(HttpClient.Version.HTTP_1_1)
        .POST(HttpRequest.BodyPublishers.ofString("{\"id\":\"warm\"}")).header("Content-Type", "application/json")
        .build()

    project.logger.warn("warmup process {} start in {}", p.pid(), LocalDateTime.now())
    val executorService = Executors.newFixedThreadPool(threads) {
        Thread(it).apply {
            isDaemon = true
        }
    }
    val httpClient = HttpClient.newHttpClient()
    val errorCount = AtomicInteger()
    var c = 0
    val routines = ArrayList<java.util.concurrent.Future<*>>()
    while (c++ < calls) {
        try {
            val response = httpClient.send(request) { BodySubscribers.ofString(Charset.defaultCharset()) }
            if (response.statusCode() != 200) {
                project.logger.error("warmup error status: " + response.statusCode() + ", body:" + response.body())
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            project.logger.error("warmup error {}:{}", e::class.java.simpleName, e.message)
            Thread.sleep(1000)
        }
    }
    //concurrently
    while (c++ < calls) {
        val maxErrors = 10
        if (errorCount.get() >= maxErrors) break
        routines += executorService.submit {
            if (errorCount.get() < maxErrors) try {
                val response = httpClient.send(request) { BodySubscribers.ofString(Charset.defaultCharset()) }
                if (response.statusCode() != 200) {
                    project.logger.error("warmup error status: " + response.statusCode() + ", body:" + response.body())
                }
            } catch (e: Exception) {
                val i = errorCount.incrementAndGet()
                project.logger.error("warmup error {}", i, e)
                if (i >= maxErrors) {
                    throw e
                }
            }
        }
    }
    routines.forEach {
        it.get()
    }
    executorService.shutdown()

    project.logger.warn("warmup finish in " + LocalDateTime.now())
}

fun Exec.setupCmd(port: String) {
    commandLine("k6", "run", "--vus", "6", "--iterations", "60000", "-e", "SERVER_PORT=$port", "script.js")
    doFirst {
        standardOutput = File(project.buildDir, "result-" + this.name + ".txt").outputStream()
        project.logger.warn("bench start in {}", LocalDateTime.now())
    }
    doLast {
        project.logger.warn("bench finish in " + LocalDateTime.now())
    }
}