plugins {
    java
    id("me.champeau.jmh") version "0.6.5"
}

group = "m4gshm.benchmark"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

jmh {
//    forceGC.set(true)
//    profilers.set(listOf("jfr"))
}