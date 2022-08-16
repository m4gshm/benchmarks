plugins {
    kotlin("jvm")
}

group = "benchmark"


dependencies {
    api(project(":rest:java:jfr"))
}