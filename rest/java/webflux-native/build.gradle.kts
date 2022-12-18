plugins {
    `java-library`
    id("org.springframework.boot") version "3.0.0"
    id("org.graalvm.buildtools.native") version "0.9.18"
//    id("org.springframework.experimental.aot") version "0.12.1"
}

//buildscript {
//    repositories {
//        maven("https://repo.spring.io/release")
//    }
//    dependencies {
//        classpath("org.springframework.experimental:spring-aot-gradle-plugin:0.12.1")
////        classpath("org.graalvm.buildtools:native-gradle-plugin:0.9.17")
//    }
//}
//
//apply(plugin = "org.springframework.boot")
//apply(plugin = "org.graalvm.buildtools.native")

repositories {
    maven("https://repo.spring.io/milestone")
    maven("https://repo.spring.io/release")
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
//    implementation(project(":rest:java:storage:model"))
//    api(project(":rest:kotlin:storage"))
    api(project(":rest:java:webflux")) {
        exclude(group = "org.springframework.boot")
    }

    api("org.springframework.boot:spring-boot-autoconfigure:3.0.0")
    api("org.springframework.boot:spring-boot-starter-webflux:3.0.0")
    api("org.springframework.boot:spring-boot-starter-data-r2dbc:3.0.0")
//    api("org.springframework:spring-web:5.3.22")
//    api("org.springframework:spring-core:5.3.22")

//    api("org.springdoc:springdoc-openapi-native:1.6.7")
//    api("org.springdoc:springdoc-openapi-webflux-ui:1.6.7")
    api("com.playtika.reactivefeign:feign-reactor-webclient:3.2.1")
    api("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.1")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    testImplementation("junit:junit:4.13.2")

//    compileOnly("org.springframework.experimental.aot:org.springframework.experimental.aot.gradle.plugin:0.11.4")
}

tasks.bootJar {
    this.mainClass.set("m4gshm.benchmark.rest.spring.boot.WebfluxApplication")
}

graalvmNative {
    binaries {
        all {
            sharedLibrary.set(false)
            debug.set(false)
            verbose.set(true)
        }
        named("main") {
            mainClass.set(tasks.bootJar.flatMap { it.mainClass })

//            buildArgs.addAll("-g", "-O0")
//            javaLauncher.set(javaToolchains.launcherFor {
////        languageVersion.set(JavaLanguageVersion.of(17))
//                this.vendor.set(JvmVendorSpec.matching("GraalVM"))
//            })
//C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools\VC\Auxiliary\Build\vcvars64.bat
        }
    }
}
