plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "2.11.2.Final"

dependencies {
//    api(project(":rest:java:storage:model"))
    api(project(":rest:kotlin:storage"))
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    api(project(":rest:java:storage:model"))

    implementation("org.springframework.boot:spring-boot-autoconfigure:2.6.6")
    implementation("org.springframework:spring-context:5.3.23")
    implementation("org.springframework.data:spring-data-r2dbc:1.5.5")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
