plugins {
    `java-library`
    id("io.quarkus") version "3.32.0.CR1"
}

repositories {
    gradlePluginPortal()
}

val quarkusVersion: String = "3.32.0.CR1"

dependencies {
    compileOnly("io.quarkus:gradle-application-plugin:$quarkusVersion")
    compileOnly("io.quarkus.arc:arc-processor:$quarkusVersion")

    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:model-jpa"))

    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))
    annotationProcessor(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest-jackson")

    implementation("io.quarkus:quarkus-reactive-pg-client")
    implementation("io.quarkus:quarkus-hibernate-reactive-panache")

    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    annotationProcessor("io.quarkus:quarkus-panache-common")
}

group = "benchmark"



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
