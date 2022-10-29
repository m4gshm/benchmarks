import io.quarkus.gradle.tasks.QuarkusBuild

plugins {
    `java-library`
    id("io.quarkus") version "2.13.3.Final"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val quarkusVersion: String = "2.13.3.Final"

dependencies {
    compileOnly("io.quarkus:gradle-application-plugin:2.13.3.Final")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    api(project(":rest:kotlin:storage"))

    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-jdbc-postgresql")

//    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    annotationProcessor("io.quarkus:quarkus-panache-common:$quarkusVersion")

//    compileOnly("io.quarkus:gradle-application-plugin:$quarkusVersion")
//    compileOnly("io.quarkus:quarkus-hibernate-orm:$quarkusVersion")
    compileOnly("io.quarkus:quarkus-hibernate-orm-deployment:$quarkusVersion")
//    compileOnly("io.quarkus:quarkus-hibernate-orm-panache-common:$quarkusVersion")
//    compileOnly("io.quarkus:quarkus-hibernate-orm-panache-common-deployment:$quarkusVersion")

}

group = "benchmark"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
