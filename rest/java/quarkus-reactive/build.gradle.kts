import io.quarkus.gradle.tasks.QuarkusBuild

plugins {
    `java-library`
    id("io.quarkus") version "3.22.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val quarkusVersion: String = "3.22.1"

dependencies {
    compileOnly("io.quarkus:gradle-application-plugin:3.22.1")

    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:model-jpa"))

    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest-jackson")

    implementation("io.quarkus:quarkus-reactive-pg-client")
    implementation("io.quarkus:quarkus-hibernate-reactive-panache")

    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
}

group = "benchmark"

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
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

quarkus {
    setFinalName("quarkus")
}

//tasks.create<QuarkusBuild>("quarkusBuildDB") {
//    doFirst {
//        System.setProperty("storage", "db")
//    }
//}
