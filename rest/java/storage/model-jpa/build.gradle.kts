plugins {
    `java-library`
}

repositories {
    mavenLocal()
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "3.22.1"

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("io.github.m4gshm:meta-api:0.0.1-rc4")
    annotationProcessor("io.github.m4gshm:meta-processor:0.0.1-rc4")

    api(project(":rest:java:storage:model"))
    implementation("io.projectreactor:reactor-core:3.4.27")
    implementation("io.smallrye.reactive:mutiny:1.6.0")

    api("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")

    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    compileOnly("com.querydsl:querydsl-apt:5.1.0:jakarta")
    compileOnly("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    api("com.querydsl:querydsl-core:5.1.0")

}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-processor",
            $$"lombok.launch.AnnotationProcessorHider$AnnotationProcessor,com.querydsl.apt.jpa.JPAAnnotationProcessor",
        )
    )
}
