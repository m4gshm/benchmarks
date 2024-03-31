import io.quarkus.gradle.tasks.QuarkusBuild

plugins {
    `java-library`
    id("io.quarkus") version "2.15.1.Final"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val quarkusVersion: String = "2.15.1.Final"

dependencies {
    compileOnly("io.quarkus:gradle-application-plugin:2.15.1.Final")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:model-jpa"))

    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))
    implementation("io.quarkus:quarkus-arc")
    if (project.hasProperty("reactive")) {
        project.logger.warn("QUARKUS-RESTEASY-REACTIVE enabled")
        implementation("io.quarkus:quarkus-resteasy-reactive")
        implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    } else {
        implementation("io.quarkus:quarkus-resteasy")
        implementation("io.quarkus:quarkus-resteasy-jackson")
    }
    implementation("io.quarkus:quarkus-jdbc-postgresql")

    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
}

group = "benchmark"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

quarkus {
    setFinalName("quarkus")
}

tasks.create<QuarkusBuild>("quarkusBuildDB") {
    doFirst {
        System.setProperty("storage", "db")
    }
}

tasks.create<QuarkusBuild>("quarkusBuildNative") {
    this.
    doFirst {
        this.project.extra["quarkus.package.type"] = "native"
        this.project.extra["quarkus.native.additional-build-args"] = "-J--enable-preview"
    }
}
