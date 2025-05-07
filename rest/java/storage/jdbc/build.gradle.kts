buildscript {
    dependencies {
        classpath("org.postgresql:postgresql:42.5.1")
        classpath("com.querydsl:querydsl-sql-codegen:5.1.0")
        classpath("com.google.guava:guava:30.1.1-android")
    }
}

plugins {
    `java-library`
    id("com.github.ryarnyah.querydsl") version "0.0.3"
}


repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage:model"))

    implementation("org.jetbrains:annotations:13.0")

    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

