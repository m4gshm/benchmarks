import io.quarkus.gradle.tasks.QuarkusBuild

plugins {
    `java-library`
    id("io.quarkus") version "3.32.0.CR1"
}

repositories {
//    mavenCentral()
    gradlePluginPortal()
}

val quarkusVersion: String = "3.32.0.CR1"

dependencies {
    compileOnly("io.quarkus:gradle-application-plugin:$quarkusVersion")

    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    api("org.jetbrains:annotations:13.0")

    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:jfr"))
    api(project(":rest:java:storage:jdbc"))
    api(project(":rest:java:storage:model-jpa"))

    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))
    annotationProcessor(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest-jackson")

    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")

    annotationProcessor("io.quarkus:quarkus-panache-common")
}

group = "benchmark"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
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

//tasks.register<QuarkusBuild>("quarkusBuildDB") {
//    doFirst {
//        System.setProperty("storage", "db")
//    }
//}
//
//tasks.register<QuarkusBuild>("quarkusBuildNative") {
//    this.doFirst {
//        this.project.extra["quarkus.package.type"] = "native"
//        this.project.extra["quarkus.native.additional-build-args"] = "-J--enable-preview"
//    }
//}
