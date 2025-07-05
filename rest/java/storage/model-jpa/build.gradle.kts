plugins {
    `java-library`
}

repositories {
    mavenLocal()
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "3.22.1"

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

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

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

tasks.withType<JavaCompile> {
    this.options.compilerArgs = listOf(
        "-processor",
        "lombok.launch.AnnotationProcessorHider\$AnnotationProcessor,com.querydsl.apt.jpa.JPAAnnotationProcessor",
//        "-Aquerydsl.excludedPackages="
    )
}