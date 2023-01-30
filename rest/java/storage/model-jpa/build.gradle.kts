plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

val quarkusVersion: String = "2.15.1.Final"

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    api(project(":rest:java:storage:model"))
    implementation("io.projectreactor:reactor-core:3.4.16")
    implementation("io.smallrye.reactive:mutiny:1.6.0")

    api("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    implementation("javax.persistence:javax.persistence-api:2.2")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

//    implementation("io.quarkus:quarkus-hibernate-orm-panache:$quarkusVersion")
    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
//    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")


    annotationProcessor("com.querydsl:querydsl-apt:5.0.0")
    annotationProcessor("com.querydsl:querydsl-jpa:5.0.0")
    annotationProcessor("javax.persistence:javax.persistence-api:2.2")

    compileOnly("com.querydsl:querydsl-apt:5.0.0")
    compileOnly("com.querydsl:querydsl-jpa:5.0.0")
    api("com.querydsl:querydsl-core:5.0.0")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    this.options.compilerArgs = listOf(
        "-processor",
        "lombok.launch.AnnotationProcessorHider\$AnnotationProcessor,com.querydsl.apt.jpa.JPAAnnotationProcessor",
//        "-Aquerydsl.excludedPackages="
    )
}