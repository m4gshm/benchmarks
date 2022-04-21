plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")
}

