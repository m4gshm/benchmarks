import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationToRunnableFiles
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

val koin_version: String by project
val ktor_version = "2.0.1"
val kotlin_version: String by project
val logback_version: String by project
//val koin_ksp_version = "1.0.0-beta-1"


plugins {
    kotlin("multiplatform")
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

kotlin {
    jvm()
    val hostOs = System.getProperty("os.name")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> linuxX64("native")
//        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
        else -> null
    }?.apply {
        binaries {
            executable {
                entryPoint = "m4gshm.benchmark.rest.ktor.main"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":rest:kotlin:storage-multiplatform"))
                api("com.benasher44:uuid:0.4.0")
                api("co.touchlab:stately-isolate:1.2.2")
                api("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
                api("io.ktor:ktor-server-core:$ktor_version")
                api("io.ktor:ktor-server-cio:$ktor_version")
                api("io.ktor:ktor-server-status-pages:$ktor_version")
                api("io.ktor:ktor-server-content-negotiation:$ktor_version")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
                api("io.ktor:ktor-server-netty:$ktor_version")
                api("io.ktor:ktor-serialization-jackson-jvm:$ktor_version")
                api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
                api("ch.qos.logback:logback-classic:$logback_version")
            }
        }
    }
}

tasks.shadowJar {
    val jvm: KotlinTarget by kotlin.targets
    val main by jvm.compilations

    from(main.output)

    configurations = mutableListOf(
        (main as KotlinCompilationToRunnableFiles<KotlinCommonOptions>).runtimeDependencyFiles as Configuration
    )
}

kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
    binaries.all {
        freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
    }
}
