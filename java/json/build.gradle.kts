plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    implementation("org.projectlombok:lombok:1.18.20")

    listOf(
        "com.fasterxml.jackson.core:jackson-databind:2.12.5",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.5"
    ).forEach { dependency ->
        listOf("implementation", "jmh").forEach { configName ->
            add(configName, dependency)
        }
    }

    testImplementation("junit:junit:4.13")
}

sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources")
    }
}

jmh {
    forceGC.set(true)
}

tasks.create("jsonBenchmarks") {
    group = "benchmark"
    dependsOn("jmh")
}
