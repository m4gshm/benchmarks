//val kotlin_version: String by project

plugins {
//    kotlin("jvm") version "1.6.20"
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    val hostOs = System.getProperty("os.name")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> linuxX64("native")// wsl
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    nativeTarget.apply {
        binaries {
            sharedLib {
                baseName = "storage"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
//                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
                api("org.jetbrains.kotlin:kotlin-stdlib-common:1.6.20")
                api("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
//                api("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.3.2")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
//                api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.3.2")
            }
        }
    }
}

group = "benchmark"
