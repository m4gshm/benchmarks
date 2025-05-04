plugins {
    `java-library`
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
//    id("org.graalvm.buildtools.native") version "0.10.6"
}

//repositories {
//    maven("https://plugins.gradle.org/m2/")
//}

dependencyManagement {
    imports {
        project.logger.warn("BOM_COORDINATES: " + org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    api(project(":rest:java:storage:model"))
    api(project(":rest:java:storage:spring-data"))
    api(project(":rest:java:storage:querydsl-sql-jdbc"))
    api(project(":rest:java:storage:jdbc"))

    api("org.hibernate:hibernate-core:6.6.13.Final")
    api("com.zaxxer:HikariCP")
    api("org.postgresql:postgresql:42.7.5")

    implementation("org.liquibase:liquibase-core:4.31.1")

    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
