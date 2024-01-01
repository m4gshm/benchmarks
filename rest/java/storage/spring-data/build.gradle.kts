plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "2.15.1.Final"

dependencies {
//    api(project(":rest:java:storage:model"))
    api(project(":rest:kotlin:storage"))
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    api(project(":rest:java:storage:model-jpa"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:3.0.0")
    implementation("org.springframework:spring-context:6.0.0")
    implementation("org.springframework.data:spring-data-jpa:3.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

