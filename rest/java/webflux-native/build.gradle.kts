plugins {
    `java-library`
    id("org.springframework.boot") version "2.6.6"
    id("org.springframework.experimental.aot") version "0.11.4"
}


repositories {
    maven { url = uri("https://repo.spring.io/release") }
    maven("https://plugins.gradle.org/m2/")
    maven("https://repo.spring.io/release")
}

dependencies {
    api(project(":rest:java:model"))
    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:webflux"))

    api("org.springframework.boot:spring-boot-autoconfigure:2.6.6")
    api("org.springframework.boot:spring-boot-starter-webflux:2.6.6")

    api("org.springdoc:springdoc-openapi-native:1.6.7")
    api("org.springdoc:springdoc-openapi-webflux-ui:1.6.7")
    api("com.playtika.reactivefeign:feign-reactor-webclient:3.2.1")
    api("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.1")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("junit:junit:4.13.2")

    compileOnly("org.springframework.experimental.aot:org.springframework.experimental.aot.gradle.plugin:0.11.4")
}

tasks.bootJar {
    this.mainClass.set("m4gshm.benchmark.rest.spring.boot.Application")
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set(tasks.bootJar.flatMap { it.mainClass })
            sharedLibrary.set(false)
            debug.set(true)
            verbose.set(true)
//            buildArgs.addAll("-g", "-O0")
            javaLauncher.set(javaToolchains.launcherFor {
//        languageVersion.set(JavaLanguageVersion.of(17))
                this.vendor.set(JvmVendorSpec.matching("GraalVM"))
            })
//C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools\VC\Auxiliary\Build\vcvars64.bat
        }
    }
}
