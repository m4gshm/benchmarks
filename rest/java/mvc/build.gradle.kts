plugins {
    `java-library`
    id("org.springframework.boot") version "2.6.5"
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage:model"))
    api(project(":rest:kotlin:storage"))
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.springframework.boot:spring-boot-starter-web:2.6.5")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.7")
}

