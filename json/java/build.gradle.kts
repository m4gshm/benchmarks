plugins {
    `java-library`
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")

    listOf(
        "com.fasterxml.jackson.core:jackson-databind:2.12.5",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.5"
    ).forEach { dependency ->
        listOf("implementation", "jmh").forEach { configName ->
            add(configName, dependency)
        }
    }
}

jmh {
    profilers.add("gc")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

sourceSets {
    main {
        resources {
            srcDir("$rootDir/resources-json")
        }
    }
}
