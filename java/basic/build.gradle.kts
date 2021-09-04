plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    jmh("com.fasterxml.jackson.core:jackson-databind:2.12.5")
}

tasks.create("mapBenchmarks") {
    group = "benchmark"
    dependsOn("jmh")
}