import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
//        maven("https://m4gshm.github.io/maven")
//        maven("https://repo.spring.io/release")
//        maven("https://repo.spring.io/milestone")
    }
}

plugins {
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "4.0.3" apply false
    kotlin("multiplatform") version "2.3.10" apply false
    kotlin("plugin.serialization") version "2.3.10" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
//    apply(plugin = "checkstyle")

    the<JavaPluginExtension>().apply {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("--enable-preview"))
    }

    afterEvaluate {
        tasks.findByName("jmh")?.apply {
            doNotTrackState("benchmark")
        }

        if (tasks.findByName("benchmarks") == null) tasks.register("benchmarks") {
            group = "benchmark"
            dependsIfExists(this@afterEvaluate, "jmh")
            dependsIfExists(this@afterEvaluate, "goBenchmarks")
        }
    }

    dependencies {
        listOf("implementation", "annotationProcessor", "testAnnotationProcessor").forEach {
            add(
                it,
                "org.projectlombok:lombok"
            )
        }
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom(BOM_COORDINATES)
        }
        dependencies {
            dependency("org.projectlombok:lombok:1.18.42")

//            dependency("org.springframework.boot:spring-boot-autoconfigure:"+ SPRING_BOOT_VERSION)
            dependency("org.springdoc:springdoc-openapi-starter-webflux-ui:3.0.1")
            dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

            dependency("name.nkonev.r2dbc-migrate:r2dbc-migrate-spring-boot-starter:4.0.1")

            dependency("org.hibernate:hibernate-core:7.2.5.Final")
            dependency("org.postgresql:postgresql:42.7.7")
            dependency("org.liquibase:liquibase-core:5.0.1")
            dependency("org.postgresql:r2dbc-postgresql:1.1.1.RELEASE")
            dependency("io.r2dbc:r2dbc-pool:1.0.2.RELEASE")
            dependency("org.jooq:jooq:3.20.11")

            dependency("org.junit.jupiter:junit-jupiter-api:5.12.2")

            dependency("org.jetbrains:annotations:13.0")

//            dependency("com.tailrocks.graphql:graphql-datetime-spring-boot-starter:6.0.0")
//            dependency("com.tailrocks.graphql:graphql-datetime-spring-boot-autoconfigure:6.0.0")
        }
    }
}

fun Task.dependsIfExists(proj: Project, taskName: String) {
    val dependsOn = proj.tasks.findByName(taskName)
    if (dependsOn != null) dependsOn(taskName)
}
