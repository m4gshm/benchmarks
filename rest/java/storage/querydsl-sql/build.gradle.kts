buildscript {
    dependencies {
        classpath("org.postgresql:postgresql:42.5.1")
        classpath("com.querydsl:querydsl-sql-codegen:5.0.0")
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

val quarkusVersion: String = "2.15.1.Final"

dependencies {
    api(project(":rest:java:storage:model"))
    api(project(":rest:kotlin:storage"))
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

//    implementation("org.springframework.boot:spring-boot-autoconfigure:3.0.0")
//    implementation("org.springframework:spring-context:6.0.0")
//    implementation("org.springframework.data:spring-data-jpa:3.0.0")

    api("com.querydsl:querydsl-sql:5.0.0")
    implementation("com.querydsl:querydsl-sql-codegen:5.0.0")
//    api("com.querydsl:querydsl-jpa:5.0.0")
//    implementation("com.querydsl:querydsl-jpa-codegen:5.0.0")

//    api("jakarta.annotation:jakarta.annotation-api:1.3.5")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.queryDslMetadataExport {
    jdbcDriver = "org.postgresql.Driver"
    jdbcUser = "postgres"
    jdbcPassword = "postgres"
    jdbcUrl = "jdbc:postgresql://localhost:5433/postgres"
    packageName = "m4gshm.benchmark.rest.spring.boot.storage.querydsl.sql.model"
    targetFolder = file("${project.projectDir}/src/main/java")
    beanTargetFolder = targetFolder

    columnAnnotations = true
    exportBeans = true
    beanSuffix = "Dto"
    exportTables = true
    exportPrimaryKeys = true
    exportForeignKeys = true
    exportInverseForeignKeys = true
    exportDirectForeignKeys = true
    customTypes = listOf("com.querydsl.sql.types.JSR310LocalDateTimeType")
}


