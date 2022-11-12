plugins {
    `java-library`
    id("org.springframework.boot") version "3.0.0-RC2"
}

repositories {
    maven("https://repo.spring.io/milestone")
//    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage:model"))
    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:spring-data-reactive"))

    api("org.springframework.boot:spring-boot-autoconfigure:3.0.0-RC2")
    api("org.springframework.boot:spring-boot-starter-webflux:3.0.0-RC2")
    api("org.springframework.boot:spring-boot-starter-data-r2dbc:3.0.0-RC2")
//    implementation("name.nkonev.r2dbc-migrate:r2dbc-migrate-core:2.7.8")
    implementation("name.nkonev.r2dbc-migrate:r2dbc-migrate-spring-boot-starter:2.7.8")


    api("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE") {
        exclude(group = "io.projectreactor.netty", module = "reactor-netty")
    }

//    api("org.hibernate:hibernate-core:5.6.10.Final")
//    api("com.zaxxer:HikariCP:4.0.3")
//    api("org.postgresql:postgresql:42.4.0")

    api("org.springdoc:springdoc-openapi-webflux-ui:1.6.11")
//    api("com.playtika.reactivefeign:feign-reactor-webclient:3.2.6")
//    api("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.6")
    api("de.mirkosertic:flight-recorder-starter:2.3.0")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    testImplementation("junit:junit:4.13.2")
}

