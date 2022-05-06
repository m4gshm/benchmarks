plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
//    application
//    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.graalvm.buildtools.native") version "0.9.11"
//    id("org.springframework.experimental.aot") version "0.11.4"
}

group = "benchmark"
//version = "0.0.1"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    val ktor_version = "2.0.1"
    val logback_version="1.2.11"
    api("com.benasher44:uuid:0.4.0")
    api("co.touchlab:stately-isolate:1.2.2")
    api("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    api("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
//    api("io.ktor:ktor-server-status-pages:$ktor_version")
    api("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    api("io.ktor:ktor-server-netty:$ktor_version")
    api("io.ktor:ktor-server-cio:$ktor_version")
    api("io.ktor:ktor-server-content-negotiation:$ktor_version")
    api("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
    api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    api("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
    api("ch.qos.logback:logback-classic:$logback_version")
}

tasks.nativeCompile {
    val options = this.options
    doFirst {
        project.logger.warn(options.get().buildArgs.get().toString())
    }
}

graalvmNative {
    binaries {
        named("main") {
            verbose.set(true)
            mainClass.set("m4gshm.benchmark.rest.ktor.graalvm.KtorGraalvmApplicationKt")
            javaLauncher.set(javaToolchains.launcherFor {
//                languageVersion.set(JavaLanguageVersion.of(8))
                vendor.set(JvmVendorSpec.matching("GraalVM"))
            })
        }
    }
}
