plugins {
    kotlin("jvm")
}

group = "benchmark"


dependencies {
    api(project(":rest:java:jfr"))
    api(project(":rest:java:storage:model"))
    implementation("io.projectreactor:reactor-core:3.4.16")
    implementation("io.smallrye.reactive:mutiny:1.6.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
