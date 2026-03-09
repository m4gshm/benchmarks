plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "3.22.1"

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    api(project(":rest:java:storage:model"))

    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
    api("io.quarkus:quarkus-hibernate-reactive-panache:$quarkusVersion")
    api("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")

}



