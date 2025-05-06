plugins {
    `java-library`
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

dependencyManagement {
    imports {
//        project.logger.warn("BOM_COORDINATES: " + org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    api(project(":rest:java:storage:model"))
    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:spring-data-reactive"))

    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("name.nkonev.r2dbc-migrate:r2dbc-migrate-spring-boot-starter:3.2.0")
    api("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.6")

    api("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE") {
        exclude(group = "io.projectreactor.netty", module = "reactor-netty")
//        exclude(group = "io.r2dbc", module = "r2dbc-spi")
    }

    api("de.mirkosertic:flight-recorder-starter:3.1.0")

    api("org.springframework.data:spring-data-redis")

    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

tasks.processAot {
    args("--spring.profiles.active=db")
}

graalvmNative {
    binaries {
        all {
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(24))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            })
            sharedLibrary.set(false)
            debug.set(false)
            verbose.set(true)
            richOutput.set(true)
            quickBuild.set(true)
            buildArgs.addAll(/*"--exact-reachability-metadata",*/ "-H:+UnlockExperimentalVMOptions", "-H:Log=registerResource:1")
//            runtimeArgs.add("-XX:MissingRegistrationReportingMode=Warn")
        }
    }
}
