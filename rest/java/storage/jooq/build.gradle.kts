plugins {
    `java-library`
    id("liquibase-conventions")
//    id("org.liquibase.gradle") version "3.0.2"
    id("org.jooq.jooq-codegen-gradle") version "3.20.11"
    id("io.spring.dependency-management")
}

buildscript {
    val liquibaseVer: String by extra { "4.33.0" }
}

sourceSets {
    main {
        java {
            srcDirs("$projectDir/build/generated-sources/jooq")
        }
    }
}

dependencyManagement {
    dependencies {
        val liquibaseVer: String by project.extra
        dependency("org.liquibase:liquibase-core:${liquibaseVer}")
        dependency("info.picocli:picocli:4.7.7")
    }
}

dependencies {
    api(project(":rest:java:storage:model"))
    api("org.jooq:jooq")

    implementation("org.slf4j:slf4j-api")
    implementation("org.jetbrains:annotations")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-jooq")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("info.picocli:picocli")
    liquibaseRuntime("org.postgresql:postgresql")

    jooqCodegen("org.postgresql:postgresql")
}

val dbSchema by project.extra { "public" }
val dbUsername by project.extra { "postgres" }
val dbPassword by project.extra { "postgres" }
val dbAddress by project.extra { "localhost:5433" }
val dbUrl by project.extra { "jdbc:postgresql://$dbAddress/postgres" }

jooq {
    configuration {
        logging = org.jooq.meta.jaxb.Logging.DEBUG
        jdbc {
            driver = "org.postgresql.Driver"
            url = dbUrl
            user = dbUsername
            password = dbPassword
        }
        generator {
            name = "org.jooq.codegen.DefaultGenerator"
            database {
                inputSchema = "public"
                name = "org.jooq.meta.postgres.PostgresDatabase"
                includes = ".*"
                excludes = ""
            }
            target {
                packageName = "io.github.m4gshm.benchmark.rest.data.access.jooq"
            }
        }
    }
}

fun requiredProperty(propertyName: String, defaultValue: String? = null) = project.findProperty(propertyName)
    ?: defaultValue ?: throw GradleException("undefined $propertyName")

tasks.register<LiquibaseTask>("liquibaseUpdate") {
    command = "update"
}

tasks.named("jooqCodegen") {
    dependsOn("liquibaseUpdate")
}

