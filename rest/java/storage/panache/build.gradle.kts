plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "2.11.2.Final"

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    api(project(":rest:java:storage:model"))


    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
    api("io.quarkus:quarkus-hibernate-reactive-panache:$quarkusVersion")
    api("io.quarkus:quarkus-reactive-pg-client:$quarkusVersion")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

