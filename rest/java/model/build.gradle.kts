plugins {
    `java-library`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
}
