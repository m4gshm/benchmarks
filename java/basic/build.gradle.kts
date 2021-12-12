import me.champeau.jmh.JMHTask

plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    jmh("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
//    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

jmhTask("mapBenchmarks", "m4gshm.benchmark.map")
jmhTask("fibonacciBenchmarks", "m4gshm.benchmark.fibonacci")

jmh {
    forceGC.set(true)
    profilers.add("gc")
    verbosity.set("extra")
}

fun jmhTask(name: String, vararg include: String) = tasks.create<JMHTask>(name) {
    group = "benchmark"
    doNotTrackState("benchmark")
    includes.addAll(*include)
    val jmhTask = tasks.jmh.get()
    jarArchive.set(jmhTask.jarArchive)
    resultsFile.set(jmhTask.resultsFile)
    dependsOn("jmhJar")
}