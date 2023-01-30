plugins {
    `java-library`
    id("org.springframework.boot") version "3.0.0"
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage:model"))
//    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:spring-data"))
    api(project(":rest:java:storage:querydsl-jdbc"))

    api("org.hibernate:hibernate-core:6.1.6.Final")
    api("com.zaxxer:HikariCP:4.0.3")
    api("org.postgresql:postgresql:42.4.3")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.0")
    implementation("org.springframework.data:spring-data-jpa:3.0.0")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.7")

    implementation("jakarta.persistence:jakarta.persistence-api:2.3.3")

    implementation("org.liquibase:liquibase-core:4.19.0")
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