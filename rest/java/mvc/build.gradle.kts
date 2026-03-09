import com.bmuschko.gradle.docker.DockerConventionJvmApplicationPlugin
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.gradle.kotlin.dsl.named

plugins {
    `java-library`
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("docker-conventions")
//    id("org.graalvm.buildtools.native") version "0.10.6"
}

//repositories {
//    maven("https://plugins.gradle.org/m2/")
//}

dependencyManagement {
    imports {
//        project.logger.warn("BOM_COORDINATES: " + org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    api(project(":rest:java:async-profile-rest-api"))
    api(project(":rest:java:storage:model"))
    api(project(":rest:java:storage:spring-data"))
    api(project(":rest:java:storage:querydsl-sql-jdbc"))
    api(project(":rest:java:storage:jdbc"))
    api(project(":rest:java:storage:jooq"))
    api(project(":rest:kotlin:storage"))

    implementation("org.jooq:jooq")
    implementation("org.hibernate:hibernate-core")
    implementation("com.zaxxer:HikariCP")
    implementation("org.postgresql:postgresql")

    implementation("org.liquibase:liquibase-core")

    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-persistence")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
//    implementation("org.springframework.boot:spring-boot-starter-graphql")
//    implementation("com.tailrocks.graphql:graphql-datetime-spring-boot-starter")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
}

tasks.named<Dockerfile>(DockerConventionJvmApplicationPlugin.DOCKERFILE_TASK_NAME) {
    exposePort(8080, 8080)
}
