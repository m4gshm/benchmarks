plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "3.22.1"

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    api(project(":rest:java:storage:model"))

    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
    api("io.quarkus:quarkus-hibernate-reactive-panache:$quarkusVersion")
    api("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")

}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

