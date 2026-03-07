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
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}


dependencies {
    implementation("io.projectreactor:reactor-core:3.4.27")
    implementation("io.smallrye.reactive:mutiny:1.6.0")

    api("io.github.m4gshm:meta-api:0.0.1-rc6")
    api("io.github.m4gshm:meta-customizer-jpa-api:0.0.1-rc6")
    annotationProcessor("io.github.m4gshm:meta-processor:0.0.1-rc6")
    annotationProcessor("io.github.m4gshm:meta-customizer-jpa-processor:0.0.1-rc6")

//    api("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
//    api("jakarta.persistence:jakarta.persistence-api:3.1.0")
//    implementation("org.springframework.data:spring-data-relational:2.4.5")
    implementation("javax.persistence:javax.persistence-api:2.2")

//    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")
}
