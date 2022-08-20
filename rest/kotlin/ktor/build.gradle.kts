val koin_version: String by project
val ktor_version = "2.0.1"
val kotlin_version: String by project
val logback_version: String by project
//val koin_ksp_version = "1.0.0-beta-1"


plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
//    id("com.google.devtools.ksp") version ("1.6.20-1.0.5")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}


group = "benchmark"
//version = "0.0.1"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

application {
    mainClass.set("m4gshm.benchmark.rest.ktor.KtorApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to application.mainClass)
    }
}

dependencies {
    api("com.benasher44:uuid:0.4.0")
    api("co.touchlab:stately-isolate:1.2.2")
    api("org.jetbrains.kotlin:kotlin-stdlib-common:1.6.20")
    api("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
    api("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    api("io.ktor:ktor-server-core:$ktor_version")
    api("io.ktor:ktor-server-cio:$ktor_version")
    api("io.ktor:ktor-server-status-pages:$ktor_version")
    api("io.ktor:ktor-server-content-negotiation:$ktor_version")
    api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    api("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    api("io.ktor:ktor-server-netty:$ktor_version")
    api("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
    api("ch.qos.logback:logback-classic:$logback_version")
    api(project(":rest:java:model"))
    api(project(":rest:kotlin:storage"))
}
