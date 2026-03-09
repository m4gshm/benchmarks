plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}



