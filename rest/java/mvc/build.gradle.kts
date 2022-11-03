plugins {
    `java-library`
    id("org.springframework.boot") version "2.6.5"
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage:model"))
//    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:spring-data"))

    api("org.hibernate:hibernate-core:5.6.10.Final")
    api("com.zaxxer:HikariCP:4.0.3")
    api("org.postgresql:postgresql:42.4.0")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.springframework.boot:spring-boot-starter-web:2.6.5")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.7")
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "--enable-preview",
        )
    )
}