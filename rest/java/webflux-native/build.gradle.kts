//buildscript {
//    dependencies {
//        classpath("org.springframework.boot:spring-boot-autoconfigure:2.6.6")
//        classpath("org.springframework.boot:spring-boot-starter-webflux:2.6.6")
//    }
//}

plugins {
    `java-library`
    id("org.springframework.boot") version "2.6.6"
    id("org.springframework.experimental.aot") version "0.11.4"
}


repositories {
    maven { url = uri("https://repo.spring.io/release") }
    maven("https://plugins.gradle.org/m2/")
    maven("https://repo.spring.io/release")
//    maven("https://repo.spring.io/libs-release-local")
}

dependencies {
    api(project(":rest:java:storage"))
    api("org.springframework.boot:spring-boot-autoconfigure:2.6.6")
    api("org.springframework.boot:spring-boot-starter-webflux:2.6.6")
    api("org.springframework.experimental:spring-native-configuration:0.11.4")

    api("org.springdoc:springdoc-openapi-webflux-ui:1.6.7")
    api("com.playtika.reactivefeign:feign-reactor-webclient:3.2.1")
    api("com.playtika.reactivefeign:feign-reactor-spring-configuration:3.2.1")

    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("junit:junit:4.13.2")

    compileOnly("org.springframework.experimental.aot:org.springframework.experimental.aot.gradle.plugin:0.11.3")

}

nativeBuild {
    sharedLibrary.set(false)
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.matching("GraalVM"))
    })
}

springAot {
//    mode.set(org.springframework.aot.gradle.dsl.AotMode.NATIVE)
//    debugVerify.set(true)
//    verify.set(false)

}
