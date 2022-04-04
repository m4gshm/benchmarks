plugins {
    `java-library`
    id("org.springframework.boot") version "2.6.5"
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage"))
    api("org.springframework.boot:spring-boot-starter-webflux:2.6.5")
    api("org.springdoc:springdoc-openapi-webflux-ui:1.6.6")
    api("com.playtika.reactivefeign:feign-reactor-webclient:3.2.1")
    api("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.1")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("junit:junit:4.13.2")
}

