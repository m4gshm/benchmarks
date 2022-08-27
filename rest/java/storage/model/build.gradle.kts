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
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


dependencies {
    implementation("io.projectreactor:reactor-core:3.4.16")
    implementation("io.smallrye.reactive:mutiny:1.6.0")

    api("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    api("jakarta.persistence:jakarta.persistence-api:2.2.3")
    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
}