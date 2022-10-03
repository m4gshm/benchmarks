import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodySubscribers
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder


val warmUpThread = Runtime.getRuntime().availableProcessors()
val warmUpAmountPerThread = 10_000
val warmUpAmounts = warmUpAmountPerThread * warmUpThread
val warmUpNativeAmounts = warmUpAmounts

val callUsers = Runtime.getRuntime().availableProcessors() * 10
val callsPerUser = 2000

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

            warmUp(p, port, warmUpAmounts)
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


val ktorBench =
    ktorExec("httpBenchmarkKtor", "8088", storage = "map", jsonEngine = "kotlinx", dateType = "kotlinx")
ktorExec("httpBenchmarkKtorState", "8088", storage = "state", jsonEngine = "kotlinx", dateType = "kotlinx")
ktorExec("httpBenchmarkKtorDateJava8", "8088", storage = "map", jsonEngine = "kotlinx", dateType = "java8")
ktorExec("httpBenchmarkKtorJackson", "8089", storage = "map", jsonEngine = "jackson", dateType = "kotlinx")
ktorExec("httpBenchmarkKtorJacksonDateJava8", "8089", storage = "map", jsonEngine = "jackson", dateType = "java8")

fun ktorExec(name: String, port: String, storage: String, jsonEngine: String, dateType: String) = tasks.create(
    name, Exec::class.java
) {
    val buildJarTask = "shadowJar"
    val project = ":rest:kotlin:ktor"
    dependsOn("$project:$buildJarTask")

    group = "benchmark"
    doNotTrackState("benchmark")
    var process: Process? = null
    doFirst {
        try {
            val jar = project(project).tasks.getByName<Jar>(buildJarTask).archiveFile.get().asFile.absolutePath
            val p = Runtime.getRuntime().exec(
                "java -jar $jar --port $port --storage $storage " +
                        "--json $jsonEngine --date-type $dateType"
            )
            checkRun("kotlin ktor server", p)
            process = p

            warmUp(p, port, warmUpAmounts)
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

tasks.create("httpBenchmarkKtorNative", Exec::class.java) {
    val buildTask = "linkReleaseExecutableNative"
    val projectName = ":rest:kotlin:ktor"
    dependsOn("$projectName:$buildTask")

    group = "benchmark"
    doNotTrackState("benchmark")

    // lsof -t -i :8087
    val port = "8090"
    var process: Process? = null
    doFirst {
        try {
            val project = project(projectName)

            val isWin = org.gradle.internal.os.OperatingSystem.current().isWindows
            val workDir = File(project.buildDir, "bin/native/releaseExecutable")

            val callGroupSize = 300
            val connectionGroupSize = 50
            val workerGroupSize = 50

            var args = listOf(
                "./ktor.kexe", "--port", "$port", "--callGroupSize", "$callGroupSize",
                "--connectionGroupSize", "$connectionGroupSize", "--workerGroupSize", "$workerGroupSize"
            )
            if (isWin) {
                args = listOf("wsl") + args
            }
            val p = ProcessBuilder(args).directory(workDir)
//                .redirectError(File(this.project.buildDir, "error.txt"))
//                .redirectOutput(File(this.project.buildDir, "output.txt"))
                .start()
            checkRun("native ktor server", p)
            process = p

            warmUp(p, port, warmUpNativeAmounts)
//            warmUp(null, port, warmUpAmounts)
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

tasks.create("httpBenchmarkKtorGraalvmNative", Exec::class.java) {
    val buildTask = "nativeCompile"
    val projectName = "ktor-graalvm-native"
    val fullProjectName = ":rest:kotlin:$projectName"
    dependsOn("$fullProjectName:$buildTask")

    group = "benchmark"
    doNotTrackState("benchmark")

    // lsof -t -i :8087
    val port = "8091"
    var process: Process? = null
    doFirst {
        try {
            val project = project(fullProjectName)
            val workDir = File(project.buildDir, "native/nativeCompile")

            val isWin = org.gradle.internal.os.OperatingSystem.current().isWindows
            val execFileName = if (isWin) File(workDir, "${projectName}.exe").absolutePath else "./$projectName"

            var args = listOf(
                execFileName, "--port", "$port"
            )

            val p = ProcessBuilder(args).directory(workDir)
//                .redirectError(File(this.project.buildDir, "error.txt"))
//                .redirectOutput(File(this.project.buildDir, "output.txt"))
                .start()
            checkRun("native ktor server", p)
            process = p

            warmUp(p, port, warmUpNativeAmounts)
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

val quarkusBench = quarkusExec("httpBenchmarkQuarkus")
quarkusExec("httpBenchmarkQuarkusDB")

val quarkusNativeBench = tasks.create("httpBenchmarkQuarkusNative", Exec::class.java) {
    val buildJarTask = "buildNative"
    val project = ":rest:java:quarkus"
    dependsOn("$project:$buildJarTask")//.project.extensions.extraProperties.set("quarkus.package.type", "native")

    group = "benchmark"
    doNotTrackState("benchmark")

    val port = "8092"
    var process: Process? = null
    doFirst {
        try {
            val workDir = project(project).buildDir
            val isWin = org.gradle.internal.os.OperatingSystem.current().isWindows
            val execFileName = if (isWin) File(workDir, "quarkus-runner.exe").absolutePath else "quarkus-runner"

            var args = listOf(
                execFileName, "-Dquarkus.http.port=$port", "-Dquarkus.log.console.enable=false"
            )
            val p = ProcessBuilder(args).directory(workDir)
//                .redirectError(File(this.project.buildDir, "error.txt"))
//                .redirectOutput(File(this.project.buildDir, "output.txt"))
                .start()

            checkRun("quarkus native server", p)
            process = p

            warmUp(p, port, warmUpAmounts)
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
//                .redirectError(File(this.project.buildDir, "error.txt"))
//                .redirectOutput(File(this.project.buildDir, "output.txt"))
                .start()

            checkRun("java server", p)
            process = p

            warmUp(p, port, warmUpAmounts)
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

tasks.create("httpBenchmarkSpringWebfluxNative", Exec::class.java) {
    val buildTask = "nativeCompile"
    val projectName = "webflux-native"
    val project = ":rest:java:$projectName"
    if (!this.project.hasProperty("nobuild")) {
        dependsOn("$project:$buildTask")
    }

    group = "benchmark"
    doNotTrackState("benchmark")

    // lsof -t -i :8087
    val port = "8087"
    var process: Process? = null
    doFirst {
        try {
            val webfluxNativeProject = project(project)

            val workDir = File(webfluxNativeProject.buildDir, "native/nativeCompile")
            val isWin = org.gradle.internal.os.OperatingSystem.current().isWindows
            val execFileName = if (isWin) File(workDir, "${projectName}.exe").absolutePath else "./$projectName"
            val p = ProcessBuilder(execFileName, "-Dserver.port=$port").directory(workDir)
                .start()
            checkRun("native java server", p)
            process = p

            warmUp(p, port, warmUpNativeAmounts)
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
            val p = ProcessBuilder("go", "run", ".").directory(workDir).start()

            checkRun("go server", p)
            process = p

            warmUp(p, port, warmUpNativeAmounts)
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
    dependsOn(springMvcBench, springWebfluxBench, ktorBench, goBench, quarkusBench)
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

fun warmUp(p: Process?, port: String, calls: Int, threads: Int = warmUpThread) {
    project.logger.warn("warmup process {} start in {}", p?.pid(), LocalDateTime.now())

    val baseUrlPath = "http://localhost:$port/task"
    val taskId = "warm"
    val idUrlPath = "$baseUrlPath/$taskId"

    val createRequest = HttpRequest
        .newBuilder(URI.create(baseUrlPath))
        .version(HttpClient.Version.HTTP_1_1)
        .POST(HttpRequest.BodyPublishers.ofString("{\"id\":\"$taskId\"}")).header("Content-Type", "application/json")
        .build()
    val getAllRequest = HttpRequest
        .newBuilder(URI.create(baseUrlPath))
        .version(HttpClient.Version.HTTP_1_1)
        .GET()
        .build()
    val getRequest = HttpRequest
        .newBuilder(URI.create(idUrlPath))
        .version(HttpClient.Version.HTTP_1_1)
        .GET()
        .build()
    val deleteRequest = HttpRequest
        .newBuilder(URI.create(idUrlPath))
        .version(HttpClient.Version.HTTP_1_1)
        .DELETE()
        .build()

    val executorService = Executors.newFixedThreadPool(threads) {
//    val executorService = Executors.newSingleThreadExecutor {
        Thread(it).apply {
            isDaemon = true
        }
    }

    val httpClient = HttpClient.newHttpClient()
    val errorCount = AtomicInteger()
    val execCount = LongAdder()
    var c = 0
    while (++c <= calls) {
        val maxErrors = 10
        if (errorCount.get() >= maxErrors) break
        try {
            execCount.increment()
            val createResponse = httpClient.send(createRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }
            if (createResponse.statusCode() != 200) {
                project.logger.error("warmup error status: " + createResponse.statusCode() + ", body:" + createResponse.body())
                Thread.sleep(1000)
            } else {
                project.logger.warn("success init warm status: " + createResponse.statusCode() + ", body:" + createResponse.body())
                httpClient.send(getAllRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }.apply {
                    if (statusCode() != 200) {
                        project.logger.error("warmup error status on getAll: " + statusCode())
                    }
                }
                httpClient.send(getRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }.apply {
                    if (statusCode() != 200) {
                        project.logger.error("warmup error status on get by id: " + statusCode())
                    }
                }
                httpClient.send(deleteRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }.apply {
                    if (statusCode() != 200) {
                        project.logger.error("warmup error status on delete by id: " + statusCode())
                    }
                }
                break
            }
        } catch (e: Exception) {
            project.logger.error("warmup error {}:{}", e::class.java.simpleName, e.message)
            val i = errorCount.incrementAndGet()
            if (i >= maxErrors) {
                throw e
            }
            Thread.sleep(1000)
        }
    }
    //concurrently
    val warmUpError = AtomicReference<Exception?>()
    val routines = ArrayList<java.util.concurrent.Future<*>>()
    while (++c <= calls) {
        val maxErrors = 10
        if (errorCount.get() >= maxErrors) break
        routines += executorService.submit {
            if (errorCount.get() < maxErrors) try {
                execCount.increment()
                val createResponse =
                    httpClient.send(createRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }
                if (createResponse.statusCode() != 200) {
                    project.logger.error("warmup error status on create: " + createResponse.statusCode() + ", body:" + createResponse.body())
                } else {
                    httpClient.send(getAllRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }.apply {
                        if (statusCode() != 200) {
                            project.logger.error("warmup error status on getAll: " + statusCode())
                        }
                    }
                    httpClient.send(getRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }.apply {
                        if (statusCode() != 200) {
                            project.logger.error("warmup error status on get by id: " + statusCode())
                        }
                    }
                }
            } catch (e: Exception) {
                val i = errorCount.incrementAndGet()
                project.logger.error("warmup error {} {}:{}", i, e::class.java.simpleName, e.message)
                if (warmUpError.get() != null) {
                    return@submit
                } else if (i >= maxErrors) {
                    warmUpError.compareAndSet(null, e)
                    executorService.shutdown()
//                    val notExecuted = executorService.shutdownNow()
//                    project.logger.warn("not executed error " + notExecuted.size)
                } else {
                    Thread.sleep(1000)
                }
            }
        }
    }
    val exception = warmUpError.get()
    if (exception != null) {
        project.logger.error("warmup error", exception)
        throw exception
    } else {
        httpClient.send(deleteRequest) { BodySubscribers.ofString(Charset.defaultCharset()) }.apply {
            if (statusCode() != 200) {
                project.logger.error("warmup error status on delete by id: " + statusCode())
            }
        }
    }


    project.logger.warn("wait concurrent routines " + routines.size)
    routines.forEach {
        it.get()
    }
    executorService.shutdown()

    project.logger.warn(
        "warmup finish in " + LocalDateTime.now() + " calls " + execCount.sum() + ", errors " + errorCount
    )
}

tasks.create("k6Run", Exec::class.java) {
    val port: String by project.extra { "8080" }
    setupCmd(port)
}

fun Exec.setupCmd(port: String, users: Int = callUsers, iterationPerUser: Int = callsPerUser) {
    commandLine(
        "k6", "run", "--vus", users.toString(), "--iterations",
        (users * iterationPerUser).toString(), "-e", "SERVER_PORT=$port", "script.js"
    )
    doFirst {
        project.buildDir.mkdirs()
        standardOutput = File(project.buildDir, "result-" + this.name + ".txt").outputStream()
        project.logger.warn("bench start in {}", LocalDateTime.now())
    }
    doLast {
        project.logger.warn("bench finish in " + LocalDateTime.now())
    }
}

fun quarkusExec(name: String, port: String = "8092", storage: String = "memory") = tasks.create(name, Exec::class.java) {
    project.extra["storage"] = storage
    val buildJarTask = "quarkusBuild"
    val project = ":rest:java:quarkus"
    dependsOn("$project:$buildJarTask")

    group = "benchmark"
    doNotTrackState("benchmark")

    var process: Process? = null
    doFirst {
        try {
            val appBuildDir = project(project).buildDir
            val jar = File(appBuildDir, "quarkus-app/quarkus-run.jar")

            val p = ProcessBuilder(
                "java", "-Dquarkus.http.port=$port", "-Dquarkus.log.console.enable=false", "-jar", "$jar"
            ).start()

            checkRun("java server", p)
            process = p

            warmUp(p, port, warmUpAmounts)
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
