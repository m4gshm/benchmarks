plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    jmh("com.fasterxml.jackson.core:jackson-databind:2.13.0")
}

tasks.create("mapBenchmarks") {
    group = "benchmark"
    dependsOn("jmh")
}

jmh {
    forceGC.set(true)
    profilers.add("gc")
    verbosity.set("extra")
}