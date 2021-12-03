allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "checkstyle")
    afterEvaluate {

        tasks.findByName("jmh").apply {
            this?.doNotTrackState("benchmark")
        }

        tasks.create("benchmarks") {
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