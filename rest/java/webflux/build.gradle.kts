plugins {
    `java-library`
    id("org.springframework.boot") version "3.2.1"
    id("org.graalvm.buildtools.native") version "0.9.18"
}

repositories {
    maven("https://repo.spring.io/milestone")
//    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    api(project(":rest:java:storage:model"))
    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:storage:spring-data-reactive"))

    api("org.springframework.boot:spring-boot-autoconfigure:3.2.1")
    api("org.springframework.boot:spring-boot-starter-webflux:3.2.1")
    api("org.springframework.boot:spring-boot-starter-actuator:3.2.1")
    api("org.springframework.boot:spring-boot-starter-data-r2dbc:3.2.1")
    api("name.nkonev.r2dbc-migrate:r2dbc-migrate-spring-boot-starter:2.8.0")
    api("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

    api("org.postgresql:r2dbc-postgresql:1.0.3.RELEASE") {
        exclude(group = "io.projectreactor.netty", module = "reactor-netty")
//        exclude(group = "io.r2dbc", module = "r2dbc-spi")
    }
//    api("io.r2dbc:r2dbc-spi:0.8.6.RELEASE")

//    api("org.hibernate:hibernate-core:6.1.6.Final")
//    api("com.zaxxer:HikariCP:4.0.3")
//    api("org.postgresql:postgresql:42.4.0")

//    api("org.springdoc:springdoc-openapi-webflux-ui:1.6.14")
//    api("com.playtika.reactivefeign:feign-reactor-webclient:3.2.6")
//    api("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.6")
    api("de.mirkosertic:flight-recorder-starter:3.1.0")

    //native dependencies
    api("org.springframework.data:spring-data-redis:3.2.1")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("junit:junit:4.13.2")
}

//tasks.bootJar {
//    this.mainClass.set("m4gshm.benchmark.rest.spring.boot.WebfluxApplication")
//}


//tasks.nativeCompile {
//    options.get().runtimeArgs.add("-H:+AllowVMInspection")
//    doFirst {
//
//    }
//}

tasks.aotClasses {

}

graalvmNative {
    binaries {
        all {
            sharedLibrary.set(false)
            debug.set(false)
            verbose.set(true)
//            buildArgs.add("-H:+AllowVMInspection")
            buildArgs.add("--enable-monitoring=jfr")
        }
//        named("main") {
////            mainClass.set(tasks.bootJar.flatMap { it.mainClass })
////C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools\VC\Auxiliary\Build\vcvars64.bat
//        }
    }
}
