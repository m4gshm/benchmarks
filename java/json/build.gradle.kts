plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {

//    jmh("org.openjdk.jmh:jmh-core:1.29")
//    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.29")

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
    profilers.add("jfr:dir=${project.buildDir}")
}

tasks.create("jsonBenchmarks") {
    group = "benchmark"
    dependsOn("jmh")
}
