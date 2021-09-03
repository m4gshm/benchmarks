allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
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