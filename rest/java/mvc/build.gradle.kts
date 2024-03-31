plugins {
    `java-library`
    id("org.springframework.boot") version "3.2.1"
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage:model"))
//    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:spring-data"))
    api(project(":rest:java:storage:querydsl-sql-jdbc"))
    api(project(":rest:java:storage:jdbc"))

    api("org.hibernate:hibernate-core:6.4.1.Final")
    api("com.zaxxer:HikariCP:5.1.0")
    api("org.postgresql:postgresql:42.7.1")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.1")
    implementation("org.springframework.data:spring-data-jpa:3.2.1")
//    implementation("org.springdoc:springdoc-openapi-ui:2.3.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    implementation("org.liquibase:liquibase-core:4.25.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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