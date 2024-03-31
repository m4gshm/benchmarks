allprojects {
    repositories {
        mavenCentral()
        maven("https://m4gshm.github.io/maven")
        maven("https://repo.spring.io/release")
        maven("https://repo.spring.io/milestone")
    }
}

plugins {
    kotlin("multiplatform") version "1.7.20" apply false
    kotlin("plugin.serialization") version "1.7.20" apply false
}

subprojects {
//    apply(plugin = "checkstyle")
    afterEvaluate {

        tasks.findByName("jmh").apply {
            this?.doNotTrackState("benchmark")
        }

        if (tasks.findByName("benchmarks") == null) tasks.create("benchmarks") {
            group = "benchmark"
            dependsIfExists(this@afterEvaluate, "jmh")
            dependsIfExists(this@afterEvaluate, "goBenchmarks")
        }
    }
}

fun Task.dependsIfExists(proj: Project, taskName: String) {
    val dependsOn = proj.tasks.findByName(taskName)
    if (dependsOn != null) dependsOn(taskName)
}