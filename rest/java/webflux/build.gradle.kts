plugins {
    `java-library`
    id("org.springframework.boot") version "2.6.6"
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:model"))
    api(project(":rest:kotlin:storage"))

    api("org.springframework.boot:spring-boot-autoconfigure:2.6.6")
    api("org.springframework.boot:spring-boot-starter-webflux:2.6.6")
    api("org.springdoc:springdoc-openapi-webflux-ui:1.6.7")
    api("com.playtika.reactivefeign:feign-reactor-webclient:3.2.1")
    api("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.1")
    api("de.mirkosertic:flight-recorder-starter:2.3.0")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("junit:junit:4.13.2")
}

