val koin_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
//val koin_ksp_version = "1.0.0-beta-1"


plugins {
    application
    kotlin("jvm") version "1.6.20"
//    id("com.google.devtools.ksp") version ("1.6.20-1.0.5")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "benchmark"
//version = "0.0.1"

repositories {
    maven("https://jitpack.io")
}

application {
    mainClass.set("m4gshm.benchmark.rest.ktor.ApplicationKt")
//    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.jar {
//    afterEvaluate {
        manifest {
            attributes("Main-Class" to application.mainClass)
        }
//    }
}

tasks.shadowJar {
    manifest {
        attributes("Main-Class" to "io.ktor.server.netty.EngineMain")
//        attributes("Main-Class" to "m4gshm.benchmark.rest.ktor.ApplicationKt")
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

//sourceSets.main {
//    java.srcDirs("build/generated/ksp/main/kotlin")
//}

dependencies {
    api(project(":rest:java:storage"))

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")

    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

//    ksp("io.insert-koin:koin-ksp-compiler:$koin_ksp_version")
//    implementation("io.insert-koin:koin-annotations:$koin_ksp_version")
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")
}