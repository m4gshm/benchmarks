import com.google.protobuf.gradle.ofSourceSet

plugins {
    java
//    `java-library`
    id("me.champeau.jmh") version "0.6.6"
    id("com.google.protobuf") version "0.8.18"
}

val protobufDepVersion = "3.0.0"
val grpcVersion = "1.37.0"

dependencies {

    annotationProcessor("org.projectlombok:lombok:1.18.22")
    implementation("org.projectlombok:lombok:1.18.22")

    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufDepVersion")

//    listOf(
////        "com.fasterxml.jackson.core:jackson-databind:2.12.5",
////        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.5"
//    ).forEach { dependency ->
//        listOf("implementation", "jmh").forEach { configName ->
//            add(configName, dependency)
//        }
//    }

    testImplementation("junit:junit:4.13")
}

sourceSets.jmh {
    resources {
        srcDir("$rootDir/resources")
    }
}

jmh {
    profilers.add("gc")
}

tasks.create("grpcBenchmarks") {
    group = "benchmark"
    dependsOn("jmh")
}

protobuf {
    protobuf.apply {
        protoc(closureOf<com.google.protobuf.gradle.ExecutableLocator> {
            artifact = "com.google.protobuf:protoc:3.0.0"
        })
        plugins(closureOf<NamedDomainObjectContainer<com.google.protobuf.gradle.ExecutableLocator>> {
            create("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:1.0.0-pre2"
            }
        })
        generateProtoTasks(closureOf<com.google.protobuf.gradle.ProtobufConfigurator.GenerateProtoTaskCollection> {
            ofSourceSet("grpc").forEach { task ->
                task.plugins {
                    get("grpc").apply {
                        outputSubDir = "grpc_output"
                    }
                }
                task.generateDescriptorSet = true
            }
        })
    }
}